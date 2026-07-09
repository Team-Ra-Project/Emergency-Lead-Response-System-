package service;

import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report queries — daily / weekly / monthly counts plus staff performance
 * and lead source breakdown. Uses only existing schema tables.
 */
public class ReportService {

    public Map<String,Object> overview() throws SQLException {
        Map<String,Object> m = new LinkedHashMap<>();
        try (Connection c = DBConnection.get()) {
            m.put("totalLeads",     scalar(c, "SELECT COUNT(*) FROM leads"));
            m.put("todayLeads",     scalar(c, "SELECT COUNT(*) FROM leads WHERE DATE(created_at)=CURDATE()"));
            m.put("bookings",       scalar(c, "SELECT COUNT(*) FROM appointments WHERE status IN ('BOOKED','COMPLETED')"));
            m.put("pendingJobs",    scalar(c, "SELECT COUNT(*) FROM leads WHERE status IN ('NEW','CONTACTED','QUALIFIED','QUOTE_SENT','BOOKED')"));
            m.put("completedJobs",  scalar(c, "SELECT COUNT(*) FROM leads WHERE status='COMPLETED'"));
            m.put("lostLeads",      scalar(c, "SELECT COUNT(*) FROM leads WHERE status='LOST'"));
            m.put("totalRevenue",   scalar(c,
                "SELECT COALESCE(SUM(s.base_price),0) " +
                "FROM leads l LEFT JOIN services s ON s.service_id=l.service_id " +
                "WHERE l.status='COMPLETED'"));
            m.put("monthRevenue",   scalar(c,
                "SELECT COALESCE(SUM(s.base_price),0) " +
                "FROM leads l LEFT JOIN services s ON s.service_id=l.service_id " +
                "WHERE l.status='COMPLETED' " +
                "  AND YEAR(l.created_at)=YEAR(CURDATE()) " +
                "  AND MONTH(l.created_at)=MONTH(CURDATE())"));
        }
        return m;
    }

    public List<Map<String,Object>> dailyLeads(int days) throws SQLException {
        String sql = "SELECT DATE(created_at) d, COUNT(*) c FROM leads " +
                     "WHERE created_at >= (CURDATE() - INTERVAL ? DAY) " +
                     "GROUP BY DATE(created_at) ORDER BY d";
        List<Map<String,Object>> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> r = new LinkedHashMap<>();
                    r.put("date", rs.getDate("d").toString());
                    r.put("count", rs.getInt("c"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    public List<Map<String,Object>> monthlyRevenue(int months) throws SQLException {
        String sql =
          "SELECT DATE_FORMAT(l.created_at,'%Y-%m') ym, " +
          "       COALESCE(SUM(s.base_price),0) revenue, COUNT(*) jobs " +
          "FROM leads l LEFT JOIN services s ON s.service_id=l.service_id " +
          "WHERE l.status='COMPLETED' " +
          "  AND l.created_at >= (CURDATE() - INTERVAL ? MONTH) " +
          "GROUP BY ym ORDER BY ym";
        List<Map<String,Object>> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> r = new LinkedHashMap<>();
                    r.put("month", rs.getString("ym"));
                    r.put("revenue", rs.getDouble("revenue"));
                    r.put("jobs", rs.getInt("jobs"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    public double conversionRate() throws SQLException {
        try (Connection c = DBConnection.get()) {
            long total = scalar(c, "SELECT COUNT(*) FROM leads");
            if (total == 0) return 0.0;
            long won = scalar(c, "SELECT COUNT(*) FROM leads WHERE status='COMPLETED'");
            return Math.round((won * 10000.0 / total)) / 100.0;
        }
    }

    public List<Map<String,Object>> leadsBySource() throws SQLException {
        String sql = "SELECT COALESCE(source,'Direct') src, COUNT(*) c FROM leads GROUP BY src ORDER BY c DESC";
        List<Map<String,Object>> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> r = new LinkedHashMap<>();
                r.put("source", rs.getString("src"));
                r.put("count", rs.getInt("c"));
                out.add(r);
            }
        }
        return out;
    }

    private long scalar(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}