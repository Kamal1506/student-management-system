package com.sms.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name:EduPortal}")
    private String appName;

    @Value("${app.email.from}")
    private String fromEmail;

    // ── CORE SEND ────────────────────────────────────────────────────
    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            log.info("Sending email -> to: {} | subject: {}", to, subject);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject("[" + appName + "] " + subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent -> {} | {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} | subject: {}", to, subject, e);
        }
    }

    // ── 1. ENROLLMENT NOTIFICATION ───────────────────────────────────
    @Async
    public void sendEnrollmentNotification(String studentEmail,
                                            String studentName,
                                            String courseTitle,
                                            String courseCode,
                                            String teacherName) {
        String subject = "You've been enrolled in " + courseTitle;
        String body = buildHtml(
            "Course Enrollment Confirmed 🎓",
            "Hi " + studentName + ",",
            "You have been successfully enrolled in the following course:",
            new String[][]{
                {"Course",   courseTitle},
                {"Code",     courseCode},
                {"Teacher",  teacherName}
            },
            "Log in to your student dashboard to view your course materials and upcoming assignments.",
            "#059669"
        );
        sendHtml(studentEmail, subject, body);
    }

    // ── 2. ASSIGNMENT CREATED ────────────────────────────────────────
    @Async
    public void sendAssignmentNotification(String studentEmail,
                                            String studentName,
                                            String assignmentTitle,
                                            String courseTitle,
                                            String dueDate,
                                            String description) {
        String subject = "New Assignment: " + assignmentTitle;
        String body = buildHtml(
            "New Assignment Posted 📝",
            "Hi " + studentName + ",",
            "A new assignment has been posted for one of your courses:",
            new String[][]{
                {"Assignment", assignmentTitle},
                {"Course",     courseTitle},
                {"Due Date",   dueDate},
                {"Details",    description != null ? description : "See your dashboard for details"}
            },
            "Log in to your student dashboard to view the full assignment details and submit your work.",
            "#3b82f6"
        );
        sendHtml(studentEmail, subject, body);
    }

    // ── 3. GRADE SUBMITTED ───────────────────────────────────────────
    @Async
    public void sendGradeNotification(String studentEmail,
                                       String studentName,
                                       String courseTitle,
                                       double marks,
                                       double maxMarks,
                                       String grade,
                                       String examDate) {
        double percentage = (marks / maxMarks) * 100;
        String emoji = percentage >= 75 ? "🏆" : percentage >= 50 ? "👍" : "📊";
        String subject = "Exam Result Posted — " + courseTitle;
        String body = buildHtml(
            "Your Exam Result is Ready " + emoji,
            "Hi " + studentName + ",",
            "Your exam result has been submitted by your teacher:",
            new String[][]{
                {"Course",     courseTitle},
                {"Marks",      marks + " / " + maxMarks},
                {"Grade",      grade},
                {"Score",      String.format("%.1f%%", percentage)},
                {"Exam Date",  examDate}
            },
            "Log in to your student dashboard to view your full grade history.",
            percentage >= 75 ? "#059669" : percentage >= 50 ? "#f59e0b" : "#ef4444"
        );
        sendHtml(studentEmail, subject, body);
    }

    // ── 4. WELCOME EMAIL ─────────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String username, String role) {
        String subject = "Welcome to " + appName + "!";
        String body = buildHtml(
            "Welcome to " + appName + " 🏫",
            "Hi " + username + ",",
            "Your account has been created successfully. Here are your details:",
            new String[][]{
                {"Username", username},
                {"Role",     role},
                {"Portal",   "http://localhost:8080/login.html"}
            },
            "You can now log in to access your " + role.toLowerCase() + " dashboard. " +
            "Please keep your credentials safe.",
            "#059669"
        );
        sendHtml(toEmail, subject, body);
    }

    // ── HTML TEMPLATE BUILDER ────────────────────────────────────────
    private String buildHtml(String title, String greeting, String intro,
                              String[][] rows, String footer, String accentColor) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8"/>
            <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap" rel="stylesheet"/>
            </head>
            <body style="margin:0;padding:0;background:#f8fafc;font-family:'Poppins',sans-serif">
            <table width="100%" cellpadding="0" cellspacing="0" style="background:#f8fafc;padding:40px 0">
            <tr><td align="center">
            <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08)">
            """);

        // Header bar
        sb.append("<tr><td style=\"background:linear-gradient(135deg,#064e3b,").append(accentColor)
          .append(");padding:32px 40px\">");
        sb.append("<div style=\"font-size:22px;font-weight:700;color:#ffffff;letter-spacing:-.3px\">🏫 EduPortal</div>");
        sb.append("<div style=\"font-size:24px;font-weight:800;color:#ffffff;margin-top:16px;letter-spacing:-.5px\">")
          .append(title).append("</div>");
        sb.append("</td></tr>");

        // Body
        sb.append("<tr><td style=\"padding:36px 40px\">");
        sb.append("<p style=\"font-size:16px;font-weight:600;color:#0f172a;margin:0 0 8px\">").append(greeting).append("</p>");
        sb.append("<p style=\"font-size:14px;color:#64748b;margin:0 0 24px;line-height:1.7\">").append(intro).append("</p>");

        // Info table
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f8fafc;border-radius:12px;overflow:hidden;margin-bottom:24px\">");
        for (String[] row : rows) {
            sb.append("<tr>")
              .append("<td style=\"padding:12px 20px;font-size:12px;font-weight:700;color:#94a3b8;text-transform:uppercase;letter-spacing:.06em;width:120px;border-bottom:1px solid #e2e8f0\">")
              .append(row[0]).append("</td>")
              .append("<td style=\"padding:12px 20px;font-size:14px;font-weight:600;color:#0f172a;border-bottom:1px solid #e2e8f0\">")
              .append(row[1]).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");

        // Footer note
        sb.append("<p style=\"font-size:13px;color:#64748b;line-height:1.7;margin:0 0 24px\">").append(footer).append("</p>");

        // CTA button
        sb.append("<a href=\"http://localhost:8080/login.html\" style=\"display:inline-block;background:")
          .append(accentColor)
          .append(";color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-size:14px;font-weight:600\">")
          .append("Go to Dashboard →</a>");

        sb.append("</td></tr>");

        // Footer
        sb.append("""
            <tr><td style="padding:20px 40px;border-top:1px solid #e2e8f0;text-align:center">
            <p style="font-size:12px;color:#94a3b8;margin:0">
              This is an automated message from EduPortal. Please do not reply to this email.
            </p>
            </td></tr>
            </table>
            </td></tr>
            </table>
            </body>
            </html>
            """);
        return sb.toString();
    }
}
