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
import javafx.stage.FileChooser;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.io.File;

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

        // the listener that updates txtFile based on txtTitle
        // txtTitle.textProperty().addListener((_, _, newValue) -> txtFile.setText("Music/" + newValue));
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
        // Getting data from UI
        String title = txtTitle.getText();
        String artist = txtArtist.getText();
        int time = Integer.parseInt(txtTime.getText());
        String address = txtFile.getText();
        String category = cbbCategory.getValue();

        if (title.isEmpty() || artist.isEmpty() || category.isEmpty() || address.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        // Use the full path of the selected file
        Path selectedFilePath = Path.of(address);
        System.out.println("Selected file path: " + selectedFilePath.toAbsolutePath());

        if (!Files.exists(selectedFilePath)) {
            throw new java.nio.file.NoSuchFileException(selectedFilePath.toString());
        }

        // Check if the file is in the src/main/resources/Music folder
        Path musicFolderPath = Path.of("src/main/resources/Music");
        Path destinationPath = musicFolderPath.resolve(selectedFilePath.getFileName());
        System.out.println("Destination path: " + destinationPath.toAbsolutePath());

        if (Files.exists(destinationPath)) {
            System.out.println("Song already exists.");
            displayError(new Exception("Song already exists."));
            return;
        }

        if (!selectedFilePath.startsWith(musicFolderPath)) {
            Files.copy(selectedFilePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            address = "Music/" + selectedFilePath.getFileName().toString();
        }

        // New song object
        MyTunes newSong = new MyTunes(-1, title, artist, category, address, time);

        // Call model to create song in the DAL
        myTunesModel.createSong(newSong);
        System.out.println("New song created: " + newSong);

        // Refresh the table
        if (myTunesController != null) {
            myTunesController.tableRefresh();
        }

        // Close the window
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

    @FXML
    public void onFilePickerPressed() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            txtFile.setText(selectedFile.getAbsolutePath()); // Store the full path
            try {
                AudioFile audioFile = AudioFileIO.read(selectedFile);
                Tag tag = audioFile.getTag();

                if (tag != null) {
                    txtTitle.setText(tag.getFirst(FieldKey.TITLE));
                    txtArtist.setText(tag.getFirst(FieldKey.ARTIST));
                    cbbCategory.setValue(tag.getFirst(FieldKey.GENRE));
                    txtTime.setText(String.valueOf(audioFile.getAudioHeader().getTrackLength()));
                }

                // Add file extension
                String fileName = selectedFile.getName().toLowerCase();
                if (fileName.endsWith(".mp3")) {
                    onMP3Pressed();
                } else if (fileName.endsWith(".wav")) {
                    onWAVPressed();
                }
            } catch (Exception e) {
                displayError(e);
            }
        }
    }
}