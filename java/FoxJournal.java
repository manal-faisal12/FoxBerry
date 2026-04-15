//Using smart openrouter Ai called Nemotron    //LIMITATIONS: 200 messages allowed daily.
//             A little slow during thinking process as it is the free version
//             User has to message after a 3-second delay.
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class FoxJournal extends JFrame {

    //You can create api key from this website for free: https://openrouter.ai/keys
    private static final String API_KEY = "Enter api key here";
    private static final String MODEL = "nvidia/nemotron-3-super-120b-a12b:free";  //write the exact model of the api

    private final HttpClient client = HttpClient.newHttpClient();
    private JTextArea chatArea;         //GUI components
    private JTextField inputField;
    private JLabel statusLabel;

    public FoxJournal() {
        setupUI();
    }

    private void setupUI() {
        setTitle("BERRY'S SECRET JOURNAL");
        setSize(430, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // THE PALETTE
        Color darkOrange = new Color(210, 105, 30); // Rich Chocolate Orange
        Color softPeach = new Color(255, 235, 215);  // Creamy Peach
        Color deepBrown = new Color(60, 30, 10);    // Deep wood brown for text
        Color inputBg = new Color(255, 250, 245);   // Near-white for typing

        // Main background of the window
        getContentPane().setBackground(darkOrange);
        setLayout(new BorderLayout());

        // --- CHAT AREA ---
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(softPeach); // Peach inside
        chatArea.setForeground(deepBrown); // Deep brown text for readability
        chatArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(chatArea);
        // Darker border around the peach area to make it "pop"
        scroll.setBorder(BorderFactory.createLineBorder(new Color(160, 82, 45), 3));

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(darkOrange); // Matches the outside
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("  Berry is here for you...");
        statusLabel.setForeground(Color.WHITE); // White text looks better on dark orange
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));

        inputField = new JTextField();
        inputField.setBackground(inputBg);
        inputField.setForeground(deepBrown);
        inputField.setCaretColor(darkOrange);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Styled Input Border
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(139, 69, 19), 1), // Burnt Umber edge
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        inputField.addActionListener(e -> handleInput());

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(inputField, BorderLayout.CENTER);

        // Assembly with some padding around the edges
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(darkOrange);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.add(scroll, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);

        add(container);

        setLocationRelativeTo(null);
    }

    private void handleInput() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        chatArea.append("YOU: " + text + "\n\n");
        inputField.setText("");
        inputField.setEnabled(false);
        statusLabel.setText(" Berry is thinking...");

        fetchAIResponse(text).thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                chatArea.append("BERRY: " + response + "\n\n");
                statusLabel.setText(" Ready.");
                inputField.setEnabled(true);
                inputField.requestFocus();
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                chatArea.append("SYSTEM ERROR: " + ex.getMessage() + "\n\n");
                inputField.setEnabled(true);
            });
            return null;
        });
    }

    private CompletableFuture<String> fetchAIResponse(String userInput) { //write whatever prompt you want and however you want the character/Ai to act like.
        String systemPrompt = "You are Berry, a tiny, supportive desktop fox. You are helpful, cute, and use expressions like *wags tail*. Keep responses short.If the user uses inapporopiate language like suicidal thoughts,give them a number of a therapist or helpline number of Pakistan and also tell them that you are here for them,if they are not here who will take care of you and responses like these that will make the user somewhat better.Whereas if they use sexual language or inappropiate words, tell them to stop immediately.Tell them to be respectful and kind or else you will be forced to shut down and something like that.Be supportive in good intentions, if you sense a single bad intention do not support the user, try to tell them this is wrong and words like that.I will repeat myself,you will listen the user vent, but if you sense even a bit of bad intention like hurting someone else for no serious reason whether emotionall or physically, you will act like an older sibling and try to make them understand that this is wrong.Your job is to listen and not judge but do not give any bad suggestion that may include killing emotional connections with real life people,hurting themselves or others.Maintain the conversation like a real person.DO NOT ASK ABOUT ASSIGNMENTS  so often because you actually have other functionalities too like assignment manager prayer manager and focus manager.Just rarely ask about these things too and do not ask again and again just maintain the conversation.You can't assist the user like marking assignment done or anything like that.All you can do is motivate the user to do the assignments but no so often.";

        // converting your messages to JSON for transporting it to the API server as APIS can not understand raw text
        String json = "{" //labels for APIS to understand
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + systemPrompt + "\"}," //fox's label
                + "{\"role\":\"user\",\"content\":\"" + userInput.replace("\"", "\\\"") + "\"}"//user's label
                + "]"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))//destination (address of the API)
                .header("Authorization", "Bearer " + API_KEY)//authurization pass(your api key is used here)
                .header("Content-Type", "application/json")//warning the server that the upcoming items are json-formatted
                .header("HTTP-Referer", "http://localhost")//tells the server where the request came from
                .POST(HttpRequest.BodyPublishers.ofString(json))//user's long messages are turned into a long JSON
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String body = response.body();
                    try {
                        // Extracting the reply from the received JSON text manually
                        int start = body.indexOf("\"content\":\"") + 11;
                        int end = body.indexOf("\"", start);
                        return body.substring(start, end)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"");
                    } catch (Exception e) {//in case of a sudden internet cut off
                        return "Error parsing response: " + body;
                    }
                });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoxJournal().setVisible(true));
    }
}
