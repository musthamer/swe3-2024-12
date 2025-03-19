package hbv.service;

import hbv.model.User;
import java.sql.SQLException;

public class LoggingUserServiceDecorator implements IUserService {
    private final IUserService delegate;

    public LoggingUserServiceDecorator(IUserService delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean registerUser(String email, String password, String vorname, String nachname, String role, String regCode) throws SQLException {
        System.out.println("Registrierungsversuch für: " + email);
        boolean result = delegate.registerUser(email, password, vorname, nachname, role, regCode);
        System.out.println("Registrierungsergebnis für " + email + ": " + result);
        return result;
    }

    @Override
    public boolean authenticateUser(String email, String password) throws SQLException {
        System.out.println("Authentifizierungsversuch für: " + email);
        boolean result = delegate.authenticateUser(email, password);
        System.out.println("Authentifizierungsergebnis für " + email + ": " + result);
        return result;
    }

    @Override
    public User getUser(String email) throws SQLException {
        return delegate.getUser(email);
    }
}
