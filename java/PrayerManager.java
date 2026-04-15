import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import java.util.prefs.Preferences;

public class PrayerManager {
    private final String[] pNames = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private final String[] pTimes = new String[5];
    private final JCheckBox[] checkboxes = new JCheckBox[5];
    private int checkedCount = 0;
    private final JFrame frame;
    private final JPanel panel;
    private final JLabel[] times = new JLabel[5];
    private final JLabel status;
    private String lastPlayedTime = "";
    private Font bluewinter;
    Preferences prefs = Preferences.userNodeForPackage(PrayerManager.class);

    public PrayerManager() {
        // LOAD FONT
        try {
            bluewinter = Font.createFont(Font.TRUETYPE_FONT,
                    new File("Resources- PP/bluewinter.ttf")).deriveFont(20f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(bluewinter);
        } catch (Exception ex) {
            bluewinter = new Font("Arial", Font.PLAIN, 20);
        }

        // FRAME
        frame = new JFrame("Prayer Manager");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // PANEL
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 235, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // TITLE
        JLabel title = new JLabel("🌙 Prayer Times");
        title.setFont(bluewinter.deriveFont(36f));
        title.setForeground(new Color(100, 60, 140));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Prayer score label
        JLabel scoreLabel = new JLabel("Prayers today: 0 / 5");
        scoreLabel.setFont(bluewinter.deriveFont(16f));
        scoreLabel.setForeground(new Color(100, 60, 140));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Prayer rows, having name,time and checkbox
        for (int i = 0; i < pNames.length; i++) {
            final int index = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            row.setBackground(new Color(245, 235, 255));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            // prayers' names
            JLabel nameLabel = new JLabel(pNames[i]);
            nameLabel.setFont(bluewinter.deriveFont(22f));
            nameLabel.setForeground(new Color(80, 50, 120));
            nameLabel.setPreferredSize(new Dimension(100, 30));

            // prayer times for each prayer...
            times[i] = new JLabel("loading...");
            times[i].setFont(bluewinter.deriveFont(22f));
            times[i].setForeground(new Color(140, 90, 180));

            // checkboxes for each prayer...
            checkboxes[i] = new JCheckBox("✓ Prayed");
            checkboxes[i].setFont(bluewinter.deriveFont(14f));
            checkboxes[i].setBackground(new Color(245, 235, 255));
            checkboxes[i].setForeground(new Color(100, 60, 140));
            checkboxes[i].addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    checkedCount++;
                    nameLabel.setText("<html><strike>" + pNames[index] + "</strike></html>");
                } else {
                    checkedCount--;
                    nameLabel.setText(pNames[index]);
                }
                // the prayer score is being updated with every click
                scoreLabel.setText("Prayers today: " + checkedCount + " / 5");
            });

            row.add(nameLabel);
            row.add(times[i]);
            row.add(checkboxes[i]);
            panel.add(row);
        }

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));


        // STATUS LABEL
        status = new JLabel("Fetching prayer times...");
        status.setFont(bluewinter.deriveFont(16f));
        status.setForeground(new Color(120, 80, 160));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(status);

        frame.add(panel);
        frame.setVisible(true);

        fetchpTimes();
        startPrayerChecker();
        startMidnightReset(); //
    }

    // resets all checkboxes at midnight (new day commences)
    private void startMidnightReset() {
        Timer midnightChecker = new Timer(60000, e -> {
            LocalTime now = LocalTime.now();
            if (now.getHour() == 0 && now.getMinute() == 0) {
                resetChecklist(); // all prayers unchecked at midnight (for the new day)
            }
        });
        midnightChecker.start();
    }

    // unchecks everything and resets count
    private void resetChecklist() { // used at midnight
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < checkboxes.length; i++) {
                checkboxes[i].setSelected(false);
            }
            checkedCount = 0;
        });
    }

    // for the fox to evaluate you
    public int getPrayerCount() {
        return checkedCount; // this count affetcs the friendship bar of fox
    }

    private void fetchpTimes() {
        new Thread(() -> {
            try {
                String city = prefs.get("city", null);
                String country = prefs.get("country", null);
                if (city == null) {
                    city = JOptionPane.showInputDialog(null, "Enter your city for Prayer Times:",
                            "Location Setup", JOptionPane.QUESTION_MESSAGE);
                    country = JOptionPane.showInputDialog(null, "Enter your country:",
                            "Location Setup", JOptionPane.QUESTION_MESSAGE);
                    prefs.put("city", city);
                    prefs.put("country", country);
                }

                URL url = new URL("http://api.aladhan.com/v1/timingsByCity" +
                        "?city=" + city + "&country=" + country + "&method=1");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();
                for (int i = 0; i < pNames.length; i++) {
                    int index = json.indexOf("\"" + pNames[i] + "\":\"")
                            + pNames[i].length() + 4;
                    pTimes[i] = json.substring(index, index + 5);
                }

                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < pNames.length; i++) {
                        times[i].setText(pTimes[i]);
                    }
                    status.setText("Times loaded! Next prayer check active ✓");
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        status.setText("Failed to load times: " + ex.getMessage()));
            }
        }).start();
    }
    // visual notification alert at specific prayer time
    private void startPrayerChecker() {
        Timer checker = new Timer(10000, e -> {
            String currentTime = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            for (int i = 0; i < pNames.length; i++) {
                if (pTimes[i] != null && pTimes[i].trim().equals(currentTime) && !currentTime.equals(lastPlayedTime)) {
                    final String prayerName = pNames[i];
                    playPrayerAlert();
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "It's time for " + prayerName + " prayer! 🌙",
                                    "Prayer Reminder",
                                    JOptionPane.INFORMATION_MESSAGE));
                }
            }
        });
        checker.start();
    }
    // audio notification alert at specific prayer time
    private void playPrayerAlert() {
        try {
            File soundFile = new File("Resources- PP/bell.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PrayerManager();
    }
}