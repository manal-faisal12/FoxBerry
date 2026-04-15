import java.time.LocalDateTime;

/*
   Quiz.java
   ---------
   Extends Task — adds two fields specific to quizzes:
   - durationMinutes : how long the quiz lasts
   - quizType        : e.g. "MCQ", "Short Answer", "Open Book"

   OOP used: Inheritance
*/

public class Quiz extends Task {

    private int DurMin;
    private String type;

    public Quiz(Task t){
        this.id= t.getId();
        this.title= t.getTitle();
        this.subject= t.getSubject();
        this.description= t.getDescription();
        this.difficulty= t.getDifficulty();
        this.taskType= t.getTaskType();
        this.submissionDate = t.getSubmissionDate();
        this.completed= false;  //default completed is false
        this.createdAt= LocalDateTime.now();
        this.DurMin = DurMin;
        this.type = type;
    }

    public Quiz(String id, String title, String subject,String description, String difficulty,
                LocalDateTime submissionDate, int DurMin, String type) {

        super(id, title, subject, description, difficulty, "Quiz", submissionDate);

        this.DurMin = DurMin;
        this.type = type;
    }

    public String getExtraInfo() {
        return "Duration: " + DurMin + " min  |  Type: " + type;
    }

    public int getDurMin() {
        return DurMin;
    }
    public String getType(){
        return type;
    }
}
