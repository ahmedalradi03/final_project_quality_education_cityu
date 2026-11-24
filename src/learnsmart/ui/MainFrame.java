package learnsmart.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import learnsmart.data.DataManager;
import learnsmart.model.*;

/**
 * Compact Swing GUI for LearnSmart: handles login, registration,
 * student quizzes/XP/leaderboard, teacher question bank, and file persistence.
 */
public class MainFrame extends JFrame {

    // Theme colors
    private static final Color BG = new Color(7, 12, 24);
    private static final Color PANEL = new Color(12, 22, 44);
    private static final Color FIELD = new Color(8, 15, 32);
    private static final Color CYAN = new Color(43, 208, 255);
    private static final Color ORANGE = new Color(255, 106, 61);
    private static final Color GOLD = new Color(255, 200, 87);
    private static final Color GOOD = new Color(52, 211, 153);
    private static final Color TEXT = new Color(234, 242, 255);
    private static final Color MUTED = new Color(143, 163, 196);
    private static final String FAMILY = pickFamily();

    private final DataManager data = DataManager.getInstance();
    private final CardLayout cards = new CardLayout();
    private final BgPanel root = new BgPanel(loadBackground());
    private JPanel dashboard;

    private final JTextField idField = new JTextField(16);
    private final JPasswordField pwField = new JPasswordField(16);

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public MainFrame() {
        super("LearnSmart - Quality Education (SDG 4)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 700);
        setLocationRelativeTo(null);
        root.setLayout(cards);
        root.add(buildLoginPanel(), "login");
        setContentPane(root);

        // Save data when closing the application
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { data.saveToFile(); }
        });
    }

    // ==================== Login & Registration ====================

    private JPanel buildLoginPanel() {
        JPanel wrap = new JPanel(new GridLayout(1, 2, 36, 0));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(48, 60, 48, 60));

        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setOpaque(false);
        hero.add(lbl("LearnSmart", 32, Font.BOLD, CYAN));
        hero.add(Box.createVerticalStrut(16));
        hero.add(lbl("Practice that feels like a game.", 28, Font.BOLD, TEXT));
        hero.add(Box.createVerticalStrut(12));
        hero.add(lbl("Pick a topic, answer questions, earn XP, and climb the leaderboard.", 15, Font.PLAIN, MUTED));

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        styleField(idField);
        styleField(pwField);

        // Sign-in tab
        JButton studentDemo = btn("Student demo", PANEL, CYAN);
        JButton teacherDemo = btn("Teacher demo", PANEL, ORANGE);
        studentDemo.addActionListener(e -> { idField.setText("student1"); pwField.setText("1234"); });
        teacherDemo.addActionListener(e -> { idField.setText("teacher1"); pwField.setText("1234"); });

        JPanel demoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        demoRow.setOpaque(false);
        demoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        demoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoRow.add(studentDemo);
        demoRow.add(teacherDemo);

        JButton loginBtn = btn("Sign in", CYAN, BG);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        JPanel signInTab = new JPanel();
        signInTab.setLayout(new BoxLayout(signInTab, BoxLayout.Y_AXIS));
        signInTab.setOpaque(false);
        signInTab.setBorder(BorderFactory.createEmptyBorder(12, 4, 6, 4));
        signInTab.add(fieldLbl("User ID"));
        signInTab.add(Box.createVerticalStrut(4));
        signInTab.add(idField);
        signInTab.add(Box.createVerticalStrut(10));
        signInTab.add(fieldLbl("Password"));
        signInTab.add(Box.createVerticalStrut(4));
        signInTab.add(pwField);
        signInTab.add(Box.createVerticalStrut(14));
        signInTab.add(demoRow);
        signInTab.add(Box.createVerticalStrut(12));
        signInTab.add(loginBtn);

        // Register tab
        JTextField regId = new JTextField(16);
        JTextField regName = new JTextField(16);
        JPasswordField regPw = new JPasswordField(16);
        JComboBox<String> regRole = new JComboBox<>(new String[]{"Student", "Teacher"});
        styleField(regId); styleField(regName); styleField(regPw); styleCombo(regRole);

        JButton regBtn = btn("Create account", ORANGE, BG);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.addActionListener(e -> {
            try {
                User u = data.register(regId.getText(), regName.getText(),
                        new String(regPw.getPassword()), (String) regRole.getSelectedItem());
                regPw.setText("");
                JOptionPane.showMessageDialog(this, "Account created! Welcome " + u.getName());
                openDashboard(u);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel regTab = new JPanel();
        regTab.setLayout(new BoxLayout(regTab, BoxLayout.Y_AXIS));
        regTab.setOpaque(false);
        regTab.setBorder(BorderFactory.createEmptyBorder(12, 4, 6, 4));
        regTab.add(fieldLbl("User ID"));
        regTab.add(Box.createVerticalStrut(3));
        regTab.add(regId);
        regTab.add(Box.createVerticalStrut(8));
        regTab.add(fieldLbl("Full name"));
        regTab.add(Box.createVerticalStrut(3));
        regTab.add(regName);
        regTab.add(Box.createVerticalStrut(8));
        regTab.add(fieldLbl("Password"));
        regTab.add(Box.createVerticalStrut(3));
        regTab.add(regPw);
        regTab.add(Box.createVerticalStrut(8));
        regTab.add(fieldLbl("Role"));
        regTab.add(Box.createVerticalStrut(3));
        regTab.add(regRole);
        regTab.add(Box.createVerticalStrut(12));
        regTab.add(regBtn);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sign in", signInTab);
        tabs.addTab("Create account", regTab);

        card.add(lbl("Welcome to LearnSmart", 22, Font.BOLD, TEXT), BorderLayout.NORTH);
        card.add(tabs, BorderLayout.CENTER);
        wrap.add(hero); wrap.add(card);
        return wrap;
    }

    private void doLogin() {
        try {
            openDashboard(data.login(idField.getText(), new String(pwField.getPassword())));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(User u) {
        if (dashboard != null) root.remove(dashboard);
        dashboard = (u instanceof Student) ? buildStudentPanel((Student) u) : buildTeacherPanel((Teacher) u);
        root.add(dashboard, "dashboard");
        root.setDash(true);
        cards.show(root, "dashboard");
    }

    private void logout() {
        data.saveToFile();
        pwField.setText("");
        root.setDash(false);
        cards.show(root, "login");
    }

    // ==================== Student Dashboard ====================

    private JPanel buildStudentPanel(Student s) {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(20, 28, 24, 28));

        JLabel welcome = lbl("", 13, Font.PLAIN, MUTED);
        JButton logoutBtn = btn("Log out", PANEL, TEXT);
        logoutBtn.addActionListener(e -> logout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JPanel hLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        hLeft.setOpaque(false);
        hLeft.add(lbl(s.getName() + " (Student)", 20, Font.BOLD, TEXT));
        hLeft.add(welcome);
        header.add(hLeft, BorderLayout.CENTER);
        header.add(logoutBtn, BorderLayout.EAST);

        JLabel levelVal = lbl("", 24, Font.BOLD, GOLD);
        JLabel xpVal = lbl("", 24, Font.BOLD, CYAN);
        JLabel quizVal = lbl("", 24, Font.BOLD, ORANGE);
        JLabel avgVal = lbl("", 24, Font.BOLD, GOOD);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statBox("Level", levelVal, GOLD));
        stats.add(statBox("Total XP", xpVal, CYAN));
        stats.add(statBox("Quizzes taken", quizVal, ORANGE));
        stats.add(statBox("Average score", avgVal, GOOD));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);

        JComboBox<String> topicBox = new JComboBox<>();
        styleCombo(topicBox);
        DefaultTableModel progressModel = tableModel("Topic", "Score", "Percent");
        DefaultTableModel boardModel = tableModel("Rank", "Student", "Level", "XP");
        JLabel adviceLbl = lbl("", 14, Font.PLAIN, TEXT);

        // Refresh stats, topics, history, recommendation, and leaderboard
        Runnable refresh = () -> {
            welcome.setText(s.getWelcomeMessage());
            levelVal.setText(s.getLevel());
            xpVal.setText(String.valueOf(s.getPoints()));
            quizVal.setText(String.valueOf(s.getHistory().size()));
            avgVal.setText(s.getHistory().isEmpty() ? "-" : String.format("%.0f%%", s.getAveragePercent()));

            Object prev = topicBox.getSelectedItem();
            topicBox.removeAllItems();
            Set<String> topics = new LinkedHashSet<>();
            for (Question q : data.getQuestions()) topics.add(q.getTopic());
            for (String t : topics) topicBox.addItem(t);
            if (prev != null) topicBox.setSelectedItem(prev);

            progressModel.setRowCount(0);
            for (String h : s.getHistory()) {
                String[] p = h.split(",");
                int sc = Integer.parseInt(p[1]), mx = Integer.parseInt(p[2]);
                progressModel.addRow(new Object[]{p[0], sc + " / " + mx, (mx == 0 ? 0 : sc * 100 / mx) + "%"});
            }

            String weak = s.getWeakestTopic();
            adviceLbl.setText(weak == null ? "Take a quiz to get a recommendation." : "Revise \"" + weak + "\" next.");

            boardModel.setRowCount(0);
            List<Student> sorted = new ArrayList<>(data.getStudents());
            sorted.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
            int rank = 1;
            for (Student st : sorted) boardModel.addRow(new Object[]{rank++, st.getName(), st.getLevel(), st.getPoints()});
        };

        JButton startBtn = btn("Start quiz", CYAN, BG);
        startBtn.addActionListener(e -> {
            String topic = (String) topicBox.getSelectedItem();
            if (topic == null) { JOptionPane.showMessageDialog(this, "No topics available."); return; }
            List<Question> quiz = new ArrayList<>();
            for (Question q : data.getQuestions()) if (q.getTopic().equals(topic)) quiz.add(q);
            Collections.shuffle(quiz);
            takeQuiz(s, topic, quiz);
            refresh.run();
        });

        JPanel quizTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 24));
        quizTab.setBackground(PANEL);
        quizTab.add(lbl("Choose topic:", 14, Font.BOLD, TEXT));
        quizTab.add(topicBox); quizTab.add(startBtn);

        JPanel progressTab = new JPanel(new BorderLayout(0, 10));
        progressTab.setBackground(PANEL);
        progressTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        progressTab.add(new JScrollPane(styleTable(new JTable(progressModel))), BorderLayout.CENTER);
        progressTab.add(adviceLbl, BorderLayout.SOUTH);

        JPanel boardTab = new JPanel(new BorderLayout());
        boardTab.setBackground(PANEL);
        boardTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        boardTab.add(new JScrollPane(styleTable(new JTable(boardModel))), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Take quiz", quizTab);
        tabs.addTab("My progress", progressTab);
        tabs.addTab("Leaderboard", boardTab);

        main.add(top, BorderLayout.NORTH); main.add(tabs, BorderLayout.CENTER);
        refresh.run();
        return main;
    }

    private void takeQuiz(Student s, String topic, List<Question> quiz) {
        List<List<JRadioButton>> buttons = new ArrayList<>();
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG);

        for (int i = 0; i < quiz.size(); i++) {
            Question q = quiz.get(i);
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBackground(PANEL);
            box.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            box.add(lbl((i + 1) + ". " + q.getText() + " (" + q.getPoints() + " pts)", 14, Font.BOLD, TEXT));

            ButtonGroup bg = new ButtonGroup();
            List<JRadioButton> row = new ArrayList<>();
            for (String choice : q.getChoices()) {
                JRadioButton rb = new JRadioButton(choice);
                rb.setFont(font(Font.PLAIN, 13));
                rb.setBackground(PANEL); rb.setForeground(TEXT);
                bg.add(rb); row.add(rb); box.add(rb);
            }
            buttons.add(row);
            list.add(box); list.add(Box.createVerticalStrut(10));
        }

        if (JOptionPane.showConfirmDialog(this, new JScrollPane(list),
                "Quiz: " + topic, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        int earned = 0, max = 0;
        StringBuilder review = new StringBuilder();
        for (int i = 0; i < quiz.size(); i++) {
            Question q = quiz.get(i);
            max += q.getPoints();
            String picked = "";
            for (JRadioButton rb : buttons.get(i)) if (rb.isSelected()) picked = rb.getText();
            boolean ok = q.isCorrect(picked);
            if (ok) earned += q.getPoints();
            review.append(ok ? "[OK] " : "[X] ").append(q.getText())
                  .append(" -> Correct: ").append(q.getCorrectAnswer()).append("\n");
        }

        int xp = earned * 10 + (earned == max ? 50 : 0);
        s.addResult(topic, earned, max, xp);
        data.saveToFile();
        JOptionPane.showMessageDialog(this, "Score: " + earned + " / " + max + " (+" + xp + " XP)\n\n" + review);
    }

    // ==================== Teacher Dashboard ====================

    private JPanel buildTeacherPanel(Teacher t) {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(20, 28, 24, 28));

        JButton logoutBtn = btn("Log out", PANEL, TEXT);
        logoutBtn.addActionListener(e -> logout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        header.add(lbl(t.getWelcomeMessage(), 18, Font.BOLD, TEXT), BorderLayout.CENTER);
        header.add(logoutBtn, BorderLayout.EAST);

        DefaultListModel<Question> qModel = new DefaultListModel<>();
        DefaultTableModel stModel = tableModel("Student", "Level", "XP", "Quizzes", "Average");

        Runnable refresh = () -> {
            qModel.clear();
            for (Question q : data.getQuestions()) qModel.addElement(q);
            stModel.setRowCount(0);
            for (Student st : data.getStudents()) {
                stModel.addRow(new Object[]{st.getName(), st.getLevel(), st.getPoints(),
                        st.getHistory().size(), String.format("%.0f%%", st.getAveragePercent())});
            }
        };

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"MCQ", "True/False"});
        JTextField topicIn = new JTextField(16), textIn = new JTextField(24), ptsIn = new JTextField("10", 6);
        JTextField o1 = new JTextField(14), o2 = new JTextField(14), o3 = new JTextField(14), o4 = new JTextField(14);
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
        JComboBox<String> tfBox = new JComboBox<>(new String[]{"True", "False"});
        for (JTextField f : new JTextField[]{topicIn, textIn, ptsIn, o1, o2, o3, o4}) styleField(f);
        for (JComboBox<?> cb : new JComboBox<?>[]{typeBox, correctBox, tfBox}) styleCombo(cb);

        JButton addBtn = btn("Add question", CYAN, BG);
        addBtn.addActionListener(e -> {
            try {
                String topic = topicIn.getText().trim(), qText = textIn.getText().trim();
                int pts = Integer.parseInt(ptsIn.getText().trim());
                if (topic.isEmpty() || qText.isEmpty() || pts <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter valid topic, question, and points.");
                    return;
                }
                if ("True/False".equals(typeBox.getSelectedItem())) {
                    data.addQuestion(new TrueFalseQuestion(qText, topic, pts, "True".equals(tfBox.getSelectedItem())));
                } else {
                    String[] opts = {o1.getText().trim(), o2.getText().trim(), o3.getText().trim(), o4.getText().trim()};
                    for (String o : opts) if (o.isEmpty()) { JOptionPane.showMessageDialog(this, "Fill all 4 options."); return; }
                    data.addQuestion(new MultipleChoiceQuestion(qText, topic, pts, opts, correctBox.getSelectedIndex()));
                }
                textIn.setText(""); o1.setText(""); o2.setText(""); o3.setText(""); o4.setText("");
                refresh.run();
                JOptionPane.showMessageDialog(this, "Question added and saved.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Points must be a number.");
            }
        });

        JPanel addTab = new JPanel(new GridLayout(0, 2, 10, 8));
        addTab.setBackground(PANEL);
        addTab.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        addTab.add(lbl("Type:", 13, Font.BOLD, MUTED)); addTab.add(typeBox);
        addTab.add(lbl("Topic:", 13, Font.BOLD, MUTED)); addTab.add(topicIn);
        addTab.add(lbl("Question:", 13, Font.BOLD, MUTED)); addTab.add(textIn);
        addTab.add(lbl("Points:", 13, Font.BOLD, MUTED)); addTab.add(ptsIn);
        addTab.add(lbl("Option 1:", 13, Font.BOLD, MUTED)); addTab.add(o1);
        addTab.add(lbl("Option 2:", 13, Font.BOLD, MUTED)); addTab.add(o2);
        addTab.add(lbl("Option 3:", 13, Font.BOLD, MUTED)); addTab.add(o3);
        addTab.add(lbl("Option 4:", 13, Font.BOLD, MUTED)); addTab.add(o4);
        addTab.add(lbl("Correct MCQ:", 13, Font.BOLD, MUTED)); addTab.add(correctBox);
        addTab.add(lbl("Correct T/F:", 13, Font.BOLD, MUTED)); addTab.add(tfBox);
        addTab.add(new JLabel()); addTab.add(addBtn);

        JList<Question> qList = new JList<>(qModel);
        qList.setFont(font(Font.PLAIN, 14));
        qList.setBackground(FIELD); qList.setForeground(TEXT);
        JButton delBtn = btn("Delete selected", ORANGE, BG);
        delBtn.addActionListener(e -> {
            Question sel = qList.getSelectedValue();
            if (sel != null) { data.removeQuestion(sel); refresh.run(); }
        });

        JPanel bankTab = new JPanel(new BorderLayout(0, 10));
        bankTab.setBackground(PANEL);
        bankTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        bankTab.add(new JScrollPane(qList), BorderLayout.CENTER);
        bankTab.add(delBtn, BorderLayout.SOUTH);

        JPanel studentsTab = new JPanel(new BorderLayout());
        studentsTab.setBackground(PANEL);
        studentsTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        studentsTab.add(new JScrollPane(styleTable(new JTable(stModel))), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Add question", addTab);
        tabs.addTab("Question bank", bankTab);
        tabs.addTab("Students", studentsTab);

        main.add(header, BorderLayout.NORTH); main.add(tabs, BorderLayout.CENTER);
        refresh.run();
        return main;
    }

    // ==================== Helpers ====================

    private static String pickFamily() {
        String[] prefs = {"Segoe UI", "Inter", "SF Pro Text", "Roboto", "Liberation Sans", "SansSerif"};
        Set<String> avail = new HashSet<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String p : prefs) if (avail.contains(p)) return p;
        return Font.SANS_SERIF;
    }

    private static Font font(int style, int size) { return new Font(FAMILY, style, size); }

    private static BufferedImage loadBackground() {
        String[] paths = {"resources/background.png", "resources/background.jpg", "resources/background.jpeg",
                "resource/background.png", "resource/background.jpg", "background.png", "background.jpg"};
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try { return ImageIO.read(f); } catch (IOException ignored) {}
            }
            URL u = MainFrame.class.getResource("/" + p);
            if (u != null) {
                try { return ImageIO.read(u); } catch (IOException ignored) {}
            }
        }
        return null;
    }

    private static class BgPanel extends JPanel {
        private final BufferedImage img;
        private boolean dash;
        BgPanel(BufferedImage img) { this.img = img; setOpaque(true); }
        void setDash(boolean d) { dash = d; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, BG, w, h, PANEL));
            g2.fillRect(0, 0, w, h);
            if (img != null) {
                double s = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                int dw = (int) Math.ceil(img.getWidth() * s), dh = (int) Math.ceil(img.getHeight() * s);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, dash ? 0.12f : 0.28f));
                g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                g2.setComposite(AlphaComposite.SrcOver);
            }
            g2.dispose();
        }
    }

    private static JLabel lbl(String text, int size, int style, Color c) {
        JLabel l = new JLabel(text); l.setFont(font(style, size)); l.setForeground(c); return l;
    }

    private static JLabel fieldLbl(String text) {
        JLabel l = lbl(text, 12, Font.BOLD, MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text); b.setBackground(bg); b.setForeground(fg);
        b.setFont(font(Font.BOLD, 13)); b.setFocusPainted(false); return b;
    }

    private static void styleField(JTextField f) {
        f.setBackground(FIELD); f.setForeground(TEXT); f.setCaretColor(CYAN);
        f.setFont(font(Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(FIELD); cb.setForeground(TEXT);
        cb.setFont(font(Font.PLAIN, 14));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private static JPanel statBox(String title, JLabel val, Color accent) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        p.add(lbl(title, 12, Font.BOLD, MUTED)); p.add(val);
        return p;
    }

    private static DefaultTableModel tableModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static JTable styleTable(JTable t) {
        t.setFont(font(Font.PLAIN, 13)); t.setBackground(FIELD); t.setForeground(TEXT); t.setRowHeight(28);
        t.getTableHeader().setFont(font(Font.BOLD, 12));
        t.getTableHeader().setBackground(PANEL); t.getTableHeader().setForeground(CYAN);
        return t;
    }
}