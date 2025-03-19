package hbv.model;
import java.sql.Date;
public class User {
    private int id;
    private String email;
    private String passwordHash;
    private String vorname;
    private String nachname;
    private String role;
    private Date geburtsdatum;
    public User(String email, String passwordHash, String vorname, String nachname, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.vorname = vorname;
        this.nachname = nachname;
        this.role = role;
    }
    public User(int id, String email, String passwordHash, String vorname, String nachname, String role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.vorname = vorname;
        this.nachname = nachname;
        this.role = role;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getVorname() { return vorname; }
    public String getNachname() { return nachname; }
    public String getRole() { return role; }
    public Date getGeburtsdatum() { return geburtsdatum; }
    public void setGeburtsdatum(Date geburtsdatum) { this.geburtsdatum = geburtsdatum; }
}
