package learnsmart.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A learner: collects XP points, has a level and a quiz history. */
public class Student extends User {
    private int points;
    // each entry looks like "topic,score,max"
    private final List<String> history = new ArrayList<>();

    public Student(String id, String name, String passwordHash, int points, String historyText) {
        super(id, name, passwordHash);
        this.points = points;
        for (String h : historyText.split(";")) if (!h.isBlank()) history.add(h);
    }

    public int getPoints() { return points; }
    public List<String> getHistory() { return history; }

    /** Saves one quiz attempt and adds the earned XP. */
    public void addResult(String topic, int score, int max, int xp) {
        history.add(topic + "," + score + "," + max);
        points += xp;
    }

    /** Decision-making: the level depends on the XP. */
    public String getLevel() {
        if (points < 100) return "Beginner";
        else if (points < 300) return "Learner";
        else if (points < 600) return "Scholar";
        return "Master";
    }

    /** Average percentage of all quizzes (0 if none). */
    public double getAveragePercent() {
        if (history.isEmpty()) return 0;
        double total = 0;
        for (String h : history) {
            String[] p = h.split(",");
            int max = Integer.parseInt(p[2]);
            total += max == 0 ? 0 : Integer.parseInt(p[1]) * 100.0 / max;
        }
        return total / history.size();
    }

    /** The topic with the lowest total percentage (null if the student never lost points). */
    public String getWeakestTopic() {
        Map<String, int[]> totals = new HashMap<>();
        for (String h : history) {
            String[] p = h.split(",");
            int[] t = totals.computeIfAbsent(p[0], k -> new int[2]);
            t[0] += Integer.parseInt(p[1]);
            t[1] += Integer.parseInt(p[2]);
        }
        String weakest = null;
        double lowest = 100;
        for (Map.Entry<String, int[]> e : totals.entrySet()) {
            double pct = e.getValue()[1] == 0 ? 100 : e.getValue()[0] * 100.0 / e.getValue()[1];
            if (pct < lowest) { lowest = pct; weakest = e.getKey(); }
        }
        return weakest;
    }

    @Override public String getRole() { return "STUDENT"; }

    @Override
    public String getWelcomeMessage() {
        return "Welcome " + getName() + "!   Level: " + getLevel() + "   |   XP: " + points;
    }

    @Override
    public String toRecord() { return baseRecord() + "~" + points + "~" + String.join(";", history); }
}
