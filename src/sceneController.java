import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

public class sceneController {

    @FXML private AnchorPane pane_AnchorPane;
    @FXML private Button save_Button;
    @FXML private TextArea journal_TextArea;
    @FXML private ImageView Image_ImageView;
    @FXML private Label emotion_Label;
    @FXML private Label tip_Label;

 
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        Image_ImageView.setImage(safeLoadImage("/Neutral.jpg"));
        pane_AnchorPane.setStyle("-fx-background-color: #f5f5f5;");
    }

    @FXML
    private void save_Button_clicked(ActionEvent e) {
        String userText = journal_TextArea.getText().trim();
        if (userText.isEmpty()) return;

        emotion_Label.setText("Analyzing mood...");
        tip_Label.setText("");
        pane_AnchorPane.setStyle("-fx-background-color: #f5f5f5;");

        new Thread(() -> {
            try {
                GeminiClient gemini = new GeminiClient("AIzaSyA0lilJEEoVz3tVqX6jkq4lKUPOnIIdm0E");
                String response = gemini.analyzeMessage(userText);
                System.out.println("Gemini raw response:\n" + response);

                if (response == null || response.isEmpty()) {
                    throw new IOException("Empty response from Gemini API");
                }

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.isEmpty()) {
                    throw new IOException("No candidates found in API response");
                }

                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.isEmpty()) {
                    throw new IOException("No parts found in API response");
                }

                String text = parts.getJSONObject(0).getString("text");
                JSONObject extracted = new JSONObject(text);
                String emotion = extracted.getString("emotion");
                String tip = extracted.getString("tip");

                Platform.runLater(() -> updateEmotionUI(emotion, tip));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    tip_Label.setText("Error analyzing mood. Please try again.");
                    emotion_Label.setText("Mood Insight: Unknown");
                });
            }
        }).start();

        Note n = new Note(userText);
        NoteManager manager = new NoteManager("test.txt");
        manager.setNote(n);
        try { manager.saveNote(); } 
            catch (IOException ex) { 
            ex.printStackTrace(); }
            journal_TextArea.clear();
    }


    private void updateEmotionUI(String emotion, String tip) {
        String imgPath, bgColor;
        String displayEmotion = emotion.substring(0, 1).toUpperCase() + emotion.substring(1);

        switch (emotion.toLowerCase()) {
            case "positive" -> { imgPath = "Happy.jpg"; bgColor = "#E8F5E9"; }
            case "sad"      -> { imgPath = "Sad.jpg";   bgColor = "#E3F2FD"; }
            case "angry"    -> { imgPath = "Angry.jpg"; bgColor = "#FFEBEE"; }
            default         -> { imgPath = "Neutral.jpg"; bgColor = "#F5F5F5"; }
        }

        Image_ImageView.setImage(safeLoadImage(imgPath));
        animateBackgroundTransition(bgColor);

        emotion_Label.setText("Mood Insight: " + displayEmotion);
        emotion_Label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        tip_Label.setText("Tip: " + tip);
    }

    private void animateBackgroundTransition(String hexColor) {
        Color newColor = Color.web(hexColor);
        Rectangle bgRect = new Rectangle(pane_AnchorPane.getWidth(), pane_AnchorPane.getHeight());
        bgRect.setFill(Color.web("#f5f5f5"));
        pane_AnchorPane.getChildren().add(0, bgRect);

        FillTransition ft = new FillTransition(Duration.millis(400), bgRect, (Color) bgRect.getFill(), newColor);
        ft.setOnFinished(ev -> {
            pane_AnchorPane.setStyle("-fx-background-color: " + hexColor + ";");
            pane_AnchorPane.getChildren().remove(bgRect);
        });
        ft.play();
    }


    private Image safeLoadImage(String fileName) {
   
        if (!fileName.endsWith(".png")) {
            fileName += ".png";
        }

        URL url1 = getClass().getResource("/main/" + fileName);
        URL url2 = getClass().getResource("/" + fileName);

        System.out.println(" Trying paths for " + fileName);
        System.out.println("  • getResource('/main/" + fileName + "') = " + url1);
        System.out.println("  • getResource('/" + fileName + "') = " + url2);

        URL chosen = (url1 != null) ? url1 : url2;

        if (chosen != null) {
            System.out.println("Loaded image from: " + chosen);
            return new Image(chosen.toExternalForm());
        } else {
            System.err.println("Image not found on classpath: " + fileName);
            return new Image("https://via.placeholder.com/200x200.png?text=Missing+" + fileName);
        }
    }


    private String extractValue(String json, String key) {
        String pattern = "\"(?i)" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) return matcher.group(1).trim();
        return "unknown";
    }
}
