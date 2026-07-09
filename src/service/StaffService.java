package service;

import dao.RoleDAO;
import dao.StaffDAO;
import dao.UserDAO;
import model.Staff;
import model.User;
import utils.PasswordUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StaffService {
    private final StaffDAO staffDao = new StaffDAO();
    private final UserDAO userDao = new UserDAO();
    private final RoleDAO roleDao = new RoleDAO();

    public List<Staff> listAll() throws SQLException { return staffDao.findAll(); }
    public Staff get(int id) throws SQLException { return staffDao.findById(id); }

    public int add(String fullName, String email, String phone, String password,
                   String designation, String specialization, String availability) throws SQLException {
        if (userDao.findByEmail(email) != null)
            throw new IllegalStateException("Email already used");

        Integer roleId = roleDao.findIdByName("STAFF");
        User u = new User();
        u.setFullName(fullName); u.setEmail(email); u.setPhone(phone);
        u.setPasswordHash(PasswordUtil.hash(password == null ? "Staff@123" : password));
        u.setRoleId(roleId); u.setStatus("ACTIVE");
        int userId = userDao.insert(u);

        return staffDao.insert(userId, designation, specialization, availability,
            Date.valueOf(LocalDate.now()));
    }

    public boolean update(int staffId, String designation, String specialization, String availability) throws SQLException {
        return staffDao.update(staffId, designation, specialization, availability);
    }

    public boolean delete(int id) throws SQLException { return staffDao.delete(id); }

    public List<Object[]> performance() throws SQLException { return staffDao.performance(); }
}