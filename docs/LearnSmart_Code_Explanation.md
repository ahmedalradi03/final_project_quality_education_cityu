# LearnSmart – Code Explanation

LearnSmart has exactly **8 classes** in 3 packages. Read them in this order.

```
src/learnsmart/
├── model/
│   ├── User.java                    abstract base class
│   ├── Student.java                 extends User
│   ├── Teacher.java                 extends User
│   ├── Question.java                abstract base class
│   ├── MultipleChoiceQuestion.java  extends Question
│   └── TrueFalseQuestion.java       extends Question
├── data/
│   └── DataManager.java             collections + business rules + file I/O
└── ui/
    └── MainFrame.java               Swing GUI + main()
```

## 1. `User` (abstract)
Private fields `id`, `name`, `passwordHash` with getters/setters (**encapsulation**). The password is saved only as a SHA-256 hash; `checkPassword()` hashes the typed password and compares. Three abstract methods (`getRole`, `getWelcomeMessage`, `toRecord`) force each subclass to provide its own version (**abstraction**).

## 2. `Student` extends `User`
Adds `points` (XP) and a `history` list (`ArrayList<String>`, each entry `topic,score,max`). Methods: `addResult()`, `getLevel()` (if / else-if decision: Beginner, Learner, Scholar, Master), `getAveragePercent()` (loop), `getWeakestTopic()` (HashMap of totals per topic, used for the recommendation).

## 3. `Teacher` extends `User`
No extra data. It overrides the abstract methods with teacher-specific text (**method overriding**).

## 4. `Question` (abstract)
Fields `text`, `topic`, `points` (private + getters/setters). Abstract methods: `getType`, `getChoices`, `getCorrectAnswer`, `isCorrect`, `getExtra`. The static **factory method** `create()` rebuilds the correct subclass when the file is loaded.

## 5. `MultipleChoiceQuestion` and 6. `TrueFalseQuestion` extend `Question`
Each one implements `isCorrect()` in its own way:
- MCQ: the answer must equal the option at `correctIndex`.
- True/False: the answer must equal "True"/"False" (ignoring case).

## 7. `DataManager`
- **Collections:** `HashMap<String, User>` (login by id) and `ArrayList<Question>`; `TreeSet` for sorted topics; streams for filtering and sorting.
- **File I/O:** `load()` and `save()` use `Files.readAllLines` / `Files.write` on `data/learnsmart.txt`; each object writes itself with `toRecord()`.
- **Rules:** `login`, `register` (validation), `addQuestion` (validation, builds the right subclass), `deleteQuestion`, `submitQuiz`, `getLeaderboard`.
- `submitQuiz()` is the core **runtime polymorphism**:
  ```java
  Question q = asked.get(i);          // superclass reference
  if (q.isCorrect(answers.get(i))) …  // the real subclass method runs
  ```
  XP = 10 × points, plus 50 bonus XP for a perfect score.
- Errors are reported with `IllegalArgumentException` and shown to the user as pop-ups.

## 8. `MainFrame`
`extends JFrame`. It contains `main()`, the login screen, the student dashboard and the teacher dashboard. A `CardLayout` switches between login and dashboard.
- **Student:** *Take Quiz* (choose topic, shuffled questions, answer review), *My Progress* (table + recommendation), *Leaderboard*.
- **Teacher:** *Add Question*, *Question Bank* (delete), *Students* (level, XP, quizzes, average).
- `openDashboard(User u)` uses `instanceof` on a `User` reference to open the right screen.

## Program flow
1. `main()` creates `MainFrame` → `DataManager` loads the file (or creates demo data).
2. Login → `DataManager.login()` → dashboard for the user type.
3. Teacher adds questions → saved immediately. Student takes a quiz → `submitQuiz()` grades, updates XP, saves.
4. Restart the program → everything is loaded from `data/learnsmart.txt`.

## Requirement checklist
| Requirement | Where |
|---|---|
| 6–8 classes | exactly 8 |
| Encapsulation | private fields + getters/setters in all model classes |
| Inheritance | `User → Student, Teacher`; `Question → MultipleChoiceQuestion, TrueFalseQuestion` (+ `MainFrame extends JFrame`) |
| Polymorphism | overriding `isCorrect`, `getChoices`, `getWelcomeMessage`…; grading through `Question` references |
| Abstraction | abstract classes `User` and `Question` |
| Collections | `ArrayList`, `HashMap`, `TreeSet`, `HashSet`-style maps, streams |
| File I/O | `DataManager.load()/save()` |
| Decisions & loops | level calculation, grading loop, validation |
| GUI & output | Swing dashboards, tables, dialogs |
