package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewSongController {

    private boolean hasExtension = false;
    @FXML
    private Button btnSave, btnWaV, btnMP3, btnChooseFile, btnCategoryMore;

    @FXML
    private Button btnCancel;

    @FXML
    public ComboBox<String> cbbCategory;

    @FXML
    public TextField txtTime, txtFile, txtArtist, txtTitle;

    private MyTunesModel myTunesModel;

    public NewSongController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        cbbCategory.getItems().addAll(
                "Pop", "Rock", "Jazz", "Classical", "Hip-Hop",
                "Country", "Reggae", "Electronic", "Blues", "Folk"
        );
        cbbCategory.setEditable(true);

        // Add lisetner
        txtTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            txtFile.setText("Music/" + newValue);
        });
    }
    private void displayError(Throwable t)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @FXML
    public void onCreate(ActionEvent actionEvent) throws Exception {

        // Getting data from ui
        String title = txtTitle.getText();
        String artist = txtArtist.getText();
        int time = Integer.parseInt(txtTime.getText());
        String adress = txtFile.getText();
        String category = cbbCategory.getId();

        if (title.isEmpty() || artist.isEmpty() || category.isEmpty() || adress.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        // new song object
        MyTunes newSong = new MyTunes(-1,title, artist, category, adress, time);

        // call model to create song in the dal
        myTunesModel.createSong(newSong);

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();

        //MyTunesController.tableRefresh();

    }
    @FXML
    public void onCancelButtonPressed(ActionEvent actionEvent) {
        System.out.printf("test");
    }

    public void onMP3Pressed(ActionEvent actionEvent) {
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".wav")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".mp3";
        } else if (!currentText.toLowerCase().endsWith(".mp3")) {
            currentText += ".mp3";
        }
        txtFile.setText(currentText);
    }

    public void onWAVPressed(ActionEvent actionEvent) {
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".mp3")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".wav";
        } else if (!currentText.toLowerCase().endsWith(".wav")) {
            currentText += ".wav";
        }
        txtFile.setText(currentText);
    }
}
