import javax.swing.*;
import javax.swing.Timer;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;// added these to assist in locating the resources successfully
import java.net.URL;
public class Pomodoro extends JFrame {
    public boolean isbreak;//so that the fox can view this
    private CardLayout cardLayout = new CardLayout(); // used to switch b/w 2 panels
    private JPanel container = new JPanel(cardLayout);  // holds the cards
    private PomodoroPanel pomodoroPanel;
    public Pomodoro() {

        setTitle("FOCUS MANAGER");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        //  Change the TASKBAR ICON
        try {
            URL imgURL = getClass().getClassLoader().getResource("Resources- PP/strawberry.png");
            ImageIcon img = new ImageIcon(imgURL);
            setIconImage(img.getImage());
        } catch (Exception e) {
            System.out.println("Icon could not be loaded: " + e.getMessage());
        }

        // panels
        this.pomodoroPanel = new PomodoroPanel(this);
        AmbientPanel ambient = new AmbientPanel(this);

        container.add(pomodoroPanel, "POMODORO");
        container.add(ambient, "AMBIENT PLAYER");

        add(container);
        setVisible(true);


    }
    public boolean isCurrentlyOnBreak() {
        return pomodoroPanel.isbreak;
    }
    public void switchPanel(String name) { // there are two panels, pomodoro and ambience
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pomodoro());

    }

}

//  POMODORO panel
class PomodoroPanel extends JPanel {
    int secleft = 25 * 60;
    private final RoundedButton startb, restartb;
    private final JLabel label, title, strawLeft, strawRight;
    private ImageIcon scaledStrawberry, scaledCoffee;
    private DecoratedPanel panel;
    private Timer timer;
    public boolean isbreak;

    public PomodoroPanel(Pomodoro mainFrame) {
        setLayout(new BorderLayout());

        // Navigation bar on top to toggle b/w the 2 cards
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navBar.setOpaque(false);
        JButton toAudio = new JButton("🎧 Ambience");
        toAudio.addActionListener(e -> mainFrame.switchPanel("AMBIENT PLAYER"));
        navBar.add(toAudio);

        Font lexend; // load different fonts
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("Resources- PP/bluewinter.ttf");
            lexend = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(100f);
        } catch (Exception ex) {
            lexend = new Font("Arial", Font.PLAIN, 48);
        }
        URL strawURL = getClass().getClassLoader().getResource("Resources- PP/strawberry.png");
        URL coffeeURL = getClass().getClassLoader().getResource("Resources- PP/coffee.png");

         // 2. Load the base icons (checking for null so it doesn't crash)
        ImageIcon strawberryIcon = (strawURL != null) ? new ImageIcon(strawURL) : new ImageIcon();
        ImageIcon coffeeIcon = (coffeeURL != null) ? new ImageIcon(coffeeURL) : new ImageIcon();
        scaledStrawberry = new ImageIcon(strawberryIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        scaledCoffee = new ImageIcon(coffeeIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));

        label = new JLabel("25 : 00");
        label.setFont(lexend.deriveFont(90f));
        label.setForeground(Color.WHITE);

        strawLeft = new JLabel(scaledStrawberry);
        strawRight = new JLabel(scaledStrawberry);

        JPanel timerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        timerRow.setOpaque(false);
        timerRow.add(strawLeft);
        timerRow.add(label);
        timerRow.add(strawRight);

        title = new JLabel("POMODORO");
        title.setFont(lexend.deriveFont(24f));
        title.setForeground(new Color(200, 82, 80));
        title.setAlignmentX(CENTER_ALIGNMENT);

        // button panel: start and restart button
        JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonpanel.setOpaque(false);
        startb = new RoundedButton("START", new Color(200, 82, 80));
        startb.setForeground(Color.WHITE);
        startb.setFont(lexend.deriveFont(28f));
        startb.addActionListener(e -> timer.start());


        restartb = new RoundedButton("RESTART", new Color(149, 170, 140));
        restartb.setFont(lexend.deriveFont(28f));
        restartb.setForeground(Color.WHITE);
        restartb.addActionListener(e -> restartTimer()
        );

        buttonpanel.add(startb);
        buttonpanel.add(restartb);

        JPanel content = new JPanel(); // start adding all components
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(Box.createVerticalGlue());
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(timerRow);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(buttonpanel);
        content.add(Box.createVerticalGlue());

        // the image on left and right borders
        panel = new DecoratedPanel(strawberryIcon.getImage(), coffeeIcon.getImage());
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(249, 213, 211));
        panel.add(navBar, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        add(panel);

        timer = new Timer(1000, e -> {
            secleft--;
            label.setText(String.format("%02d : %02d", secleft / 60, secleft % 60));
            if (secleft == 0) {
                handleSwitch();
                playAlert(); }
        });
    }

    private void handleSwitch() { // screen change going from pomodoro to break and viceversa
        isbreak = !isbreak;

        if (isbreak) {
            secleft = 5 * 60;
            title.setText("BREAK TIME ");
            title.setForeground(new Color(139, 90, 43));
            panel.setBackground(new Color(235, 220, 200));
            strawLeft.setIcon(scaledCoffee);
            strawRight.setIcon(scaledCoffee);
            FoxDesktopPet.announce(true);

        } else {
            secleft = 25 * 60;
            title.setText("POMODORO");
            title.setForeground(new Color(200, 82, 80));
            panel.setBackground(new Color(249, 213, 211));
            strawLeft.setIcon(scaledStrawberry);
            strawRight.setIcon(scaledStrawberry);
            FoxDesktopPet.announce(false);

        }
        panel.repaint();

    }

    public void restartTimer() {
        secleft = 25 * 60;
        label.setText("25 : 00");
        timer.stop();
        isbreak = false;
        panel.setBackground(new Color(249, 213, 211));
        title.setText("POMODORO");
        title.setForeground(new Color(200, 82, 80));
        strawLeft.setIcon(scaledStrawberry);
        strawRight.setIcon(scaledStrawberry);
        panel.repaint();
    }

    private void playAlert() { // bell sound when timer ends
        try {
            URL soundURL = getClass().getResource("Resources- PP/bell.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(0);
            clip.start();

        } catch (Exception ex) {
            System.err.println("Error playing alert: " + ex.getMessage());
        }
    }

    class RoundedButton extends JButton { //custom round button
        private Color color;
        public RoundedButton(String text, Color color) {
            super(text);
            this.color = color;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(200, 60));
            setMaximumSize(new Dimension(200, 60));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class DecoratedPanel extends JPanel { // left right images
        private Image smallBerry, smallCoffee;
        public DecoratedPanel(Image img1, Image img2) {
            this.smallBerry = img1.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            this.smallCoffee = img2.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            Image toDraw = isbreak ? smallCoffee : smallBerry;
            for (int y = 10; y + 50 <= getHeight() - 10; y += 70) {
                g2.drawImage(toDraw, 10, y, null);
                g2.drawImage(toDraw, getWidth() - 60, y, null);
            }
            g2.dispose();
        }
    }
}

//  AMBIENT PANEL
class AmbientPanel extends JPanel {
    private JLabel nowPlaying;
    private Clip currentClip;
    private JSlider volumeSlider;
    private String[] soundNames = {"RAIN", "FIREPLACE", "CAFE", "FOREST", "RIVER"};
    private String[] soundFiles = {"Resources- Audio/rain.wav", "Resources- Audio/fireplace.wav", "Resources- Audio/cafe.wav", "Resources- Audio/forest.wav", "Resources- Audio/river.wav"};

    public AmbientPanel(Pomodoro mainFrame) {
        setLayout(new BorderLayout());
        setBackground(new Color(247, 241, 229));

        // Navigation bar on top (to go back to pomodoro)
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navBar.setOpaque(false);
        JButton backBtn = new JButton("←");
        backBtn.addActionListener(e -> mainFrame.switchPanel("POMODORO"));
        navBar.add(backBtn);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

        Font lora, montserrat; // lod custom fonts
        try {
            InputStream loraIS = getClass().getClassLoader().getResourceAsStream("Resources- PP/lora.ttf");
            InputStream montIS = getClass().getClassLoader().getResourceAsStream("Resources- PP/Montserrat-SemiBold.ttf");
            lora = Font.createFont(Font.TRUETYPE_FONT, loraIS).deriveFont(36f);
            montserrat = Font.createFont(Font.TRUETYPE_FONT, montIS).deriveFont(14f);
        } catch (Exception ex) {
            lora = new Font("Serif", Font.ITALIC, 36);
            montserrat = new Font("SansSerif", Font.PLAIN, 14);
        }

        JLabel title = new JLabel("Ambient Scores");
        title.setFont(lora.deriveFont(Font.ITALIC, 38f));
        title.setForeground(new Color(9, 32, 82));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("for the studious mind.");
        subtitle.setFont(montserrat.deriveFont(12f));
        subtitle.setForeground(new Color(48, 46, 46));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        nowPlaying = new JLabel("Select a track...");
        nowPlaying.setFont(montserrat.deriveFont(14f));
        nowPlaying.setForeground(new Color(28, 43, 72));
        nowPlaying.setAlignmentX(CENTER_ALIGNMENT);

        // VOLUME SLIDER
        JLabel volLabel = new JLabel("🔊 Volume");
        volLabel.setFont(montserrat.deriveFont(10f));
        volLabel.setAlignmentX(CENTER_ALIGNMENT);

        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 75); // Start at 75%
        volumeSlider.setOpaque(false);
        volumeSlider.setMaximumSize(new Dimension(300, 30));
        volumeSlider.addChangeListener(e -> adjustVolume());

        JLabel collectionLabel = new JLabel("THE COLLECTION");
        collectionLabel.setFont(montserrat.deriveFont(11f));
        collectionLabel.setForeground(new Color(9, 32, 82));
        collectionLabel.setAlignmentX(CENTER_ALIGNMENT);

        content.add(Box.createVerticalGlue()); // start adding components
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(subtitle);
        content.add(Box.createRigidArea(new Dimension(0, 14)));
        content.add(nowPlaying);
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(volumeSlider);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(collectionLabel);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        // display the saved sounds as buttons
        for (int i = 0; i < soundNames.length; i++) {
            final int index = i;
            AmbientButton btn = new AmbientButton(soundNames[i], new Color(209, 223, 236), new Color(9, 32, 82));
            btn.setAlignmentX(CENTER_ALIGNMENT);
            btn.setFont(montserrat.deriveFont(Font.BOLD, 15f));
            btn.addActionListener(e -> {
                playSound(soundFiles[index]);
                nowPlaying.setText("Now playing: The " + soundNames[index]);
            });
            content.add(btn);
            content.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        //  Add your Own Audio Button
        AmbientButton addOwn = new AmbientButton("+ Add your own", new Color(175, 199, 235), new Color(9, 32, 82));
        addOwn.setFont(lora.deriveFont(18f));
        addOwn.setAlignmentX(CENTER_ALIGNMENT);
        addOwn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (!file.getName().endsWith(".wav")) {
                    JOptionPane.showMessageDialog(this, "Only WAV files supported.\nPlease convert to WAV AT\nhttps://cloudconvert.com/mp3-to-wav", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    URL fileURL = file.toURI().toURL();//must convert your file's address to url for this java code to understand its location on your computer
                    playSound(fileURL); // to
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                nowPlaying.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                nowPlaying.setText("Now playing: " + file.getName());
            }
        });
        content.add(addOwn);

        JButton stop = new JButton("Stop"); //  button to stop track
        stop.setFont(montserrat.deriveFont(20f));
        stop.setForeground(new Color(9, 32, 82));
        stop.setAlignmentX(CENTER_ALIGNMENT);
        stop.addActionListener(e -> stopSound());
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(stop);

        // "Work conquers all" -> latin quote
        JLabel latin = new JLabel("labor omnia vincit");
        latin.setFont(lora.deriveFont(Font.ITALIC, 13f));
        latin.setForeground(new Color(48, 46, 46));
        latin.setAlignmentX(CENTER_ALIGNMENT);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(latin);
        content.add(Box.createVerticalGlue());
        add(navBar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    // play an audio (in .wav format)
    private void playSound(String path) {//for the pre-downloaded audios
        try {
            stopSound();
            // This is the URL method
            java.net.URL soundURL = getClass().getClassLoader().getResource(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);
            currentClip = AudioSystem.getClip();
            currentClip.open(audio);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentClip.start();
        } catch (Exception ex) { nowPlaying.setText("Error loading sound."); }
    }
    //-----------------------------------------------------------
    private void playSound(URL url) {//for your own audios
        try {
            stopSound();
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            currentClip = AudioSystem.getClip();
            currentClip.open(audio);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentClip.start();
        } catch (Exception ex) {
            nowPlaying.setText("Error loading sound.");
        }
    }
    // adjust volume slider
    private void adjustVolume() {
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float volume = (float) volumeSlider.getValue() / 100f;
            // Convert linear 0-1 to decibels
            float dB = (float) (Math.log(volume == 0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }
    private void stopSound() {
        if (currentClip != null) { currentClip.stop(); currentClip.close(); }
        nowPlaying.setText("Select a track...");
    }
    // custom round button
    class AmbientButton extends JButton {
        private Color bg, border;
        public AmbientButton(String text, Color bg, Color border) {
            super(text);
            this.bg = bg;
            this.border = border;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(400, 50));
            setMaximumSize(new Dimension(400, 50));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}