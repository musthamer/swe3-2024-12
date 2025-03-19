package hbv.service;

import hbv.db.UserVerwalten;
import hbv.model.User;
import hbv.util.PasswortHelper;
import hbv.util.PasswortService;
import java.sql.SQLException;

public class UserService implements IUserService {
    private final UserVerwalten userVerwalten;
    private final PasswortService passwortService;

    public UserService() {
        this.userVerwalten = UserVerwalten.getInstance();
        this.passwortService = new PasswortHelper();
    }

    @Override
    public boolean registerUser(String email, String password, String vorname, String nachname, String role, String regCode) throws SQLException {
        if (userVerwalten.existiertUser(email)) return false;
        String passwordHash = passwortService.hashePasswortMitSalt(password);
        User user = new User(email, passwordHash, vorname, nachname, role);
        return userVerwalten.speichereUser(user);
    }

    @Override
    public boolean authenticateUser(String email, String password) throws SQLException {
        User user = userVerwalten.findeUser(email);
        if (user == null) return false;
        return passwortService.passwortVergleichen(password, user.getPasswordHash());
    }

    @Override
    public User getUser(String email) throws SQLException {
        return userVerwalten.findeUser(email);
    }
}
