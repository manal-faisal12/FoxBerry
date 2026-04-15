import javax.swing.*;
public class MainTaskManager {

    public static void main(String[] args) {

        // Make text look smoother on screen
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Try to use Nimbus look & feel (cleaner style than default Java)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, Java uses its default look
        }

        // Swing apps must start on the Event Dispatch Thread
        // SwingUtilities.invokeLater makes sure of that
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TaskManager manager = new TaskManager();
                manager.load();         // load tasks from tasks.csv
                new Dashboard(manager); // open the main window
            }
        });
    }
}
