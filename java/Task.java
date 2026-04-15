import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public abstract class Task {
    String id;
    String title;
    String subject;
    String description;
    String difficulty;      // easy, medium, hard
    String taskType;        // assignment, quiz.
    LocalDateTime submissionDate;
    boolean completed;
    LocalDateTime createdAt;

    // Date format used when SHOWING the date to the user
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a");

    // Date format used when SAVING/LOADING from the CSV file
    public static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


    public Task(){} //fix since no no-arg constructor should have values
    public Task(String id, String title, String subject, String description, String difficulty,
                String taskType, LocalDateTime submissionDate) {
        this.id= id;
        this.title= title;
        this.subject= subject;
        this.description= description;
        this.difficulty= difficulty;
        this.taskType= taskType;
        this.submissionDate = submissionDate;
        this.completed= false;  //default completed is false
        this.createdAt= LocalDateTime.now();
    }

    public abstract String getExtraInfo();  //for the extra information for each specific class


    public boolean isOverdue() {
        if (this.completed)
            return false; //not completed
        else
            return LocalDateTime.now().isAfter(submissionDate);
    }

    public boolean isDueSoon(int days) {
        if (completed) {
            return false;
        }
        if (isOverdue()) {
            return false;
        }
        // if it is completed or overdue then is not due soon

        LocalDateTime now      = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);

        if (submissionDate.isBefore(deadline)) { //submission date is attribute
            // so if date has passed first and then its still not completed or overdue
            return true;//isBefore is build in checks if it is before the date
        } else {
            return false;
        }
    }

    public String getStatus() {
        if (completed)
            return " Completed ";
        if (isOverdue()) {
            long days = ChronoUnit.DAYS.between(submissionDate, LocalDateTime.now());
            return "Overdue by " + days + " days";
            //checks how many days before submission date
        }
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), submissionDate); //using long beacause between returns a long
        if (days == 0)
            return "Due Today";
        else
            return "Due in " + days + " days";
    }

    public String getId() {
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getSubject(){
        return subject;
    }
    public String getDescription(){
        return description;
    }
    public String getDifficulty(){
        return difficulty;
    }
    public String getTaskType(){
        return taskType;
    }
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    public boolean getCompleted(){
        return completed;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    //setters start from below
    public void setCompleted(boolean completed)       {
        this.completed = completed;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
}
