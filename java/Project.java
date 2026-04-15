import java.time.LocalDateTime;

public class Project extends Task { //extends the abstract class

    private int teamSize; //extra of Project
    private String presentationRequired;

    public Project(Task t){
        this.id= t.getId();
        this.title= t.getTitle();
        this.subject= t.getSubject();
        this.description= t.getDescription();
        this.difficulty= t.getDifficulty();
        this.taskType= t.getTaskType();
        this.submissionDate = t.getSubmissionDate();
        this.completed= false;  //default completed is false
        this.createdAt= LocalDateTime.now();
        this.teamSize = teamSize;
        this.presentationRequired = presentationRequired;
    }

    Project(String id, String title, String subject, String description, String difficulty,
                   LocalDateTime submissionDate, int teamSize, String presentationRequired) {
        super(id, title, subject, description, difficulty, "Project", submissionDate);
        //calls task constructor
        this.teamSize = teamSize;
        this.presentationRequired = presentationRequired;
    }

    public String getExtraInfo() {
        return "Team Size: " + teamSize + "  |  Presentation: " + presentationRequired;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int    getTeamSize()             { return teamSize; }
    public String getPresentationRequired() { return presentationRequired; }
}
