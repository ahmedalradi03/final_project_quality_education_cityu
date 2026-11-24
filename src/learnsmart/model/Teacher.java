package learnsmart.model;

/** A teacher: creates and manages quiz questions and watches student progress. */
public class Teacher extends User {

    public Teacher(String id, String name, String passwordHash) {
        super(id, name, passwordHash);
    }

    @Override public String getRole() { return "TEACHER"; }

    @Override
    public String getWelcomeMessage() {
        return "Welcome Teacher " + getName() + "!   Add questions and track your students.";
    }

    @Override public String toRecord() { return baseRecord(); }
}
