package learnsmart.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import learnsmart.model.*;

public class DataManager {
    private static final Path DATA_FILE = Paths.get("learnsmart_data.txt");
    private static DataManager instance;

    private final List<User> users = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final List<Teacher> teachers = new ArrayList<>();
    private final List<Question> questions = new ArrayList<>();
    private User currentUser;

    public DataManager() {
        loadFromFile();
        seedInitialData();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void seedInitialData() {
        boolean changed = false;
        if (users.isEmpty()) {
            addUserInternal(new Student("student1", "Demo Student", User.hash("1234"), 0, ""));
            addUserInternal(new Teacher("teacher1", "Demo Teacher", User.hash("1234")));
            changed = true;
        }
        if (questions.isEmpty()) {
            questions.add(new TrueFalseQuestion(
                "Java is an object-oriented programming language.",
                "Java Basics",
                10,
                true
            ));
            questions.add(new MultipleChoiceQuestion(
                "Which collection preserves insertion order?",
                "Collections",
                15,
                new String[]{"HashSet", "ArrayList", "TreeSet", "HashMap"},
                1
            ));
            changed = true;
        }
        if (changed) {
            saveToFile();
        }
    }

    private void addUserInternal(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
            if (user instanceof Student && !students.contains(user)) {
                students.add((Student) user);
            } else if (user instanceof Teacher && !teachers.contains(user)) {
                teachers.add((Teacher) user);
            }
        }
    }

    public void addUser(User user) {
        addUserInternal(user);
        saveToFile();
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public List<User> getUsers() { return Collections.unmodifiableList(users); }
    public List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public List<Teacher> getTeachers() { return Collections.unmodifiableList(teachers); }
    public List<Question> getQuestions() { return Collections.unmodifiableList(questions); }

    public User register(String id, String name, String plainPassword, String role) {
        String cleanId = id == null ? "" : id.trim();
        String cleanName = name == null ? "" : name.trim();
        String cleanPass = plainPassword == null ? "" : plainPassword;
        String cleanRole = role == null ? "Student" : role.trim();

        if (cleanId.isEmpty() || cleanName.isEmpty() || cleanPass.isEmpty()) {
            throw new IllegalArgumentException("Please fill in User ID, Password, and Full name.");
        }
        for (User existing : users) {
            if (existing.getId().equalsIgnoreCase(cleanId)) {
                throw new IllegalArgumentException("A user with ID \"" + cleanId + "\" already exists.");
            }
        }

        String hash = User.hash(cleanPass);
        User created = "Teacher".equalsIgnoreCase(cleanRole)
                ? new Teacher(cleanId, cleanName, hash)
                : new Student(cleanId, cleanName, hash, 0, "");

        addUserInternal(created);
        setCurrentUser(created);
        saveToFile();
        return created;
    }

    public User login(String userId, String plainPassword) {
        String cleanId = userId == null ? "" : userId.trim();
        String cleanPass = plainPassword == null ? "" : plainPassword;

        if (cleanId.isEmpty() || cleanPass.isEmpty()) {
            throw new IllegalArgumentException("Please enter both User ID and Password.");
        }
        for (User user : users) {
            if (cleanId.equalsIgnoreCase(user.getId()) && user.checkPassword(cleanPass)) {
                setCurrentUser(user);
                return user;
            }
        }
        throw new IllegalArgumentException("Invalid User ID or Password.");
    }

    public void addQuestion(Question q) {
        if (q != null) {
            questions.add(q);
            saveToFile();
        }
    }

    public void removeQuestion(Question q) {
        if (questions.remove(q)) {
            saveToFile();
        }
    }

    /** Saves all users, questions, and student records to a text file. */
    public synchronized void saveToFile() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.toRecord());
        }
        for (Question q : questions) {
            lines.add(q.toRecord());
        }
        try {
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    /** Reloads users, questions, and student records from the file upon startup. */
    private void loadFromFile() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                String[] p = line.split("~", -1);
                if (p[0].equals("STUDENT") && p.length >= 5) {
                    int pts = p[4].isBlank() ? 0 : Integer.parseInt(p[4]);
                    String hist = p.length >= 6 ? p[5] : "";
                    addUserInternal(new Student(p[1], p[2], p[3], pts, hist));
                } else if (p[0].equals("TEACHER") && p.length >= 4) {
                    addUserInternal(new Teacher(p[1], p[2], p[3]));
                } else if (p[0].equals("QUESTION") && p.length >= 6) {
                    int pts = Integer.parseInt(p[3]);
                    questions.add(Question.create(p[1], p[2], pts, p[4], p[5]));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e.getMessage());
        }
    }
}