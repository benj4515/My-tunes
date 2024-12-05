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

import java.net.URL;
import java.util.ResourceBundle;

public class EditSongController {

    @FXML
    private Button btnSave, btnWaV, btnMP3, btnChooseFile, btnCategoryMore;

    @FXML
    private Button btnCancel;

    @FXML
    public ComboBox<String> cbbCategory;

    @FXML
    public TextField txtTime, txtFile, txtArtist, txtTitle;

    private MyTunesModel myTunesModel;

    private MyTunesController myTunesController;

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

    public EditSongController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbbCategory.getItems().addAll(
                "Pop", "Rock", "Jazz", "Classical", "Hip-Hop",
                "Country", "Reggae", "Electronic", "Blues", "Folk"
        );
        cbbCategory.setEditable(true);

        // Add listener
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
    public void onEdit(ActionEvent actionEvent) throws Exception {
        MyTunes selectedSong = myTunesController.getSelectedSong();

        if (selectedSong != null) {
            System.out.println("Shit fuck: " + selectedSong);
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
    public void onCancelButtonPressed(ActionEvent actionEvent) {
        System.out.printf(cbbCategory.getValue());
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