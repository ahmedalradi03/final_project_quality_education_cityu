package learnsmart.model;

/** Abstract quiz question. Each subclass decides how an answer is checked (polymorphism). */
public abstract class Question {
    private String text;
    private String topic;
    private int points;

    public Question(String text, String topic, int points) {
        this.text = text;
        this.topic = topic;
        this.points = points;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public abstract String getType();            // "MCQ" or "TF"
    public abstract String[] getChoices();       // answers shown to the student
    public abstract String getCorrectAnswer();   // used for the review screen
    public abstract boolean isCorrect(String answer);
    protected abstract String getExtra();        // subclass data stored in the file

    public String toRecord() {
        return "QUESTION~" + getType() + "~" + topic + "~" + points + "~" + text + "~" + getExtra();
    }

    /** Factory: rebuilds the right subclass when the file is loaded. */
    public static Question create(String type, String topic, int points, String text, String extra) {
        if (type.equals("MCQ")) {
            String[] o = extra.split("\\^", -1);
            return new MultipleChoiceQuestion(text, topic, points, new String[]{o[0], o[1], o[2], o[3]}, Integer.parseInt(o[4]));
        }
        return new TrueFalseQuestion(text, topic, points, Boolean.parseBoolean(extra));
    }

    @Override
    public String toString() { return "[" + getType() + "] " + topic + " - " + text + " (" + points + " pts)"; }
}
