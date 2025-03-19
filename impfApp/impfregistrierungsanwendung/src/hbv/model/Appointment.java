package hbv.model;
import java.sql.Date;

public class Appointment {
    private int appointmentId;
    private Date dateSlot;
    private String timeSlot;
    private String vaccine;
    private int capacity;
    private int remainingCapacity;
    private String location;
    private String provider;

    public Appointment(int appointmentId, Date dateSlot, String timeSlot, String vaccine,
                       int capacity, int remainingCapacity, String location, String provider) {
        this.appointmentId = appointmentId;
        this.dateSlot = dateSlot;
        this.timeSlot = timeSlot;
        this.vaccine = vaccine;
        this.capacity = capacity;
        this.remainingCapacity = remainingCapacity;
        this.location = location;
        this.provider = provider;
    }

    public int getAppointmentId() { return appointmentId; }
    public Date getDateSlot() { return dateSlot; }
    public String getTimeSlot() { return timeSlot; }
    public String getVaccine() { return vaccine; }
    public int getCapacity() { return capacity; }
    public int getRemainingCapacity() { return remainingCapacity; }
    public String getLocation() { return location; }
    public String getProvider() { return provider; }
    public boolean isAvailable() { return remainingCapacity > 0; }
    public void book() {
        if (!isAvailable()) {
            throw new IllegalStateException("Termin ist ausgebucht.");
        }
        remainingCapacity--;
    }
}
