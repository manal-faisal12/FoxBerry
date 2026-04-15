import java.time.LocalDateTime;

public class LabReport extends Task {

    private String labNumber;
    private String experiment;

    public LabReport(Task t){
        this.id= t.getId();
        this.title= t.getTitle();
        this.subject= t.getSubject();
        this.description= t.getDescription();
        this.difficulty= t.getDifficulty();
        this.taskType= t.getTaskType();
        this.submissionDate = t.getSubmissionDate();
        this.completed= false;  //default completed is false
        this.createdAt= LocalDateTime.now();
        this.labNumber   = labNumber;
        this.experiment = experiment;
    }

    public LabReport(String id, String title, String subject,String description, String difficulty,
                     LocalDateTime submissionDate,String labNumber, String experiment) {

        super(id, title, subject, description, difficulty, "Lab Report", submissionDate);

        this.labNumber  = labNumber;
        this.experiment = experiment;
    }

    public String getExtraInfo() {
        return "Lab #" + labNumber + "  |  Experiment # " + experiment;
    }

    public String getLabNumber()  {
        return labNumber;
    }
    public String getExperiment() {
        return experiment;
    }
}
