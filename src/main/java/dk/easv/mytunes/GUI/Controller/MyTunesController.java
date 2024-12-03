package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MyTunesController implements Initializable {

    @FXML
    public Button btnNewSong;

    @FXML
    private TableView<MyTunes> tblSongs;

    @FXML
    private TableColumn<MyTunes, String> colTitle;

    @FXML
    private TableColumn<MyTunes, String> colArtist;

    @FXML
    private TableColumn<MyTunes, String> colCategory;

    @FXML
    private TableColumn<MyTunes, String> colTime;


    private MyTunesModel myTunesModel;

    @FXML
    private MediaView mediaViewThing;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnLast;
    @FXML
    private Button btnNext;

    private MyTunes selectedSong;
    private MediaPlayer mediaPlayer;
    @FXML
    private Label lblPlayingSong;

    public MyTunesController() {

        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    private void displayError(Throwable t) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(t.getMessage());
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // setup columns in tableview
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        // connect tableview + listview to the ObservableList
        tblSongs.setItems(myTunesModel.getObservableSongs());

        // table view listener (when user selects a song in the tableview)
        tblSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedSong = newValue; // Set the selected song
                System.out.println("Selected song: " + selectedSong.getAddress());
                lblPlayingSong.setText("Selected: " + selectedSong.getTitle());
            }
        });
    }

    @FXML
    private void onPlayButtonPressed(ActionEvent actionEvent) {
        if (selectedSong != null) {
            if (mediaPlayer == null || !mediaPlayer.getMedia().getSource().equals(new File("src/main/resources/" + selectedSong.getAddress()).toURI().toString())) {
                playSong();
            } else {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED) {
                    mediaPlayer.play();
                }
            }
        } else {
            System.out.println("No song selected");
            lblPlayingSong.setText("No song selected");
        }
    }

    private void playSong() {
        if (selectedSong != null) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                Media pick = new Media(new File("src/main/resources/" + selectedSong.getAddress()).toURI().toString());
                mediaPlayer = new MediaPlayer(pick);
                mediaPlayer.play();
                System.out.println("Playing music: " + selectedSong.getTitle());
                lblPlayingSong.setText("Playing: " + selectedSong.getTitle());
            } catch (MediaException e) {
                displayError(e);
                System.out.println("Error playing media: " + e.getMessage());
                lblPlayingSong.setText("Error playing media");
            }
        } else {
            System.out.println("No song selected");
            lblPlayingSong.setText("No song selected");
        }
    }

    @FXML
    private void onLastButtonPressed(ActionEvent actionEvent) {
        tblSongs.getSelectionModel().selectPrevious();
        playSong();
    }

    @FXML
    private void onNextButtonPressed(ActionEvent actionEvent) {
        tblSongs.getSelectionModel().selectNext();
        playSong();
    }

    @FXML
    private void onNewSongButtonPressed(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/NewSongView.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setTitle("New/Edit Song");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // Optional: makes the new window modal
        stage.show();
    }

    public void tableRefresh(){
        tblSongs.refresh();
    }
}