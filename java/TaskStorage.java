import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TaskStorage {
    // Since it's marked as Resource Root, we don't need "Resource/"
    private static final String FILE_NAME = System.getProperty("user.dir") + "/Resource/Main_taskManager.resources/tasks.csv";
    //one place to store and change name of file
    //to make easier to read on csv file
    private static final String HEADER =
            "id,taskType,title,subject,description,difficulty," + "submissionDate,completed,createdAt,extra1,extra2";
    public static void saveTasks(ArrayList<Task> tasks) {
        try {
            File file = new File(FILE_NAME).getAbsoluteFile();

            // 2. This creates the 'resources' subfolder if it doesn't exist
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            FileWriter   fw = new FileWriter(FILE_NAME);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(HEADER);
            bw.newLine();

            // buffered writer; builds csv file
            for (Task t : tasks) {
                String line = BuildCsvLine(t);
                bw.write(line);
                bw.newLine();
            }
            bw.close();

        }
        catch (Exception e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    public static ArrayList<Task> loadTasks() {
        ArrayList<Task> tasks = new ArrayList<Task>();

        // If the file doesn't exist yet, return an empty list
        File file = new File(FILE_NAME).getAbsoluteFile();
        if (!file.exists()) { //part of IO library that checks if the file even exists
            return tasks; //since task is empty right now, will return nothing
        }

        try {
            FileReader   fr = new FileReader(FILE_NAME); //reading file
            BufferedReader br = new BufferedReader(fr); //creates input buffer default sized
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {

                //skips first row (header row)
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                //skip empty lines and removes extra spaces
                if (line.trim().isEmpty()) {
                    continue;
                }

                //turns string back to task, method of task storage
                //static method call therefore no object reference
                Task t = buildTaskFromLine(line);
                if (t != null) { //if returns something then add task to created before to arraylist
                    tasks.add(t);
                }
            }
            br.close();
        }

        catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }

        return tasks;
    }

    //builds storage task line
    private static String BuildCsvLine(Task t) {

        //stores extra fields depending on task
        String extra1 = "";
        String extra2 = "";

        switch (t.getTaskType()) {
            case "Assignment": {
                Assignment a = new Assignment(t);
                extra1 = String.valueOf(a.getTotalMarks()); //assignment extra is total marks
                //return int value of string since stored as string
                extra2 = a.getSubmitFormat(); //submit format, method of assignment
                break;
            }
            case "Quiz": {
                Quiz q = new Quiz (t);
                extra1 = String.valueOf(q.getDurMin()); //duration string representation of int
                extra2 = q.getType();//
                break;
            }
            case "Project": {
                Project p = new Project (t);
                extra1 = String.valueOf(p.getTeamSize());
                extra2 = p.getPresentationRequired();
                break;
            }
            case "Lab Report": {
                LabReport lr = new LabReport (t);
                extra1 = lr.getLabNumber();
                extra2 = lr.getExperiment();
            }
        }

        // Build the line by joining all values with commas
        // We wrap each value in quotes so commas inside values don't break CSV
        String line = quote(t.getId())                                    + ","
                    + quote(t.getTaskType())                               + ","
                    + quote(t.getTitle())                                  + ","
                    + quote(t.getSubject())                                + ","
                    + quote(t.getDescription())                            + ","
                    + quote(t.getDifficulty())                             + ","
                    + quote(t.getSubmissionDate().format(Task.SAVE_FORMAT))+ ","
                    + quote(String.valueOf(t.getCompleted()))               + ","
                    + quote(t.getCreatedAt().format(Task.SAVE_FORMAT))     + ","
                    + quote(extra1)                                        + ","
                    + quote(extra2);

        return line;
    }
    //this above converts task to line 

    //builds task from line
    private static Task buildTaskFromLine(String line) {
        try {
            //
            String[] parts = splitCSV(line);

            if (parts.length < 11) return null;

            String id           = parts[0];
            String taskType     = parts[1];
            String title        = parts[2];
            String subject      = parts[3];
            String description  = parts[4];
            String difficulty   = parts[5];
            String subDateStr   = parts[6];
            String completedStr = parts[7];
            String createdStr   = parts[8];
            String extra1       = parts[9];
            String extra2       = parts[10];

            LocalDateTime subDate = LocalDateTime.parse(subDateStr, Task.SAVE_FORMAT);
            LocalDateTime created = LocalDateTime.parse(createdStr, Task.SAVE_FORMAT);

            Task task = null;

            // Create the right subclass based on the taskType column
            if (taskType.equals("Assignment")) {
                int marks = Integer.parseInt(extra1);
                task = new Assignment(id, title, subject, description, difficulty, subDate, marks, extra2);

            }
            else if (taskType.equals("Quiz")) {
                int duration = Integer.parseInt(extra1);
                task = new Quiz(id, title, subject, description, difficulty, subDate, duration, extra2);

            }
            else if (taskType.equals("Project")) {
                int teamSize = Integer.parseInt(extra1);
                task = new Project(id, title, subject, description, difficulty, subDate, teamSize, extra2);

            }
            else if (taskType.equals("Lab Report")) {
                task = new LabReport(id, title, subject, description, difficulty, subDate, extra1, extra2);
            }

            if (task != null) {
                task.setCompleted(completedStr.equals("true"));
                task.setCreatedAt(created);
            }

            return task;

        } catch (Exception e) {
            System.out.println("Skipping bad line: " + e.getMessage());
            return null;
        }
    }

    // ── Wrap a value in double quotes ─────────────────────────────────────────
    // This prevents commas inside a value from breaking the CSV format
    private static String quote(String value) {
        if (value == null) value = "";
        // If the value has a quote inside it, escape it by doubling it
        value = value.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }

    //splits file
    private static String[] splitCSV(String line) {
        ArrayList<String> parts = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        //can get any type, replacement to buffer
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i); //checks character at every place

            if (insideQuotes) {
                if (c == '"') {
                    // Check if the next character is also a quote (escaped quote)
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip the second quote
                    }
                    else {
                        insideQuotes = false; // end of quoted section
                    }
                } else {
                    current.append(c);
                }
            }
            else { //will run first always
                if (c == '"') { //since everything is in
                    insideQuotes = true;
                }
                else if (c == ',') {
                    parts.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }

        parts.add(current.toString()); // add the last field

        return parts.toArray(new String[0]);
    }
}
