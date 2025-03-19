package hbv.service;

import hbv.model.User;
import java.sql.SQLException;

public interface IUserService {
    boolean registerUser(String email, String password, String vorname, String nachname, String role, String regCode) throws SQLException;
    boolean authenticateUser(String email, String password) throws SQLException;
    User getUser(String email) throws SQLException;
}
