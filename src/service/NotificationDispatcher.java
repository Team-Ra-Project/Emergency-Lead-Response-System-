package service;

import dao.UserDAO;
import model.User;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Sends notifications through EMAIL / SMS / WHATSAPP / BROWSER channels.
 *
 * The real transport integrations (SMTP, Twilio, WhatsApp Business API) are
 * owned by other modules of the ELRS project. This dispatcher provides the
 * seam our module needs: it resolves the recipient, logs the outbound
 * attempt, and returns a delivery status string that {@link NotificationService}
 * persists together with the in-app record. Swapping in a real transport
 * later is a one-method change per channel.
 */
public class NotificationDispatcher {
    private static final Logger LOG = Logger.getLogger(NotificationDispatcher.class.getName());
    private final UserDAO users = new UserDAO();

    public String dispatch(int userId, String channel, String title, String message) {
        String ch = channel == null ? "BROWSER" : channel.toUpperCase();
        User u;
        try { u = users.findById(userId); }
        catch (Exception e) { return "FAILED: recipient lookup — " + e.getMessage(); }
        if (u == null) return "FAILED: recipient not found";

        switch (ch) {
            case "EMAIL":    return sendEmail(u, title, message);
            case "SMS":      return sendSms(u, title, message);
            case "WHATSAPP": return sendWhatsApp(u, title, message);
            case "BROWSER":  return "QUEUED: in-app bell";
            default:         return "FAILED: unknown channel " + ch;
        }
    }

    private String sendEmail(User u, String title, String message) {
        if (u.getEmail() == null || u.getEmail().isBlank())
            return "FAILED: user has no email address";
        LOG.info(() -> "[EMAIL " + LocalDateTime.now() + "] to=" + u.getEmail()
                + " subject=" + title + " body=" + message);
        // TODO: hand off to SMTP integration (owned by integrations module)
        return "QUEUED: email → " + u.getEmail();
    }

    private String sendSms(User u, String title, String message) {
        if (u.getPhone() == null || u.getPhone().isBlank())
            return "FAILED: user has no phone number";
        LOG.info(() -> "[SMS " + LocalDateTime.now() + "] to=" + u.getPhone()
                + " text=" + title + " — " + message);
        // TODO: hand off to SMS gateway integration
        return "QUEUED: sms → " + u.getPhone();
    }

    private String sendWhatsApp(User u, String title, String message) {
        if (u.getPhone() == null || u.getPhone().isBlank())
            return "FAILED: user has no phone number";
        LOG.info(() -> "[WHATSAPP " + LocalDateTime.now() + "] to=" + u.getPhone()
                + " text=" + title + " — " + message);
        // TODO: hand off to WhatsApp Business API integration
        return "QUEUED: whatsapp → " + u.getPhone();
    }
}