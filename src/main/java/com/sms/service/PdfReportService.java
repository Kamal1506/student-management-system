package com.sms.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import com.sms.enums.AssignmentStatus;
import com.sms.model.*;
import com.sms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final StudentRepository      studentRepository;
    private final TeacherRepository      teacherRepository;
    private final CourseRepository       courseRepository;
    private final EnrollmentRepository   enrollmentRepository;
    private final ExamResultRepository   examResultRepository;
    private final AssignmentRepository   assignmentRepository;

    // Emerald green brand colour
    private static final DeviceRgb BRAND_GREEN  = new DeviceRgb(5,  150, 105);
    private static final DeviceRgb DARK_GREEN   = new DeviceRgb(6,  78,  59);
    private static final DeviceRgb LIGHT_GREEN  = new DeviceRgb(209,250, 229);
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(15,  23,  42);
    private static final DeviceRgb TEXT_MUTED   = new DeviceRgb(100,116, 139);
    private static final DeviceRgb ROW_ALT      = new DeviceRgb(248,250, 252);
    private static final DeviceRgb TABLE_HEADER = new DeviceRgb(6,  78,  59);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── 1. STUDENT REPORT CARD ───────────────────────────────────────
    public byte[] generateStudentReport(Long studentId) throws Exception {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Enrollment>  enrollments = enrollmentRepository.findByStudentIdAndActiveTrue(studentId);
        List<ExamResult>  grades      = examResultRepository.findByStudentId(studentId);
        List<Long> courseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .distinct()
                .collect(Collectors.toList());
        List<Assignment> assignments = courseIds.isEmpty()
                ? List.of()
                : assignmentRepository.findByCourseIdIn(courseIds);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer = new PdfWriter(baos);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Header
        addHeader(doc, bold, regular,
                "Student Report Card",
                student.getFirstName() + " " + student.getLastName(),
                "Email: " + student.getEmail() + "   |   Phone: " + (student.getPhone() != null ? student.getPhone() : "—"),
                "Generated: " + LocalDate.now().format(FMT));

        // ── Summary Stats ──────────────────────────────────────────
        addSectionTitle(doc, bold, "Academic Summary");

        double avgScore = grades.stream()
                .mapToDouble(g -> (g.getMarks() / g.getMaxMarks()) * 100)
                .average().orElse(0);
        long completed = assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();

        Table summary = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1})).useAllAvailableWidth();
        addSummaryCell(summary, bold, regular, String.valueOf(enrollments.size()), "Courses Enrolled");
        addSummaryCell(summary, bold, regular, grades.size() + "", "Exams Graded");
        addSummaryCell(summary, bold, regular, String.format("%.1f%%", avgScore), "Average Score");
        addSummaryCell(summary, bold, regular, completed + "/" + assignments.size(), "Assignments Done");
        doc.add(summary);
        doc.add(new Paragraph("\n"));

        // ── Enrollments ────────────────────────────────────────────
        if (!enrollments.isEmpty()) {
            addSectionTitle(doc, bold, "Enrolled Courses");
            Table t = buildTable(new String[]{"#", "Course", "Code", "Teacher", "Enrolled On"}, bold, regular);
            for (int i = 0; i < enrollments.size(); i++) {
                Enrollment e = enrollments.get(i);
                boolean alt = i % 2 == 1;
                addRow(t, regular, alt,
                    String.valueOf(i + 1),
                    e.getCourse().getTitle(),
                    e.getCourse().getCode(),
                    e.getCourse().getTeacher() != null
                            ? e.getCourse().getTeacher().getFirstName() + " " + e.getCourse().getTeacher().getLastName()
                            : "—",
                    e.getEnrolledAt() != null ? e.getEnrolledAt().format(FMT) : "—");
            }
            doc.add(t);
            doc.add(new Paragraph("\n"));
        }

        // ── Grades ────────────────────────────────────────────────
        if (!grades.isEmpty()) {
            addSectionTitle(doc, bold, "Exam Results");
            Table t = buildTable(new String[]{"#", "Course", "Marks", "Max", "Grade", "Score", "Date"}, bold, regular);
            for (int i = 0; i < grades.size(); i++) {
                ExamResult g = grades.get(i);
                double pct = (g.getMarks() / g.getMaxMarks()) * 100;
                boolean alt = i % 2 == 1;
                addRow(t, regular, alt,
                    String.valueOf(i + 1),
                    g.getCourse().getTitle(),
                    String.valueOf(g.getMarks()),
                    String.valueOf(g.getMaxMarks()),
                    g.getGrade(),
                    String.format("%.1f%%", pct),
                    g.getExamDate() != null ? g.getExamDate().format(FMT) : "—");
            }
            doc.add(t);
            doc.add(new Paragraph("\n"));
        }

        // ── Assignments ───────────────────────────────────────────
        if (!assignments.isEmpty()) {
            addSectionTitle(doc, bold, "Assignments");
            Table t = buildTable(new String[]{"#", "Title", "Course", "Due Date", "Status"}, bold, regular);
            for (int i = 0; i < assignments.size(); i++) {
                Assignment a = assignments.get(i);
                boolean alt = i % 2 == 1;
                addRow(t, regular, alt,
                    String.valueOf(i + 1),
                    a.getTitle(),
                    a.getCourse().getTitle(),
                    a.getDueDate() != null ? a.getDueDate().format(FMT) : "—",
                    a.getStatus().name());
            }
            doc.add(t);
        }

        addFooter(doc, regular);
        doc.close();
        return baos.toByteArray();
    }

    // ── 2. COURSE REPORT ─────────────────────────────────────────────
    public byte[] generateCourseReport(Long courseId) throws Exception {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndActiveTrue(courseId);
        List<ExamResult> grades      = examResultRepository.findByCourseId(courseId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = initDoc(baos);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        String teacher = course.getTeacher() != null
                ? course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName()
                : "Unassigned";

        addHeader(doc, bold, regular,
                "Course Report",
                course.getTitle() + " (" + course.getCode() + ")",
                "Teacher: " + teacher + "   |   Students: " + enrollments.size(),
                "Generated: " + LocalDate.now().format(FMT));

        // Summary
        addSectionTitle(doc, bold, "Course Summary");
        double avg = grades.stream().mapToDouble(g -> (g.getMarks() / g.getMaxMarks()) * 100).average().orElse(0);
        double highest = grades.stream().mapToDouble(g -> (g.getMarks() / g.getMaxMarks()) * 100).max().orElse(0);
        double lowest  = grades.stream().mapToDouble(g -> (g.getMarks() / g.getMaxMarks()) * 100).min().orElse(0);

        Table summary = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1})).useAllAvailableWidth();
        addSummaryCell(summary, bold, regular, String.valueOf(enrollments.size()), "Enrolled");
        addSummaryCell(summary, bold, regular, String.format("%.1f%%", avg),     "Class Average");
        addSummaryCell(summary, bold, regular, String.format("%.1f%%", highest), "Highest Score");
        addSummaryCell(summary, bold, regular, String.format("%.1f%%", lowest),  "Lowest Score");
        doc.add(summary);
        doc.add(new Paragraph("\n"));

        // Enrolled Students
        if (!enrollments.isEmpty()) {
            addSectionTitle(doc, bold, "Enrolled Students");
            Table t = buildTable(new String[]{"#", "Student Name", "Email", "Enrolled On", "Status"}, bold, regular);
            for (int i = 0; i < enrollments.size(); i++) {
                Enrollment e = enrollments.get(i);
                boolean alt = i % 2 == 1;
                addRow(t, regular, alt,
                    String.valueOf(i + 1),
                    e.getStudent().getFirstName() + " " + e.getStudent().getLastName(),
                    e.getStudent().getEmail(),
                    e.getEnrolledAt() != null ? e.getEnrolledAt().format(FMT) : "—",
                    e.isActive() ? "Active" : "Inactive");
            }
            doc.add(t);
            doc.add(new Paragraph("\n"));
        }

        // Grade Sheet
        if (!grades.isEmpty()) {
            addSectionTitle(doc, bold, "Grade Sheet");
            Table t = buildTable(new String[]{"#", "Student", "Marks", "Max", "Grade", "Score", "Date"}, bold, regular);
            for (int i = 0; i < grades.size(); i++) {
                ExamResult g = grades.get(i);
                double pct = (g.getMarks() / g.getMaxMarks()) * 100;
                boolean alt = i % 2 == 1;
                addRow(t, regular, alt,
                    String.valueOf(i + 1),
                    g.getStudent().getFirstName() + " " + g.getStudent().getLastName(),
                    String.valueOf(g.getMarks()),
                    String.valueOf(g.getMaxMarks()),
                    g.getGrade(),
                    String.format("%.1f%%", pct),
                    g.getExamDate() != null ? g.getExamDate().format(FMT) : "—");
            }
            doc.add(t);
        }

        addFooter(doc, regular);
        doc.close();
        return baos.toByteArray();
    }

    // ── 3. ADMIN SUMMARY REPORT ──────────────────────────────────────
    public byte[] generateAdminSummaryReport() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = initDoc(baos);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        long totalStudents   = studentRepository.count();
        long totalTeachers   = teacherRepository.count();
        long totalCourses    = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long totalGrades     = examResultRepository.count();

        addHeader(doc, bold, regular,
                "School Summary Report",
                "EduPortal — Academic Overview",
                "Complete snapshot of all academic data",
                "Generated: " + LocalDate.now().format(FMT));

        // Top stats
        addSectionTitle(doc, bold, "System Overview");
        Table summary = new Table(UnitValue.createPercentArray(new float[]{1,1,1,1,1})).useAllAvailableWidth();
        addSummaryCell(summary, bold, regular, String.valueOf(totalStudents),   "Students");
        addSummaryCell(summary, bold, regular, String.valueOf(totalTeachers),   "Teachers");
        addSummaryCell(summary, bold, regular, String.valueOf(totalCourses),    "Courses");
        addSummaryCell(summary, bold, regular, String.valueOf(totalEnrollments),"Enrollments");
        addSummaryCell(summary, bold, regular, String.valueOf(totalGrades),     "Grades Given");
        doc.add(summary);
        doc.add(new Paragraph("\n"));

        // All Students
        List<Student> students = studentRepository.findAll();
        if (!students.isEmpty()) {
            addSectionTitle(doc, bold, "All Students");
            Table t = buildTable(new String[]{"#", "Name", "Email", "Phone"}, bold, regular);
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                addRow(t, regular, i%2==1,
                    String.valueOf(i+1),
                    s.getFirstName()+" "+s.getLastName(),
                    s.getEmail(),
                    s.getPhone()!=null?s.getPhone():"—");
            }
            doc.add(t);
            doc.add(new Paragraph("\n"));
        }

        // All Teachers
        List<Teacher> teachers = teacherRepository.findAll();
        if (!teachers.isEmpty()) {
            addSectionTitle(doc, bold, "All Teachers");
            Table t = buildTable(new String[]{"#", "Name", "Email", "Subject"}, bold, regular);
            for (int i = 0; i < teachers.size(); i++) {
                Teacher tc = teachers.get(i);
                addRow(t, regular, i%2==1,
                    String.valueOf(i+1),
                    tc.getFirstName()+" "+tc.getLastName(),
                    tc.getEmail(),
                    tc.getSubject()!=null?tc.getSubject():"—");
            }
            doc.add(t);
            doc.add(new Paragraph("\n"));
        }

        // All Courses
        List<Course> courses = courseRepository.findAll();
        if (!courses.isEmpty()) {
            addSectionTitle(doc, bold, "All Courses");
            Table t = buildTable(new String[]{"#", "Title", "Code", "Teacher"}, bold, regular);
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                String tName = c.getTeacher()!=null ? c.getTeacher().getFirstName()+" "+c.getTeacher().getLastName() : "—";
                addRow(t, regular, i%2==1,
                    String.valueOf(i+1), c.getTitle(), c.getCode(), tName);
            }
            doc.add(t);
        }

        addFooter(doc, regular);
        doc.close();
        return baos.toByteArray();
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private Document initDoc(ByteArrayOutputStream baos) throws Exception {
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);
        return doc;
    }

    private void addHeader(Document doc, PdfFont bold, PdfFont regular,
                            String title, String name, String subtitle, String date) {
        // Top green bar
        Table header = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();

        Cell left = new Cell().setBorder(null)
                .setBackgroundColor(DARK_GREEN)
                .setPadding(20);
        left.add(new Paragraph("🏫 EduPortal").setFont(bold).setFontSize(11).setFontColor(ColorConstants.WHITE));
        left.add(new Paragraph(title).setFont(bold).setFontSize(20).setFontColor(ColorConstants.WHITE).setMarginTop(6));
        left.add(new Paragraph(name).setFont(bold).setFontSize(14).setFontColor(new DeviceRgb(167,243,208)).setMarginTop(4));
        left.add(new Paragraph(subtitle).setFont(regular).setFontSize(10).setFontColor(new DeviceRgb(167,243,208)).setMarginTop(2));
        header.addCell(left);

        Cell right = new Cell().setBorder(null)
                .setBackgroundColor(BRAND_GREEN)
                .setPadding(20)
                .setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph(date).setFont(regular).setFontSize(10).setFontColor(ColorConstants.WHITE));
        header.addCell(right);

        doc.add(header);
        doc.add(new Paragraph("\n"));
    }

    private void addSectionTitle(Document doc, PdfFont bold, String title) {
        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(13)
                .setFontColor(DARK_GREEN)
                .setBorderBottom(new SolidBorder(BRAND_GREEN, 2))
                .setPaddingBottom(6).setMarginBottom(12));
    }

    private void addSummaryCell(Table t, PdfFont bold, PdfFont regular, String value, String label) {
        Cell c = new Cell().setBorder(new SolidBorder(new DeviceRgb(226,232,240), 1))
                .setBackgroundColor(ROW_ALT)
                .setPadding(16).setTextAlignment(TextAlignment.CENTER);
        c.add(new Paragraph(value).setFont(bold).setFontSize(24).setFontColor(BRAND_GREEN).setMargin(0));
        c.add(new Paragraph(label).setFont(regular).setFontSize(10).setFontColor(TEXT_MUTED).setMarginTop(4));
        t.addCell(c);
    }

    private Table buildTable(String[] headers, PdfFont bold, PdfFont regular) {
        float[] widths = new float[headers.length];
        for (int i = 0; i < widths.length; i++) widths[i] = 1;
        widths[0] = 0.4f; // # column narrower
        Table t = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        for (String h : headers) {
            t.addHeaderCell(new Cell()
                    .setBackgroundColor(TABLE_HEADER)
                    .setBorder(null).setPadding(10)
                    .add(new Paragraph(h).setFont(bold).setFontSize(10)
                            .setFontColor(ColorConstants.WHITE)));
        }
        return t;
    }

    private void addRow(Table t, PdfFont regular, boolean alt, String... values) {
        DeviceRgb bg = alt ? ROW_ALT : new DeviceRgb(255, 255, 255);
        for (String v : values) {
            t.addCell(new Cell()
                    .setBackgroundColor(bg)
                    .setBorderBottom(new SolidBorder(new DeviceRgb(226,232,240), 0.5f))
                    .setBorderLeft(null).setBorderRight(null).setBorderTop(null)
                    .setPadding(10)
                    .add(new Paragraph(v != null ? v : "—")
                            .setFont(regular).setFontSize(11).setFontColor(TEXT_DARK)));
        }
    }

    private void addFooter(Document doc, PdfFont regular) {
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("This report was automatically generated by EduPortal. For queries, contact your administrator.")
                .setFont(regular).setFontSize(9).setFontColor(TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderTop(new SolidBorder(new DeviceRgb(226,232,240), 1))
                .setPaddingTop(12));
    }
}
