package learnsmart.model;

/** True / False question. */
public class TrueFalseQuestion extends Question {
    private final boolean correct;

    public TrueFalseQuestion(String text, String topic, int points, boolean correct) {
        super(text, topic, points);
        this.correct = correct;
    }

    @Override public String getType() { return "TF"; }
    @Override public String[] getChoices() { return new String[]{"True", "False"}; }
    @Override public String getCorrectAnswer() { return correct ? "True" : "False"; }

    @Override
    public boolean isCorrect(String answer) { return getCorrectAnswer().equalsIgnoreCase(answer); }

    @Override
    protected String getExtra() { return String.valueOf(correct); }
}
