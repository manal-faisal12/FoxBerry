import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.URL;//for creating object of type
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.util.Random;//for random dialogue selection

//---------------Using enum for different states of the pet--------------
enum PetState {                                                 //Maxticks: Timer for transitioning in other states
    IDLE           (1,         14,        200),//while stopping during roam
    WALKING        (2,          8,        600),//while  roaming
    SITTING        (0,          5,         -1),
    STAYING        (5,          6,         -1),//during the STAY HERE command
    HELD           (3,          1,         -1),//when it is being picked up
    DEAD           (0,          1,         -1); // Death of the fox

    private final int spriteRow;//row of animation
    private final int maxFrames;//total number of frames of the animation
    private final int maxTicks;//timer

    PetState(int spriteRow, int maxFrames, int maxTicks) {//constructor for petstate
        this.spriteRow = spriteRow;
        this.maxFrames = maxFrames;
        this.maxTicks  = maxTicks;
    }
//---------get methods for attributes of pet state----------
    public int getSpriteRow()  { return spriteRow; }
    public int getMaxFrames()  { return maxFrames; }
    public boolean hasTimeLimit() { return maxTicks > 0; }
    public int getMaxTicks()   { return maxTicks; }
    public boolean isStationary() {
        return this == SITTING || this == STAYING || this == HELD || this == DEAD;//to stop it from moving during picking it u
    }
}

class PetContext {
    public Point   location;
    public double  dx, dy;
    public int     stateTimer;
    public boolean isFacingLeft;
    public int     batteryLevel;
    public int     windowW, windowH;

    public PetContext(Point location, double dx, double dy, int stateTimer,
                      boolean isFacingLeft, int batteryLevel, int windowW, int windowH) {
        this.location     = location;
        this.dx           = dx;
        this.dy           = dy;
        this.stateTimer   = stateTimer;
        this.isFacingLeft = isFacingLeft;
        this.batteryLevel = batteryLevel;
        this.windowW      = windowW;
        this.windowH      = windowH;
    }
}

abstract class BehaviourHandler {
    protected static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();
    public abstract PetState tick(PetContext ctx);
    protected Point clamp(Point p, int w, int h) {//for making a boundary for the random movement of the fox
        int x = Math.max(0, Math.min(p.x, SCREEN.width  - w));
        int y = Math.max(0, Math.min(p.y, SCREEN.height - h));
        return new Point(x, y);
    }
}

class IdleBehaviour extends BehaviourHandler {
    @Override

    public PetState tick(PetContext ctx) {

        ctx.dx = 0;

        ctx.dy = 0;

        ctx.stateTimer++;

        if (PetState.IDLE.hasTimeLimit() && ctx.stateTimer > PetState.IDLE.getMaxTicks()) {

            ctx.stateTimer = 0;

            int hMargin = 250;

            boolean inCenter = ctx.location.x > hMargin && ctx.location.x < (SCREEN.width - hMargin);

            if (inCenter) {

                ctx.dx = (ctx.location.x < SCREEN.width / 2) ? -3.5 : 3.5;

                ctx.dy = (ctx.location.y < SCREEN.height / 2) ? -1.5 : 1.5;

            } else {

                if (Math.random() < 0.9) {

                    ctx.dx = (ctx.location.x < SCREEN.width / 2) ? -3.5 : 3.5;

                    ctx.dy = (ctx.location.y < SCREEN.height / 2) ? -1.5 : 1.5;

                } else {

                    ctx.dx *= -1;

                }

            }

            return PetState.WALKING;

        }

        return null;

    }

}

class WalkingBehaviour extends BehaviourHandler {

    @Override

    public PetState tick(PetContext ctx) {

        ctx.stateTimer++;

        double speedMult = (ctx.batteryLevel < 25) ? 0.5 : 1.0;

        ctx.isFacingLeft = (ctx.dx < 0);

        int nx = ctx.location.x + (int)(ctx.dx * speedMult);

        int ny = ctx.location.y + (int)(ctx.dy * speedMult);

        if (nx < 0 || nx > SCREEN.width - ctx.windowW) ctx.dx *= -1;

        if (ny < 0 || ny > SCREEN.height - ctx.windowH) ctx.dy *= -1;

        ctx.location = clamp(new Point(nx, ny), ctx.windowW, ctx.windowH);

        if (PetState.WALKING.hasTimeLimit() && ctx.stateTimer > PetState.WALKING.getMaxTicks()) {

            ctx.stateTimer = 0;

            return PetState.IDLE;

        }

        return null;

    }

}

class SittingBehaviour extends BehaviourHandler {
    @Override
    public PetState tick(PetContext ctx) {
        ctx.dx = 0;
        ctx.dy = 0;
        return null;
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  MODIFIED FriendshipManager: Handles Death Logic
// ════════════════════════════════════════════════════════════════════════════
class FriendshipManager {
    private static final String SAVE_FILE = ".fox_status.dat";
    private double friendship = 50.0;
    private boolean isDead = false;
    private JProgressBar bar;

    public FriendshipManager() { load(); }

    public void increase(double amount) {
        if (isDead) return;
        friendship = Math.min(friendship + amount, 100.0);
        refreshBar();
        save();
    }

    public void decrease(double amount) {
        if (isDead) return;
        friendship = Math.max(friendship - amount, 0.0);
        if (friendship <= 0) isDead = true;
        refreshBar();
        save();
    }

    public boolean isDead()      { return isDead; }
    public double getTotal()     { return friendship; }
    public void setBar(JProgressBar bar) { this.bar = bar; refreshBar(); }

    public void save() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeDouble(friendship);
            out.writeBoolean(isDead);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            friendship = in.readDouble();
            isDead     = in.readBoolean();
        } catch (IOException e) { e.printStackTrace(); }
    }
    public void reset() {
        friendship = 50.0;
        isDead = false;
        refreshBar();
        save();
    }
    private void refreshBar() {
        if (bar != null) bar.setValue((int) friendship);
    }
}
class SpriteAnimator {
    private static final int TILE_SIZE = 32;
    private final int petScale;
    private BufferedImage spriteSheet;
    private final JLabel target;
    private int currentFrame = 0;

    public SpriteAnimator(JLabel target, int petScale) {
        this.target   = target;
        this.petScale = petScale;
        try {
            URL url = getClass().getResource("/fox.png");
            if (url != null) spriteSheet = ImageIO.read(url);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void advance(PetState state, boolean isFacingLeft, boolean isHeld, boolean isDead) {
        if (isDead) {
            setTombstone();
            return;
        }
        if (spriteSheet == null) return;
        int row, frame;
        if (isHeld) {
            row   = PetState.HELD.getSpriteRow();
            frame = 6;
        } else {
            currentFrame = (currentFrame + 1) % state.getMaxFrames();
            row   = state.getSpriteRow();
            frame = currentFrame;
        }
        int px = frame * TILE_SIZE, py = row * TILE_SIZE;
        if (px + TILE_SIZE > spriteSheet.getWidth() ||
                py + TILE_SIZE > spriteSheet.getHeight()) return;

        BufferedImage tile = spriteSheet.getSubimage(px, py, TILE_SIZE, TILE_SIZE);
        if (isFacingLeft) tile = flip(tile);
        target.setIcon(new ImageIcon(tile.getScaledInstance(petScale, petScale, Image.SCALE_FAST)));
    }

    public void setTombstone() {
        URL url = getClass().getResource("/tombstone.png");
        if (url != null) {
            target.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(petScale, petScale, Image.SCALE_SMOOTH)));
        }

    }

    private BufferedImage flip(BufferedImage img) {
        BufferedImage out = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, TILE_SIZE, 0, -TILE_SIZE, TILE_SIZE, null);
        g.dispose();
        return out;
    }
}

public class FoxDesktopPet extends JFrame {

    private static final int PET_SCALE = 128;
    private final FriendshipManager friendshipManager = new FriendshipManager();
    private SpriteAnimator animator;
    private BehaviourHandler currentBehaviour = new IdleBehaviour();
    private PetState currentState = PetState.IDLE;
    private int stateTimer = 0;
    private double dx = 0, dy = 0;
    private boolean isFacingLeft = false;
    private boolean isStaying = false;
    private boolean isHeld = false;
    private int batteryLevel = 100;
    private int strokeDistance = 0;
    private Point lastStrokePoint = null;
    private JLabel petLabel;
    private JLayeredPane layeredPane;
    private Point dragOffset;
    private FoxBubble activeBubble;
    private JFrame activeMenu;
    private VisualFlowMenu activeFlowMenu = null; // will see whether the flowchart menu is open or not
    public static FoxDesktopPet currentFox;
    private TaskManager taskManager;
    public FoxDesktopPet() {
        currentFox = this;
        //friendshipManager.reset(); for resetting the friendship

        //friendshipManager.decrease(200); for testing death of the fox
        if (friendshipManager.isDead()) {
            handleDeathStartup();
            return;
        }
        setupWindow();
        setupStage();
        setupPetLabel();
        animator = new SpriteAnimator(petLabel, PET_SCALE);
        setupMouseInteractions();
        initTimers();
        setVisible(true);
    }

    private void handleDeathStartup() {
        setupWindow();
        setupStage();
        setupPetLabel();
        animator = new SpriteAnimator(petLabel, PET_SCALE);
        animator.setTombstone();
        animator.advance(PetState.DEAD, false, false, true);
        petLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                triggerJumpscare(); // This fires when the user clicks the tombstone
            }
        });
        setVisible(true);

        new Timer(5000, e -> System.exit(0)).start();
    }
    private void triggerJumpscare() {
        JWindow scare = new JWindow();
        scare.setAlwaysOnTop(true);
        // This makes the window fill the entire screen
        scare.setSize(Toolkit.getDefaultToolkit().getScreenSize());

        // This makes the background of the window invisible
        scare.setBackground(new Color(0, 0, 0, 0));

        JLabel gifLabel = new JLabel();
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);

        URL gifURL = getClass().getResource("/scare.gif");
        if (gifURL != null) {
            gifLabel.setIcon(new ImageIcon(gifURL));
        }

        scare.add(gifLabel);
        scare.setVisible(true);

        // Shake the window or just wait, then kill the process
        Timer exitTimer = new Timer(2500, e -> System.exit(0));
        exitTimer.setRepeats(false);
        exitTimer.start();
    }
    private void setupWindow() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getRootPane().setDoubleBuffered(true);
        setSize(PET_SCALE, PET_SCALE + 100);
        setLocationRelativeTo(null);
    }

    private void setupStage() {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(PET_SCALE, PET_SCALE + 100));
        add(layeredPane);
    }

    private void setupPetLabel() {
        petLabel = new JLabel();
        petLabel.setBounds(0, 50, PET_SCALE, PET_SCALE);
        layeredPane.add(petLabel, JLayeredPane.DEFAULT_LAYER);
    }

    private void transitionTo(PetState newState) {
        if(friendshipManager.isDead()) return;
        currentState = newState;
        stateTimer = 0;
        switch (newState) {
            case IDLE: currentBehaviour = new IdleBehaviour(); break;
            case WALKING: currentBehaviour = new WalkingBehaviour(); break;
            case SITTING:
            case STAYING: currentBehaviour = new SittingBehaviour(); break;
        }
    }

    private void setupMouseInteractions() {
        if(friendshipManager.isDead()) return;
        petLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if (lastStrokePoint != null) {
                    strokeDistance += (int) p.distance(lastStrokePoint);
                    if (strokeDistance > 400) {
                        spawnHeart();
                        friendshipManager.increase(0.5);
                        strokeDistance = 0;
                    }
                }
                lastStrokePoint = p;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null) return;
                Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
                Point mouse = e.getLocationOnScreen();
                int nx = Math.max(0, Math.min(mouse.x - dragOffset.x, scr.width - PET_SCALE));
                int ny = Math.max(0, Math.min(mouse.y - (dragOffset.y + 50), scr.height - (PET_SCALE + 100)));
                setLocation(nx, ny);
                if (activeBubble != null) activeBubble.setLocation(nx - 10, ny +25);
                petLabel.setBounds(0, 20, PET_SCALE, PET_SCALE);
            }
        });

        petLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragOffset = e.getPoint();
                    dx = 0; dy = 0; isHeld = true;
                    closeMenu();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                dragOffset = null; isHeld = false;
                petLabel.setBounds(0, 50, PET_SCALE, PET_SCALE);
                transitionTo(isStaying ? PetState.STAYING : PetState.IDLE);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (activeFlowMenu != null) {
                        activeFlowMenu.dispose();
                    }
                    activeFlowMenu = new VisualFlowMenu(getLocationOnScreen());//using the updated location received from updatemovement() class make the flowchart follow the fox
                   // if u click elsewhere,the flowchart menu will close
                    activeFlowMenu.setVisible(true);
                    activeFlowMenu.requestFocus();

                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Keep your original menu for left-click
                    showMenu(e);
                }
            }
        });
    }

    private void initTimers() {
        TaskManager taskManager = null;
        batteryLevel = getSystemBattery();
        new Timer(60000, e -> batteryLevel = getSystemBattery()).start();//after a short delay fetch devices's battery again
        new Timer(30, e -> updateMovement()).start();
        new Timer(100, e -> updateAnimation()).start();//after some time update the animation(idle,walking)

        // NEGLECT TIMER: Friendship will drop every 30 minutes
        new Timer(1800000, e -> {
            friendshipManager.decrease(2.0);//friendship level will drop 2 points
            if(friendshipManager.isDead()) repaint();
        }).start();
        new Timer(20000, e -> {
            if (!isHeld && !friendshipManager.isDead()) {

                int pending = AlertService.latestTaskCount;

                // Console check - look at your terminal at the bottom of IntelliJ!
                System.out.println("Fox is checking... Tasks found: " + pending);

                if (pending > 0) {
                    // Priority: Nagging
                    speak(new FoxTaskAlert(pending));
                } else if (Math.random() > 0.7) {
                    // Normal: Chill talk
                    speak(new RandomSpeech());
                }
            }
        }).start();

        new Timer(30000, e -> {
            if (taskManager != null) {
                // Update the "Billboard" manually here
                ArrayList<Task> tasks = taskManager.getAlertTasks();
                AlertService.latestTaskCount = tasks.size();
            }
        }).start();
        new Timer(20000, e -> {
            if (!isHeld && !friendshipManager.isDead() && Math.random() > 0.7) {
                speak(new RandomSpeech());
            }
        }).start();
    }

    private void updateMovement() {
        if (isHeld || friendshipManager.isDead()) return;
        PetContext ctx = new PetContext(getLocation(), dx, dy, stateTimer, isFacingLeft, batteryLevel, PET_SCALE, PET_SCALE + 100);
        PetState next = currentBehaviour.tick(ctx);
        dx = ctx.dx; dy = ctx.dy; stateTimer = ctx.stateTimer; isFacingLeft = ctx.isFacingLeft;
        if (!ctx.location.equals(getLocation())) {
            setLocation(ctx.location);
            if (activeBubble != null) activeBubble.setLocation(getX() - 10, getY() +25);
        }
        if (next != null) transitionTo(next);
       //to make the flowchart follow the fox,give the updated location to the mouse clicked class
        if (activeFlowMenu != null && activeFlowMenu.isVisible()) {
            Point loc = getLocationOnScreen();
            // Use the same offset math used in the Menu constructor
            activeFlowMenu.setLocation(loc.x - 135, loc.y + 5);
        }
    }

    private void updateAnimation() {
        PetState animState = isStaying ? PetState.STAYING : currentState;
        animator.advance(animState, isFacingLeft, isHeld, friendshipManager.isDead());
    }

    private void closeMenu() {
        if (activeMenu != null) {
            activeMenu.dispose();
            activeMenu = null;
            friendshipManager.setBar(null);
            if (!isStaying && !friendshipManager.isDead()) transitionTo(PetState.IDLE);
            if (activeFlowMenu != null) { activeFlowMenu.dispose(); activeFlowMenu = null; }
        }
    }

    private void showMenu(MouseEvent e) {
        if(friendshipManager.isDead()) return;
        closeMenu();
        JFrame menuFrame = new JFrame();
        menuFrame.setUndecorated(true);
        menuFrame.setBackground(new Color(0, 0, 0, 0));
        menuFrame.setAlwaysOnTop(true);
        activeMenu = menuFrame;

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                try {
                    URL bgUrl = getClass().getResource("/Background.png");
                    if (bgUrl != null) {
                        g.drawImage(new ImageIcon(bgUrl).getImage(), 0, 0, getWidth(), getHeight(), this);
                        return;
                    }
                } catch (Exception ignored) {}
                g.setColor(new Color(245, 222, 179));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel energyLabel = new JLabel("Energy Bar: " + batteryLevel + "%");
        energyLabel.setForeground(new Color(80, 50, 20));
        energyLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel friendLabel = new JLabel("Friendship:");
        friendLabel.setForeground(new Color(80, 50, 20));
        friendLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(false);
        bar.setForeground(new Color(255, 105, 180));
        bar.setBackground(new Color(245, 222, 179));
        bar.setMaximumSize(new Dimension(100, 10));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        friendshipManager.setBar(bar);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(180, 140, 100));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JButton stayBtn = makeMenuButton(isStaying ? "Start Roaming" : "Stay Here");
        JButton feedBtn = makeMenuButton("Feed Fox");
        JButton exitBtn = makeMenuButton("Send Home");

        stayBtn.addActionListener(al -> {
            isStaying = !isStaying;
            transitionTo(isStaying ? PetState.STAYING : PetState.IDLE);
            closeMenu();
        });
        feedBtn.addActionListener(al -> { closeMenu(); handleFeedAction(); });
        exitBtn.addActionListener(al -> { friendshipManager.save(); System.exit(0); });

        panel.add(energyLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(friendLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(8));
        panel.add(stayBtn);
        panel.add(Box.createVerticalStrut(4));
        panel.add(feedBtn);
        panel.add(Box.createVerticalStrut(4));
        panel.add(exitBtn);

        menuFrame.add(panel);
        menuFrame.pack();
        Point loc = petLabel.getLocationOnScreen();
        menuFrame.setLocation(loc.x + e.getX(), loc.y + e.getY());
        menuFrame.addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent we) {}
            public void windowLostFocus(WindowEvent we) { closeMenu(); }
        });
        menuFrame.setVisible(true);
    }

    private JButton makeMenuButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getForeground());
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), 0, fm.getAscent());
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(new Color(40, 20, 5));
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return btn;
    }

    private void handleFeedAction() {
        friendshipManager.increase(0.8);
        speak(new FoodSpeech());
        transitionTo(PetState.SITTING); // Berry sits to eat

        // Updated Timer to respect the "Stay" command
        Timer wakeUp = new Timer(4000, e -> {
            if (!isHeld && !friendshipManager.isDead()) {
                // Check the boolean we toggled in the menu!
                if (isStaying) {
                    transitionTo(PetState.STAYING);
                } else {
                    transitionTo(PetState.IDLE);
                }
            }
        });
        wakeUp.setRepeats(false);
        wakeUp.start();
    }
//Interface used for different dialogues of the fox
    interface FoxSpeech {
        String getDialogue();
    }
    public class FoodSpeech implements FoxSpeech {
        public String getDialogue() {
            return "YUMMMM! BERRILIOUS.";
        }
    }
    public class BatterySpeech implements FoxSpeech {
        private String[] lines = {"I am feeling quite sleepy....", "I need some rest.", "Let me sleep Hooman", "BERRY WANTS TO SLEEP"};
        private Random object = new Random();

        public String getDialogue() {
            return
                    lines[object.nextInt(lines.length)];
        }

    }
    public static void announce(boolean isBreak) {
        Random object=new Random();
        if (currentFox != null) {
            if (isBreak) {
                String[] breakLines = {
                    "Yay! Break time!",
                    "Time for a berry snack!",
                    "You earned this rest,hooman!",
                    "Stretch those legs!",
                    "You did great." ,
                    "Ready for another session?",
                    "You earned your treat."
            };
                int index=object.nextInt(breakLines.length);
                currentFox.speak(() -> breakLines[index]);
            } else {
                String[] focusLines = {
                        "You are doing great.",
                        "Keep working hard!",
                        "You  have got this.",
                        "FOCUS HOOMAN.",
                        "Just a bit more.",
                        "No distractions Hooman.",
                        "Tick Tock."
                };
              int index=object.nextInt(focusLines.length);
                currentFox.speak(() -> focusLines[index]);
            }
        }
    }
    public static class FoxTaskAlert implements FoxSpeech {
        private int taskCount;
        private String[] variations = {
                " %d tasks due soon!",
                "Check your Task Manager!",
                " You have %d tasks waiting!",
                "Berry sees %d tasks on your plate."
        };

        // Constructor to receive the number of tasks from AlertService
        public FoxTaskAlert(int count) {
            this.taskCount = count;
        }

        @Override
        public String getDialogue() {
            // Pick a random variation and inject the number of tasks
            String line = variations[new Random().nextInt(variations.length)];
            return String.format(line, taskCount);
        }
    }
   public class RandomSpeech implements FoxSpeech {
       private String[] lines = {"I like my new home.", "I am feeling bored.", "Pets please!", "You are a great person", "Berry misses you", "Did you forget\n about me?", "Berry well done!","Fun Fact:Foxes\ncan whistle.","I am hungry."};
       private Random object = new Random();

       public String getDialogue() {
           return lines[object.nextInt(lines.length)];
       }

   }
//------------------------------------------------------------
protected void speak(FoxSpeech speech){
        if(friendshipManager.isDead()) return;
        String line=speech.getDialogue();
        if (activeBubble != null) { activeBubble.dispose(); activeBubble = null; }
      transitionTo(isStaying?PetState.STAYING:PetState.IDLE);//if pet is in stay here command it will keep still else it will start moving
        activeBubble = new FoxBubble(line);
        activeBubble.showAt(getX() - 10, getY() +25 );
        Timer timer = new Timer(4000, e -> { if (activeBubble != null) { activeBubble.dispose(); activeBubble = null; } });//dispose the previous bubble and form a new one
        timer.setRepeats(false);
        timer.start();
    }

    private void spawnHeart() {
        if(friendshipManager.isDead()) return;
        JLabel heart = new JLabel();
        URL url = getClass().getResource("/heart.png");//obtaining heart png for the resources root
        if (url != null) heart.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        heart.setBounds(PET_SCALE / 2 - 20, 50, 40, 40);
        layeredPane.add(heart, JLayeredPane.POPUP_LAYER);
        new Timer(30, ev -> {
            heart.setLocation(heart.getX(), heart.getY() - 4);
            if (heart.getY() < -40) { layeredPane.remove(heart); ((Timer) ev.getSource()).stop(); }
        }).start();
    }

    private int getSystemBattery() {
        try {
            Process p = Runtime.getRuntime().exec("powershell (Get-WmiObject -Class Win32_Battery).EstimatedChargeRemaining");//this command fetchs the device's battery
            Scanner s = new Scanner(p.getInputStream());
            int level = s.hasNextInt() ? s.nextInt() : 100;
            if (level < 20 && !friendshipManager.isDead()) speak(new BatterySpeech());//if battay is less than 20 it is evoke a dialogue
            return level;
        } catch (Exception e) { return 100; }
    }

    public class FoxBubble extends JWindow {
        public FoxBubble(String text) {
            JLabel label = new JLabel(text);
            URL url = getClass().getResource("/bubble1.png");
            if (url != null) label.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(180, 100, Image.SCALE_SMOOTH)));
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.TOP);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
            label.setFont(new Font("Arial", Font.BOLD, 12));
            add(label);
            setBackground(new Color(0, 0, 0, 0));
            setAlwaysOnTop(true);
            pack();
        }
        public void showAt(int x, int y) { setLocation(x, y); setVisible(true); }
    }
    public void setTaskManager(TaskManager manager) {
        this.taskManager = manager;
        manager.load();

        int overdueCount = manager.countOverdue();
        if (overdueCount > 0) {
            friendshipManager.decrease(overdueCount * 5.0);//friendship will decrease if you do not complete your tasks
        }

        AlertService.latestTaskCount = manager.getAlertTasks().size();
        AlertService alertService = new AlertService(FoxDesktopPet.this, manager);
        alertService.checkAndAlert();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManager manager = new TaskManager();
            FoxDesktopPet fox = new FoxDesktopPet();
            fox.setTaskManager(manager);
            fox.setVisible(true);

        });
    }
}

class VisualFlowMenu extends JWindow {

    public VisualFlowMenu(Point foxLoc) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose(); // Missed a button? Menu gone.
            }
        });
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0)); // Full transparency for the "floating" look
        setLayout(null);
        setSize(400, 300); // The "Canvas" area above the fox

       //For creating a flowchart type menu with the further functionalities of the fox
        JButton pomo = createNode("Pomodoro", 155, 200, new Color(255, 107, 107));
        JButton task = createNode("Task Manager", 280, 110, new Color(78, 205, 196));
        JButton pray = createNode("Prayer Manager", 30, 110, new Color(255, 217, 61));
        JButton jour = createNode("My Journal", 155, 20, new Color(162, 155, 254));

        // For now, these just close the menu so you can see the animation
        ActionListener closeAction = e -> dispose();
        pomo.addActionListener(e -> { Pomodoro myPomo = new Pomodoro();
            // Create a new timer that nags you every 30 seconds
            Timer nagTimer = new Timer(60000, event -> {
                // If the window was closed, stop nagging
                if (!myPomo.isVisible()) {
                    ((Timer)event.getSource()).stop();
                    return;
                }
                // Ask the Fox to announce based on the current state
                // We look at the 'isbreak' variable inside your Pomodoro
                boolean onBreak = myPomo.isCurrentlyOnBreak();
                FoxDesktopPet.announce(onBreak);
            });
            nagTimer.start();
            dispose();
        });

        task.addActionListener(e -> {
            MainTaskManager.main(null); // Just run the Task Manager's main method!
            dispose();
        });
        pray.addActionListener(closeAction);
        jour.addActionListener(e->{FoxJournal MyJournal =new FoxJournal();//inheritance is used here
            MyJournal.setVisible(true);//the journal will be visible
            dispose();});//for closing the menu

        add(pomo); add(task); add(pray); add(jour);

        // Position it so the bottom "Pomodoro" node sits right above the fox's head
        setLocation(foxLoc.x - 135, foxLoc.y +5);

        // Close if you click anywhere else on the screen
        addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) {}
            public void windowLostFocus(WindowEvent e) { dispose(); }
        });
    }

    private JButton createNode(String text, int x, int y, Color bg) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 95, 45);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Draw Flowchart Connectors ---
        g2.setColor(new Color(255, 255, 255, 180)); // Semi-transparent white lines
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Lines connecting from the top node (Journal) down to the side nodes and bottom
        g2.drawLine(202, 65, 202, 200);   // Journal -> Pomodoro (Vertical Spine)
        g2.drawLine(202, 100, 80, 110);   // Spine -> Prayer
        g2.drawLine(202, 100, 330, 110);  // Spine -> Tasks

        super.paint(g); // Draws the buttons on top of the lines
    }
}
