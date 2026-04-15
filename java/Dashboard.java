import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
public class Dashboard extends JFrame {


    private static final Color BG      = new Color(0x0F0F1A);
    private static final Color CARD    = new Color(0x1A1A2E);
    private static final Color ACCENT  = new Color(0x7C3AED);
    private static final Color ACCENT2 = new Color(0xE040FB);
    private static final Color TEXT    = new Color(0xF0F0FF);
    private static final Color SUBTLE  = new Color(0x8888AA);

    private TaskManager  manager;
    private StatsPanel   statsPanel;
    private AlertService alertService;

    // The panel that holds all the task cards
    private JPanel taskListPanel;

    // Which filter is currently active
    private String activeFilter = "All";

    // Clock labels in the header
    private JLabel clockLabel;
    private JLabel dateLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Dashboard(TaskManager manager) {
        super("Task Manager");
        this.manager      = manager;
        this.statsPanel   = new StatsPanel();
        this.alertService = new AlertService(this, manager);

        buildUI();
        refresh();       // show existing tasks
        startClock();    // start the live clock
        URL iconUrl = getClass().getResource("/Main_taskManager/resources/icon_fox.png"); // path to image
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setIconImage(icon.getImage());
        } else {
            System.out.println("Could not find the icon file!");
        }
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 660);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        setVisible(true);

        // Show alerts 800ms after window opens
        Timer alertTimer = new Timer(true);
        alertTimer.schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        alertService.checkAndAlert();
                    }
                });
            }
        }, 800);
    }


    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    //builds header
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, ACCENT),
            new EmptyBorder(12, 22, 12, 22)));

        // App name on the left
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel appName = new JLabel("Task Manager");
        appName.setFont(new Font("Segue UI", Font.BOLD, 22));
        appName.setForeground(TEXT);
        /*JLabel subtitle = new JLabel("  Academic Task Manager");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(SUBTLE);
        */
        left.add(appName);
        //left.add(subtitle);
        header.add(left, BorderLayout.WEST);

        // Live clock on the right
        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setOpaque(false);

        clockLabel = new JLabel("--:--:--", SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Segue UI", Font.BOLD, 18));
        clockLabel.setForeground(ACCENT2);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateLabel = new JLabel("---", SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segue UI", Font.PLAIN, 11));
        dateLabel.setForeground(SUBTLE);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockPanel.add(clockLabel);
        clockPanel.add(dateLabel);
        header.add(clockPanel, BorderLayout.EAST);

        return header;
    }

    //adds scroll filter and sets border the lower panel set
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(14, 18, 14, 18));

        body.add(statsPanel, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setBackground(BG);
        centre.add(buildFilterBar(), BorderLayout.NORTH);
        centre.add(buildTaskScroll(), BorderLayout.CENTER);
        body.add(centre, BorderLayout.CENTER);

        body.add(buildAddButton(), BorderLayout.SOUTH);
        return body;
    }

    //filter between assignment, project, quiz and etc
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBackground(BG);

        String[] filters = {"All", "Assignment", "Quiz", "Project", "Lab Report", "Completed", "Overdue"};
        ButtonGroup group = new ButtonGroup();

        for (final String filter : filters) {
            JToggleButton btn = new JToggleButton(filter);
            btn.setFont(new Font("Segue UI", Font.BOLD, 11));
            btn.setForeground(TEXT);
            btn.setBackground(CARD);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(SUBTLE.darker(), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (filter.equals("All")) btn.setSelected(true);

            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    activeFilter = filter;
                    refresh();
                }
            });

            group.add(btn);
            bar.add(btn);
        }

        return bar;
    }

    // ── Scrollable task list ──────────────────────────────────────────────────
    private JScrollPane buildTaskScroll() {
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Add Task button at the bottom ─────────────────────────────────────────
    private JPanel buildAddButton() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bar.setBackground(BG);

        JButton addBtn = new JButton("+") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        addBtn.setFont(new Font("Segue UI", Font.BOLD, 34));
        addBtn.setForeground(TEXT);
        addBtn.setBackground(ACCENT);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(false);
        addBtn.setPreferredSize(new Dimension(55, 55));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openAddDialog();
            }
        });

        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(ACCENT2); }
            public void mouseExited(MouseEvent e)  { addBtn.setBackground(ACCENT); }
        });

        bar.add(addBtn);
        return bar;
    }

    //refresh deletes everything in panel and loads array list based on filter
    public void refresh() {
        taskListPanel.removeAll();
        ArrayList<Task> toShow;  //creates arraylist

        if (activeFilter.equals("All")) {
            toShow = manager.getAllTasks();
        }
        else if (activeFilter.equals("Completed")) {
            toShow = manager.filterCompleted();
        }
        else if (activeFilter.equals("Overdue")) {
            toShow = manager.filterOverdue();
        }
        else {
            toShow = manager.filterByType(activeFilter);
        }
        //filters by type and creates arraylist of type

        // Show a message if the list is empty
        if (toShow.isEmpty()) {
            JLabel empty = new JLabel("No tasks added yet", SwingConstants.CENTER);
            empty.setFont(new Font("Segue UI", Font.ITALIC, 14));
            empty.setForeground(SUBTLE);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            taskListPanel.add(Box.createVerticalGlue());
            taskListPanel.add(empty); //empty is the label added to the panel that holds text
            taskListPanel.add(Box.createVerticalGlue());

        } else {
            // Add one TaskCard for each task
            for (final Task t : toShow) {
                // Runnable is like a simple action it tells the card what to do when toggled/deleted
                Runnable onToggle = new Runnable() { //operation does not return result
                    public void run() {
                        manager.toggleCompletion(t.getId());
                        refresh();
                    }
                };

                Runnable onDelete = new Runnable() {
                    public void run() {
                        int choice = JOptionPane.showConfirmDialog(
                                Dashboard.this,
                                "Delete \"" + t.getTitle() + "\"?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            manager.removeTask(t.getId());
                            refresh();
                        }
                    }
                };

                TaskCard card = new TaskCard(t, onToggle, onDelete);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                taskListPanel.add(card);
                taskListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        // Update the stats panel
        statsPanel.update(
            manager.totalTasks(),
            manager.countCompleted(),
            manager.countPending(),
            manager.countOverdue()
        );

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // ── Open the Add Task dialog ──────────────────────────────────────────────
    private void openAddDialog() {
        AddTaskDialog dialog = new AddTaskDialog(this, manager);
        dialog.setVisible(true);

        Task newTask = dialog.getResult();
        if (newTask != null) {
            manager.addTask(newTask);
            refresh();
        }
    }

    // ── Live clock: updates every second ─────────────────────────────────────
    private void startClock() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

        Timer timer = new Timer(true); // daemon timer — stops when app closes
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Always update Swing components on the Event Dispatch Thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        LocalDateTime now = LocalDateTime.now();
                        clockLabel.setText(now.format(timeFormat));
                        dateLabel.setText(now.format(dateFormat));
                    }
                });
            }
        }, 0, 1000); // run immediately, then every 1000ms (1 second)
    }

    public static class AddTaskDialog extends JDialog {


        static final Color BG     = new Color(0x0F0F1A);
        static final Color CARD   = new Color(0x1A1A2E);
        static final Color ACCENT = new Color(0x7C3AED);
        static final Color ACCENT2= new Color(0xE040FB);
        static final Color TEXT   = new Color(0xF0F0FF);
        static final Color SUBTLE = new Color(0x8888AA);

        // The task that was created — null if user canceled
        private Task result = null;
        //declaring a reference variable but not assigning anything
        // abstact do not have objects but this is just a ref

        private TaskManager manager;

        private JComboBox typeBox;
        private JComboBox difficultyBox; //part of swing, combines dropdown and text field
        private JTextField titleField;
        private JTextField subjectField;
        private JTextField descField;
        //sets time
        private JSpinner yearSpinner;
        private JSpinner monthSpinner;
        private JSpinner daySpinner;
        private JSpinner hourSpinner;
        private JSpinner minuteSpinner;

        // handles extra per task
        private JPanel extraPanel;
        // for assignment
        private JTextField marksF, formatF;
        // for quiz
        private JTextField durationF, quizType;
        // for project
        private JTextField teamSize, presentation;
        // for lab report
        private JTextField labNum, experiment;

        //for add new task
        public AddTaskDialog(Frame owner, TaskManager manager) {
            super(owner, "Add New Task", true);
            //model makes sure that can go nowhere without entering data

            this.manager = manager; //manager is a class ref object
            addTaskBox(); //method creates panel for input data
            setSize(560, 660);
            setLocationRelativeTo(owner);
            setResizable(false);
        }

        private void addTaskBox() {
            getContentPane().setBackground(BG);
            setLayout(new BorderLayout());
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
            header.setBackground(CARD);
            header.setBorder(new MatteBorder(0, 0, 1, 0, ACCENT));
            JLabel title = new JLabel(" Add New Task");
            title.setFont(new Font("Segue UI", Font.BOLD, 18));
            title.setForeground(TEXT);
            header.add(title);
            add(header, BorderLayout.NORTH);
            JPanel form = new JPanel();
            form.setBackground(BG);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBorder(new EmptyBorder(16, 22, 12, 22));
            JPanel row1 = rowPanel();

            typeBox = makeCombo(new String[]{"Assignment", "Quiz", "Project", "Lab Report"});
            difficultyBox = makeCombo(new String[]{"Easy", "Medium", "Hard"});

            //panel named row1 adds task type
            row1.add(labeledField("Task Type", typeBox, 200));
            row1.add(Box.createHorizontalStrut(12));

            row1.add(labeledField("Difficulty", difficultyBox, 160));
            form.add(row1);

            form.add(Box.createVerticalStrut(10));
            titleField = makeTextField();
            form.add(labeledField("Task Title", titleField, 460));
            form.add(Box.createVerticalStrut(10));
            subjectField = makeTextField();
            form.add(labeledField("Subject", subjectField, 460));
            form.add(Box.createVerticalStrut(10));
            descField = makeTextField();
            form.add(labeledField("Description (optional)", descField, 460));
            form.add(Box.createVerticalStrut(14));
            JLabel dateLabel = new JLabel(" Submission Date & Time");
            dateLabel.setFont(new Font("Segue UI", Font.BOLD, 13));
            dateLabel.setForeground(ACCENT2);
            dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(dateLabel);
            form.add(Box.createVerticalStrut(6));
            form.add(buildDateRow());
            form.add(Box.createVerticalStrut(14));

            // Extra fields section label
            JLabel extraLabel = new JLabel(" Task Details");
            extraLabel.setFont(new Font("Segue UI", Font.BOLD, 13));
            extraLabel.setForeground(ACCENT2);
            extraLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(extraLabel);
            form.add(Box.createVerticalStrut(6));

            // Extra fields panel (swapped when type changes)
            extraPanel = new JPanel();
            extraPanel.setBackground(BG);
            extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.Y_AXIS));
            extraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            buildExtraFields("Assignment"); // show Assignment fields by default
            form.add(extraPanel);

            // When the type dropdown changes, rebuild the extra fields
            typeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selected = (String) typeBox.getSelectedItem();
                    extraPanel.removeAll(); //removes everything in panel and rewrites the selected one again
                    buildExtraFields(selected);
                    extraPanel.revalidate();//updates the scroll bar
                    extraPanel.repaint();
                }
            });

            JScrollPane scroll = new JScrollPane(form);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(BG);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(scroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            footer.setBackground(CARD);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, ACCENT));

            JButton cancelBtn = makeButton("x Cancel", SUBTLE);
            JButton addBtn    = makeButton("+ Add Task  ", ACCENT);

            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { dispose(); }
            });

            addBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { onAdd(); }
            });

            footer.add(cancelBtn);
            footer.add(addBtn);
            add(footer, BorderLayout.SOUTH);
        }

        private JPanel buildDateRow() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setBackground(BG);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);

            LocalDateTime now = LocalDateTime.now().plusDays(3);

            yearSpinner   = makeSpinner(now.getYear(), 2025, 2035);//includes min
            monthSpinner  = makeSpinner(now.getMonthValue(), 1, 12);
            daySpinner    = makeSpinner(now.getDayOfMonth(), 1, 31);
            hourSpinner   = makeSpinner(now.getHour(), 0, 23); //24 hr clock
            minuteSpinner = makeSpinner(now.getMinute(), 0, 59);

            p.add(smallLabel("Year"));   p.add(yearSpinner);
            p.add(Box.createHorizontalStrut(4));
            p.add(smallLabel("Month"));  p.add(monthSpinner);
            p.add(Box.createHorizontalStrut(4));
            p.add(smallLabel("Day"));    p.add(daySpinner);
            p.add(Box.createHorizontalStrut(10));
            p.add(smallLabel("Hour"));   p.add(hourSpinner);
            p.add(smallLabel(":"));      p.add(minuteSpinner);

            return p;
        }

        private void buildExtraFields(String type) {
            switch (type) {
                case "Assignment": {
                    marksF = makeTextField();
                    formatF = makeTextField();
                    marksF.setText("100");
                    formatF.setText("PDF/ Handwritten");
                    JPanel r = rowPanel();
                    r.add(labeledField("Total Marks", marksF, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Submission Format", formatF, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Quiz": {
                    durationF = makeTextField();
                    quizType = makeTextField();
                    durationF.setText("30");
                    quizType.setText("MCQ/theory/lab");
                    JPanel r = rowPanel();
                    r.add(labeledField("Duration in minutes", durationF, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Quiz Type", quizType, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Project": {
                    teamSize = makeTextField();
                    presentation = makeTextField();
                    teamSize.setText("1");
                    presentation.setText("No");
                    JPanel r = rowPanel();
                    r.add(labeledField("Team Size", teamSize, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Presentation Required?", presentation, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Lab Report": {
                    labNum = makeTextField();
                    experiment = makeTextField();
                    labNum.setText("1");
                    JPanel r = rowPanel();
                    r.add(labeledField("Lab Number:", labNum, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Experiment no:", experiment, 200));
                    extraPanel.add(r);
                    break;
                }
            }
        }

        private void onAdd() {
            String title   = titleField.getText(); //text component so gets the text there
            String subject = subjectField.getText();
            String desc    = descField.getText();
            String type    = (String) typeBox.getSelectedItem();
            String diff    = (String) difficultyBox.getSelectedItem();

            //if title or subject is empty
            if (title.isEmpty() || subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Title and Subject.",
                        "Missing Fields",JOptionPane.WARNING_MESSAGE);
                return;
            }


            // Reads date for time
            int year = (int) yearSpinner.getValue();
            int month = (int) monthSpinner.getValue();
            int day = (int) daySpinner.getValue();
            int hour = (int) hourSpinner.getValue();
            int minute = (int) minuteSpinner.getValue();

            LocalDateTime dueDate;
            try {
                dueDate = LocalDateTime.of(year, month, day, hour, minute);
                if (dueDate.isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this,
                            "Due date cannot be in the past!", "Invalid Date",JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date. Please check the date fields.", "Date Error",JOptionPane.WARNING_MESSAGE);
                return;
            }

            String id = manager.generateId();

            //different
            // Task is null
            try {
                if (type.equals("Assignment")) {
                    int marks = Integer.parseInt(marksF.getText().trim()); //marks is
                    String fmt = formatF.getText().trim();
                    result = new Assignment(id, title, subject, desc, diff, dueDate, marks, fmt);

                } else if (type.equals("Quiz")) {
                    int duration = Integer.parseInt(durationF.getText().trim());
                    String qtype = quizType.getText().trim();
                    result = new Quiz(id, title, subject, desc, diff, dueDate, duration, qtype);

                } else if (type.equals("Project")) {
                    int teamSize = Integer.parseInt(this.teamSize.getText().trim());
                    String pres  = presentation.getText().trim();
                    result = new Project(id, title, subject, desc, diff, dueDate, teamSize, pres);

                } else if (type.equals("Lab Report")) {
                    String labNum = this.labNum.getText().trim();
                    String exp    = experiment.getText().trim();
                    result = new LabReport(id, title, subject, desc, diff, dueDate, labNum, exp);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid number in the numeric fields.", "Number Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            manager.addTask(result);

    // 2. TELL THE ALERT SERVICE TO RE-CHECK EVERYTHING RIGHT NOW
    // This updates 'latestTaskCount' so the Fox hears it
            new AlertService((JFrame)getOwner(), manager).checkAndAlert(); //added by minahil
            dispose(); // close the dialog
        }

        // Returns the task that was created (or null if cancelled)
        public Task getResult() { return result; }

        // ── Helper methods for building UI components ─────────────────────────────

        private JPanel rowPanel() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.setBackground(BG);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            return p;
        }

        private JPanel labeledField(String label, JComponent field, int width) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(BG);

            JLabel lb = new JLabel(label);
            lb.setFont(new Font("Segue UI", Font.BOLD, 10));
            lb.setForeground(SUBTLE);
            lb.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(width, 32));

            p.add(lb);
            p.add(Box.createVerticalStrut(3));
            p.add(field);
            return p;
        }

        private JTextField makeTextField() {
            JTextField tf = new JTextField();
            tf.setBackground(CARD);
            tf.setForeground(TEXT);
            tf.setCaretColor(TEXT);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tf.setBorder(new CompoundBorder(
                new LineBorder(ACCENT.darker(), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
            return tf;
        }

        private JComboBox makeCombo(String[] options) {
            JComboBox cb = new JComboBox(options);
            cb.setBackground(CARD);
            cb.setForeground(TEXT);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            return cb;
        }

        private JSpinner makeSpinner(int value, int min, int max) {
            JSpinner sp = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
            sp.setPreferredSize(new Dimension(64, 30));
            sp.setBackground(CARD);
            sp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            // Fix text visibility inside the spinner
            JComponent editor = sp.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JSpinner.DefaultEditor de = (JSpinner.DefaultEditor) editor;
                de.getTextField().setBackground(CARD);
                de.getTextField().setForeground(TEXT);
                de.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 12));
                de.getTextField().setCaretColor(TEXT);
                de.getTextField().setOpaque(true);
                de.getTextField().setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
                de.getTextField().setBackground(CARD);
                de.getTextField().setForeground(TEXT);
            }
            sp.setBorder(new LineBorder(ACCENT.darker(), 1, true));
            return sp;
        }

        private JButton makeButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBackground(bg);
            b.setForeground(TEXT);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(7, 16, 7, 16));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JLabel smallLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            l.setForeground(SUBTLE);
            return l;
        }
    }
}
