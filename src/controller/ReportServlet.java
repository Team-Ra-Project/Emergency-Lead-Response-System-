package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ReportService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  /api/reports              → dashboard overview
 *  /api/reports/daily?days=7
 *  /api/reports/monthly?months=6
 *  /api/reports/conversion
 *  /api/reports/sources
 */
@WebServlet(urlPatterns = {"/api/reports", "/api/reports/*"})
public class ReportServlet extends HttpServlet {
    private final ReportService svc = new ReportService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String p = req.getPathInfo();
            if (p == null || "/".equals(p)) { JsonUtil.ok(resp, svc.overview()); return; }
            switch (p) {
                case "/daily":
                    JsonUtil.ok(resp, svc.dailyLeads(RequestUtil.intParam(req,"days",7))); return;
                case "/monthly":
                    JsonUtil.ok(resp, svc.monthlyRevenue(RequestUtil.intParam(req,"months",6))); return;
                case "/conversion":
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("conversionRate", svc.conversionRate());
                    JsonUtil.ok(resp, m); return;
                case "/sources":
                    JsonUtil.ok(resp, svc.leadsBySource()); return;
                default:
                    JsonUtil.error(resp, 404, "Unknown report");
            }
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }
}