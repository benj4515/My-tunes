package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditSongController {

    @FXML
    public ComboBox<String> cbbCategory;
    @FXML
    public TextField txtTime, txtFile, txtArtist, txtTitle;
    @FXML
    private Button btnSave;
    @FXML
    private MyTunesModel myTunesModel;

    private MyTunesController myTunesController;

    public EditSongController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    public void setMyTunesController(MyTunesController myTunesController) {
        this.myTunesController = myTunesController;

        // Initialize the fields with the selected song's details
        if (myTunesController.getSelectedSong() != null) {
            MyTunes selectedSong = myTunesController.getSelectedSong();
            txtTitle.setText(selectedSong.getTitle());
            txtArtist.setText(selectedSong.getArtist());
            txtTime.setText(String.valueOf(selectedSong.getTime()));
            txtFile.setText(selectedSong.getAddress());
            cbbCategory.setValue(selectedSong.getCategory());
        }
    }

    @FXML
    public void initialize() {
        // get drop down menu with music genre
        cbbCategory.getItems().addAll(
                "Pop", "Rock", "Jazz", "Classical", "Hip-Hop",
                "Country", "Reggae", "Electronic", "Blues", "Folk"
        );
        cbbCategory.setEditable(true);

        // Add listener for address
        txtTitle.textProperty().addListener((_, _, newValue) ->
                txtFile.setText("Music/" + newValue));
    }

    private void displayError(Throwable t) {
        // showing an error message if something went wrong
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @FXML
    public void onEdit() throws Exception {
        MyTunes selectedSong = myTunesController.getSelectedSong();

        // gets all song information into the edit dialog  - when saving it updates the info with list and database
        if (selectedSong != null) {
            selectedSong.setTitle(txtTitle.getText());
            selectedSong.setArtist(txtArtist.getText());
            selectedSong.setCategory(cbbCategory.getValue());
            selectedSong.setAddress(txtFile.getText());
            selectedSong.setTime(Integer.parseInt(txtTime.getText()));

            myTunesModel.updateSong(selectedSong);

            //MyTunesController.tableRefresh();
            if (myTunesController != null) {
                myTunesController.tableRefresh();
            }
        }

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onCancelButtonPressed() {
        // close the pop up window
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public void onMP3Pressed() {
        // adds the mp3 file type to the address, so that the user doesn't have to write it
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".wav")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".mp3";
        } else if (!currentText.toLowerCase().endsWith(".mp3")) {
            currentText += ".mp3";
        }
        txtFile.setText(currentText);
    }

    public void onWAVPressed() {
        // adds the wav file type to the address, so that the user doesn't have to write it
        String currentText = txtFile.getText();
        if (currentText.toLowerCase().endsWith(".mp3")) {
            currentText = currentText.substring(0, currentText.lastIndexOf('.')) + ".wav";
        } else if (!currentText.toLowerCase().endsWith(".wav")) {
            currentText += ".wav";
        }
        txtFile.setText(currentText);
    }
}