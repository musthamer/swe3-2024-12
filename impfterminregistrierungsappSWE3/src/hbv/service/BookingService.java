package hbv.service;

import java.sql.*;
import java.util.*;
import hbv.messaging.EmailMessageFactory;
import hbv.messaging.RedisEmailSender;
import hbv.utils.DbUtils;
import hbv.web.PDFGenerator;

public class BookingService {

    private static class ConfirmationData {
        final String personName;
        final java.util.Date appointmentDate;
        final String vaccinationCenter;
        final String vaccineType;
        final String email;

        private ConfirmationData(String personName, java.util.Date appointmentDate, String vaccinationCenter, String vaccineType, String email) {
            this.personName = personName;
            this.appointmentDate = appointmentDate;
            this.vaccinationCenter = vaccinationCenter;
            this.vaccineType = vaccineType;
            this.email = email;
        }

        boolean isComplete() {
            return appointmentDate != null
                && personName != null && !personName.isEmpty()
                && email != null && !email.isEmpty();
        }
    }

    public List<Map<String, Object>> getAppointmentsForUser(int userId) throws Exception {
        List<Map<String, Object>> appointments = new ArrayList<>();
        
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT b.id, b.status, p.first_name, p.last_name, t.start_time, t.end_time, v.name AS vaccine_name, " +
                "vc.name AS center_name, vc.address AS center_address " +
                "FROM booking b " +
                "JOIN person p ON b.person_id = p.id " +
                "JOIN timeslot t ON b.timeslot_id = t.id " +
                "JOIN vaccine v ON b.vaccine_id = v.id " +
                "JOIN vaccination_center vc ON t.center_id = vc.id " +
                "WHERE b.account_id = ? " +
                "ORDER BY t.start_time"
            );
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", rs.getInt("id"));
                appointment.put("status", rs.getString("status"));
                appointment.put("firstName", rs.getString("first_name"));
                appointment.put("lastName", rs.getString("last_name"));
                appointment.put("startTime", rs.getTimestamp("start_time"));
                appointment.put("endTime", rs.getTimestamp("end_time"));
                appointment.put("vaccineName", rs.getString("vaccine_name"));
                appointment.put("centerName", rs.getString("center_name"));
                appointment.put("centerAddress", rs.getString("center_address"));
                appointments.add(appointment);
            }
        }
        
        return appointments;
    }
    
    public Map<String, Object> bookAppointment(int userId, int timeslotId, int vaccineId, int personId,
            String baseUrl, String webapp, String sessionId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = DbUtils.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String limitError = checkBookingLimit(connection, userId);
                if (limitError != null) {
                    fail(result, connection, limitError);
                    return result;
                }

                String vaccineError = checkVaccineAvailability(connection, timeslotId, vaccineId);
                if (vaccineError != null) {
                    fail(result, connection, vaccineError);
                    return result;
                }

                String slotError = checkTimeslotCapacity(connection, timeslotId);
                if (slotError != null) {
                    fail(result, connection, slotError);
                    return result;
                }

                decrementVaccineDose(connection, timeslotId, vaccineId);
                int bookingId = createBooking(connection, personId, userId, timeslotId, vaccineId);
                ConfirmationData confirmationData = loadConfirmationData(connection, timeslotId, vaccineId, personId, userId);

                connection.commit();
                result.put("success", true);
                result.put("message", "Termin erfolgreich gebucht");
                
                sendConfirmationBestEffort(bookingId, confirmationData, baseUrl, webapp, sessionId);
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
        return result;
    }

    private void fail(Map<String, Object> result, Connection connection, String message) throws SQLException {
        result.put("success", false);
        result.put("message", message);
        connection.rollback();
    }

    private String checkBookingLimit(Connection connection, int userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) AS booking_count FROM booking " +
            "WHERE account_id = ? AND status != 'CANCELLED'"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getInt("booking_count") >= 4) {
            return "Sie können nicht mehr als 4 Impftermine gleichzeitig buchen.";
        }
        return null;
    }

    private String checkVaccineAvailability(Connection connection, int timeslotId, int vaccineId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT available_doses FROM vaccination_center_vaccine " +
            "WHERE center_id = (SELECT center_id FROM timeslot WHERE id = ?) " +
            "AND vaccine_id = ? " +
            "FOR UPDATE NOWAIT"
        );
        ps.setInt(1, timeslotId);
        ps.setInt(2, vaccineId);
        ResultSet rs = ps.executeQuery();
        if (!rs.next() || rs.getInt("available_doses") <= 0) {
            return "Der gewählte Impfstoff ist leider nicht mehr verfügbar.";
        }
        return null;
    }

    private String checkTimeslotCapacity(Connection connection, int timeslotId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT t.capacity, COUNT(b.id) AS booked " +
            "FROM timeslot t " +
            "LEFT JOIN booking b ON t.id = b.timeslot_id AND b.status = 'CONFIRMED' " +
            "WHERE t.id = ? " +
            "GROUP BY t.id, t.capacity " +
            "FOR UPDATE NOWAIT"
        );
        ps.setInt(1, timeslotId);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            return "Der gewählte Termin wurde nicht gefunden.";
        }

        int capacity = rs.getInt("capacity");
        int booked = rs.getInt("booked");
        if (booked >= capacity) {
            return "Der gewählte Termin ist leider nicht mehr verfügbar.";
        }
        return null;
    }

    private void decrementVaccineDose(Connection connection, int timeslotId, int vaccineId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE vaccination_center_vaccine " +
            "SET available_doses = available_doses - 1 " +
            "WHERE center_id = (SELECT center_id FROM timeslot WHERE id = ?) " +
            "AND vaccine_id = ?"
        );
        ps.setInt(1, timeslotId);
        ps.setInt(2, vaccineId);
        ps.executeUpdate();
    }

    private int createBooking(Connection connection, int personId, int userId, int timeslotId, int vaccineId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO booking (person_id, account_id, timeslot_id, vaccine_id, booking_date, status) " +
            "VALUES (?, ?, ?, ?, NOW(), 'CONFIRMED')",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setInt(1, personId);
        ps.setInt(2, userId);
        ps.setInt(3, timeslotId);
        ps.setInt(4, vaccineId);
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        return 0;
    }

    private ConfirmationData loadConfirmationData(Connection connection, int timeslotId, int vaccineId,
            int personId, int accountId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT p.first_name, p.last_name, a.email AS account_email, t.start_time, " +
            "vc.name AS center_name, v.name AS vaccine_name " +
            "FROM person p " +
            "JOIN account a ON a.id = ? " +
            "JOIN timeslot t ON t.id = ? " +
            "JOIN vaccination_center vc ON t.center_id = vc.id " +
            "JOIN vaccine v ON v.id = ? " +
            "WHERE p.id = ?"
        );
        ps.setInt(1, accountId);
        ps.setInt(2, timeslotId);
        ps.setInt(3, vaccineId);
        ps.setInt(4, personId);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            return new ConfirmationData("", null, "", "", "");
        }

        String personName = rs.getString("first_name") + " " + rs.getString("last_name");
        java.util.Date appointmentDate = rs.getTimestamp("start_time");
        String vaccinationCenter = rs.getString("center_name");
        String vaccineType = rs.getString("vaccine_name");
        String email = rs.getString("account_email");

        return new ConfirmationData(personName, appointmentDate, vaccinationCenter, vaccineType, email);
    }

    private void sendConfirmationBestEffort(int bookingId, ConfirmationData data, String baseUrl, String webapp,
            String sessionId) {
        if (bookingId <= 0 || data == null || !data.isComplete()) {
            return;
        }

        try {
            byte[] pdfData = PDFGenerator.generateVaccinationConfirmation(
                data.personName, data.appointmentDate, data.vaccinationCenter, data.vaccineType, bookingId,
                baseUrl, webapp
            );

            RedisEmailSender.send(EmailMessageFactory.bookingConfirmation(
                data.email, data.personName, data.appointmentDate,
                data.vaccinationCenter, data.vaccineType, pdfData
            ), sessionId);
        } catch (Exception ignored) {
        }
    }

    public Map<String, Object> getAppointmentById(int bookingId) throws Exception {
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT b.id, b.status, p.first_name, p.last_name, t.start_time, t.end_time, v.name AS vaccine_name, " +
                "vc.name AS center_name, vc.address AS center_address " +
                "FROM booking b " +
                "JOIN person p ON b.person_id = p.id " +
                "JOIN timeslot t ON b.timeslot_id = t.id " +
                "JOIN vaccine v ON b.vaccine_id = v.id " +
                "JOIN vaccination_center vc ON t.center_id = vc.id " +
                "WHERE b.id = ?"
            );
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", rs.getInt("id"));
                appointment.put("status", rs.getString("status"));
                appointment.put("firstName", rs.getString("first_name"));
                appointment.put("lastName", rs.getString("last_name"));
                appointment.put("startTime", rs.getTimestamp("start_time"));
                appointment.put("endTime", rs.getTimestamp("end_time"));
                appointment.put("vaccineName", rs.getString("vaccine_name"));
                appointment.put("centerName", rs.getString("center_name"));
                appointment.put("centerAddress", rs.getString("center_address"));
                return appointment;
            }
        }
        
        return null;
    }

    public Map<String, Object> getConfirmedAppointmentForAccount(int bookingId, int accountId) throws Exception {
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT b.id, p.first_name, p.last_name, t.start_time, vc.name AS center_name, v.name AS vaccine_name " +
                "FROM booking b " +
                "JOIN person p ON b.person_id = p.id " +
                "JOIN timeslot t ON b.timeslot_id = t.id " +
                "JOIN vaccination_center vc ON t.center_id = vc.id " +
                "JOIN vaccine v ON b.vaccine_id = v.id " +
                "WHERE b.id = ? AND b.account_id = ? AND b.status = 'CONFIRMED'"
            );
            ps.setInt(1, bookingId);
            ps.setInt(2, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", rs.getInt("id"));
                appointment.put("personName", rs.getString("first_name") + " " + rs.getString("last_name"));
                appointment.put("startTime", rs.getTimestamp("start_time"));
                appointment.put("centerName", rs.getString("center_name"));
                appointment.put("vaccineName", rs.getString("vaccine_name"));
                return appointment;
            }
        }

        return null;
    }

    public boolean updateAppointmentStatus(int bookingId, String newStatus) throws Exception {
        try (Connection connection = DbUtils.getConnection()) {
            PreparedStatement checkPs = connection.prepareStatement(
                "SELECT status FROM booking WHERE id = ?"
            );
            checkPs.setInt(1, bookingId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                String currentStatus = rs.getString("status");
                if ("CANCELLED".equals(currentStatus)) {
                    return false;
                }

                PreparedStatement updatePs = connection.prepareStatement(
                    "UPDATE booking SET status = ? WHERE id = ?"
                );
                updatePs.setString(1, newStatus);
                updatePs.setInt(2, bookingId);
                
                return updatePs.executeUpdate() > 0;
            }
            
            return false;
        }
    }

    public Map<String, Object> cancelAppointment(int userId, int bookingId) throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (Connection connection = DbUtils.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT b.status, b.vaccine_id, b.timeslot_id, t.center_id " +
                    "FROM booking b " +
                    "JOIN timeslot t ON b.timeslot_id = t.id " +
                    "WHERE b.id = ? AND b.account_id = ? " +
                    "FOR UPDATE"
                );
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    result.put("success", false);
                    result.put("message", "Termin nicht gefunden");
                    connection.rollback();
                    return result;
                }

                String status = rs.getString("status");
                int vaccineId = rs.getInt("vaccine_id");
                int centerId = rs.getInt("center_id");

                if (!"CONFIRMED".equals(status)) {
                    result.put("success", false);
                    result.put("message", "Termin kann nicht storniert werden (Status: " + status + ")");
                    connection.rollback();
                    return result;
                }

                PreparedStatement cancelPs = connection.prepareStatement(
                    "UPDATE booking SET status = 'CANCELLED' WHERE id = ? AND account_id = ? AND status = 'CONFIRMED'"
                );
                cancelPs.setInt(1, bookingId);
                cancelPs.setInt(2, userId);
                int updated = cancelPs.executeUpdate();
                if (updated <= 0) {
                    result.put("success", false);
                    result.put("message", "Termin konnte nicht storniert werden");
                    connection.rollback();
                    return result;
                }

                PreparedStatement refundPs = connection.prepareStatement(
                    "UPDATE vaccination_center_vaccine " +
                    "SET available_doses = available_doses + 1 " +
                    "WHERE center_id = ? AND vaccine_id = ?"
                );
                refundPs.setInt(1, centerId);
                refundPs.setInt(2, vaccineId);
                refundPs.executeUpdate();

                connection.commit();
                result.put("success", true);
                result.put("message", "Termin storniert");
                return result;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
} 