import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class sceneController {

    @FXML
    AnchorPane pane_AnchorPane;

    @FXML
    Button save_Button;

    @FXML
    TextArea journal_TextArea;

    @FXML
    ImageView Image_ImageView;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        Image image = new Image(getClass().getResource("/Happy.jpg").toExternalForm());
        Image_ImageView.setImage(image);
    }

    @FXML
    private void save_Button_clicked(ActionEvent e) {
        Note n = new Note(journal_TextArea.getText());
        NoteManager manager = new NoteManager("test.txt");
        manager.setNote(n);
        try {
            manager.saveNote();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        journal_TextArea.clear();
    }

    
}
