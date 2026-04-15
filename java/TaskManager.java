import java.util.ArrayList;
public class TaskManager {
    private ArrayList<Task> tasks;
    private static final int ALERT_DAYS = 1;

    public TaskManager() {
        tasks = new ArrayList<Task>();
    }

    public void load() {
        tasks = TaskStorage.loadTasks();
    }

    public void save() {
        TaskStorage.saveTasks(tasks);
    }

    public void addTask(Task t) {
        tasks.add(t);
        save();
    }

    void removeTask(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                tasks.remove(i);
                break; // stop looping once found
            }
        }
        save();
    }
    Task findById(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    void toggleCompletion(String id) {
        Task t = findById(id);
        if (t != null) {
            t.setCompleted(!t.getCompleted());
            save();
        }
    }
    ArrayList<Task> getAllTasks() {
        return tasks;
    }

    int countCompleted() {
        int count = 0;
        for (Task task : tasks) {
            if (task.getCompleted()) {
                count++;
            }
        }
        return count;
    }

    int countPending() {
        int count = 0;
        for (Task task : tasks) {
            if (!task.getCompleted()) {
                count++;
            }
        }
        return count;
    }
    int countOverdue() {
        int count = 0;
        for (Task task : tasks) {
            if (task.isOverdue()) {
                count++;
            }
        }
        return count;
    }

    ArrayList<Task> getAlertTasks() {
        ArrayList<Task> alertList = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.isDueSoon(ALERT_DAYS)) {
                alertList.add(task);
            }
        }
        return alertList;
    }

    ArrayList<Task> filterByType(String type) {
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTaskType().equals(type)) {
                result.add(task);
            }
        }
        return result;
    }

    ArrayList<Task> filterCompleted() { //completed tasks
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getCompleted()) {
                result.add(task);
            }
        }
        return result;
    }

    ArrayList<Task> filterOverdue() { //overdue results
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.isOverdue()) {
                result.add(task);
            }
        }
        return result;
    }
//this part needs cropping cutting since no need for id
    String generateId() {
        // Uses the current time in milliseconds — guaranteed to be unique
        return "T" + System.currentTimeMillis();
    }
    int totalTasks() {
        return tasks.size();
    }
}
