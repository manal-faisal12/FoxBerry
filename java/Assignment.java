import java.time.LocalDateTime;

public class Assignment extends Task {

    private int    totalMarks;
    private String submitFormat;

    public Assignment(Task t){
        this.id= t.getId();
        this.title= t.getTitle();
        this.subject= t.getSubject();
        this.description= t.getDescription();
        this.difficulty= t.getDifficulty();
        this.taskType= t.getTaskType();
        this.submissionDate = t.getSubmissionDate();
        this.completed= false;  //default completed is false
        this.createdAt= LocalDateTime.now();
        this.totalMarks   = totalMarks;
        this.submitFormat = submitFormat;
    }

    public Assignment(String id, String title, String subject,String description, String difficulty,
                      LocalDateTime submissionDate, int totalMarks, String submitFormat) {

        super(id, title, subject, description, difficulty, "Assignment", submissionDate);

        this.totalMarks   = totalMarks;
        this.submitFormat = submitFormat;
    }


    public String getExtraInfo() {
        return "Marks: " + totalMarks + "  |  Format: " + submitFormat;
    }

    public int    getTotalMarks()   { return totalMarks; }
    public String getSubmitFormat() { return submitFormat; }

}
