import javax.swing.*;
import java.util.ArrayList;

/*
   AlertService.java
   -----------------
   Checks if any tasks are due within 1 day and not yet completed.
   If there are any, it shows a popup message to the user.
*/

public class AlertService {

    private TaskManager manager;
    private JFrame      owner;

    public AlertService(JFrame owner, TaskManager manager) {
        this.owner   = owner;
        this.manager = manager;
    }
    public static int latestTaskCount = 0;
    // Called once when the app starts
        public void checkAndAlert() {
            ArrayList<Task> alertTasks = manager.getAlertTasks();
            latestTaskCount = alertTasks.size();
            if (alertTasks.size() == 0) return;

            if (FoxDesktopPet.currentFox != null) {//added by Minahil
                FoxDesktopPet.currentFox.speak(new FoxDesktopPet.FoxTaskAlert(alertTasks.size()));
            }


        // Build the message to show
        String message = "⏰  Tasks due within 1 day:\n\n";

        for (int i = 0; i < alertTasks.size(); i++) {
            Task t = alertTasks.get(i);
            message = message + t.getTitle() + "  (" + t.getSubject() + ")\n" + "    Due: " + t.getSubmissionDate().format(Task.DISPLAY_FORMAT)
                    + "\n\n";
        }

        JOptionPane.showMessageDialog(owner, message,
                "Due Soon — " + alertTasks.size() + " task(s)",
                JOptionPane.WARNING_MESSAGE);
    }
}
