package learnsmart.model;

import java.util.List;

/** Question with four options and exactly one correct option. */
public class MultipleChoiceQuestion extends Question {
    private final String[] options;
    private final int correctIndex;

    public MultipleChoiceQuestion(String text, String topic, int points, String[] options, int correctIndex) {
        super(text, topic, points);
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public MultipleChoiceQuestion(String string, List<String> of, int i) {
        this(string, "", 0, of.toArray(new String[0]), i);
    }

    @Override public String getType() { return "MCQ"; }
    @Override public String[] getChoices() { return options; }
    @Override public String getCorrectAnswer() { return options[correctIndex]; }

    @Override
    public boolean isCorrect(String answer) { return options[correctIndex].equals(answer); }

    @Override
    protected String getExtra() { return String.join("^", options) + "^" + correctIndex; }
}
