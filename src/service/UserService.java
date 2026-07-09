package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private final UserDAO dao = new UserDAO();
    public List<User> listAll() throws SQLException { return dao.findAll(); }
    public User get(int id) throws SQLException { return dao.findById(id); }
    public boolean update(User u) throws SQLException { return dao.update(u); }
    public boolean delete(int id) throws SQLException { return dao.delete(id); }
}