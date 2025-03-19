package hbv.model;
import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private int userId;
    private int appointmentId;
    private String bookingName;
    private Timestamp bookingTime;

    public Booking(int bookingId, int userId, int appointmentId, String bookingName, Timestamp bookingTime) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.appointmentId = appointmentId;
        this.bookingName = bookingName;
        this.bookingTime = bookingTime;
    }

    public int getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getAppointmentId() { return appointmentId; }
    public String getBookingName() { return bookingName; }
    public Timestamp getBookingTime() { return bookingTime; }
}
