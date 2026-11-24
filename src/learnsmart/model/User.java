package learnsmart.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Abstract base class for all users (Abstraction + Inheritance). */
public abstract class User {
    private final String id;
    private String name;
    private final String passwordHash; // passwords are stored as SHA-256 hashes, never as plain text

    public User(String id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    // ---- Encapsulation: private fields + getters/setters ----
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean checkPassword(String plain) {
        return passwordHash.equals(hash(plain));
    }

    /** Converts a plain password to a SHA-256 hex string. */
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Subclasses override these methods (polymorphism). */
    public abstract String getRole();
    public abstract String getWelcomeMessage();
    public abstract String toRecord();

    /** First four fields of every user record in the data file. */
    protected String baseRecord() {
        return getRole() + "~" + id + "~" + name + "~" + passwordHash;
    }

    @Override
    public String toString() { return name + " (" + id + ")"; }
}
