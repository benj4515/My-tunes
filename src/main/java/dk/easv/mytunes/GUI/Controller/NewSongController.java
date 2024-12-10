package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewSongController {

    @FXML
    public ComboBox<String> cbbCategory;
    @FXML
    public TextField txtTime, txtFile, txtArtist, txtTitle;
    @FXML
    private Button btnSave;

    private MyTunesModel myTunesModel;

    private MyTunesController myTunesController;

    public NewSongController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    public void setMyTunesController(MyTunesController myTunesController) {
        this.myTunesController = myTunesController;
    }

    @FXML
    private void initialize() {
        // this adds the music genre to the combobox with predefined genre
        cbbCategory.getItems().addAll(
                "Pop", "Rock", "Jazz", "Classical", "Hip-Hop",
                "Country", "Reggae", "Electronic", "Blues", "Folk"
        );
        cbbCategory.setEditable(true);

        // Add listener
        txtTitle.textProperty().addListener((_, _, newValue) -> txtFile.setText("Music/" + newValue));
    }

    private void displayError(Throwable t) {
        // This display if any error occours
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @FXML
    public void onCreate() throws Exception {
        // Getting data from ui
        String title = txtTitle.getText();
        String artist = txtArtist.getText();
        int time = Integer.parseInt(txtTime.getText());
        String address = txtFile.getText();
        String category = cbbCategory.getValue();

        if (title.isEmpty() || artist.isEmpty() || category.isEmpty() || address.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        // new song object
        MyTunes newSong = new MyTunes(-1, title, artist, category, address, time);

        // call model to create song in the dal
        myTunesModel.createSong(newSong);
        System.out.println("New song created: " + newSong);

        //MyTunesController.tableRefresh();
        if (myTunesController != null) {
            myTunesController.tableRefresh();
        }

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onCancelButtonPressed() {
        // this closes the window
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public void onMP3Pressed() {
        // this adds .mp3 to the address so that the user doesn't have to write it
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".wav")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".mp3";
        } else if (!currentText.toLowerCase().endsWith(".mp3")) {
            currentText += ".mp3";
        }
        txtFile.setText(currentText);
    }

    public void onWAVPressed() {
        // this adds .wav to the address so that the user doesn't have to write it
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".mp3")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".wav";
        } else if (!currentText.toLowerCase().endsWith(".wav")) {
            currentText += ".wav";
        }
        txtFile.setText(currentText);
    }
}