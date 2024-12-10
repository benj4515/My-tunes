package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.collections.ObservableList;
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
import java.io.IOError;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MyTunesController implements Initializable {

    @FXML
    public Button btnNewSong;
    public Button btnEditSong;
    public TextField txtSearch;
    public Slider sldSongSlider;
    public ListView lstSongsOnList;
    public Button btnDeleteSong;

    @FXML
    private TableView<Playlist> tblPlaylists;

    @FXML
    private TableColumn<Playlist, String> colName;

    @FXML
    private TableColumn<Playlist, Integer> colSongCount;

    @FXML
    private TableColumn<Playlist, String> colTotalTime;

    private ObservableList<Playlist> playlists;

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

        // setup columns in tableview
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        try {
            tblPlaylists.setItems((myTunesModel.getAllPlaylists()));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        tblPlaylists.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(newValue));
                    lstSongsOnList.getSelectionModel().selectedItemProperty().addListener((obs, oldSong, newSong) -> {
                        if (newSong != null) {
                            selectedSong = (MyTunes) newSong; // Set the selected song from the playlist
                            System.out.println("Selected song from playlist: " + selectedSong.getAddress());
                            lblPlayingSong.setText("Selected: " + selectedSong.getTitle());
                        }
                    });
                } catch (Exception e) {
                    displayError(e);
                }
            }
        });

        tblPlaylists.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(newValue));
                } catch (Exception e) {
                    displayError(e);
                }
            }
        });

        txtSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                myTunesModel.searchSong(newValue);
            } catch (Exception e) {
                displayError(e);
                e.printStackTrace();
            }
        });

        // Disable the slider initially
        sldSongSlider.setDisable(true);

        // Add listener to the slider to seek the song
        sldSongSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (sldSongSlider.isValueChanging() && mediaPlayer != null) {
                // Convert slider value to seconds and seek the media player
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });

        // Add mouse click listener to the slider
        sldSongSlider.setOnMouseClicked(event -> {
            if (mediaPlayer != null) {
                // Calculate the new time based on the mouse click position
                double mouseX = event.getX();
                double sliderWidth = sldSongSlider.getWidth();
                double newTime = (mouseX / sliderWidth) * sldSongSlider.getMax();
                mediaPlayer.seek(Duration.seconds(newTime));
            }
        });
    }

    public MyTunes getSelectedSong() {
        return selectedSong;
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
                mediaPlayer.setOnReady(() -> {
                    sldSongSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
                    sldSongSlider.setDisable(false); // Enable the slider when the song is ready
                });
                mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                    if (!sldSongSlider.isValueChanging()) {
                        sldSongSlider.setValue(newValue.toSeconds());
                    }
                });
                mediaPlayer.setOnEndOfMedia(() -> {
                    onNextButtonPressed(null);
                });
                mediaPlayer.play();
                System.out.println("Playing music: " + selectedSong.getTitle());
                lblPlayingSong.setText("Playing: " + selectedSong.getTitle() + " by " + selectedSong.getArtist());
            } catch (MediaException e) {
                displayError(e);
                System.out.println("Error playing media: " + e.getMessage());
                lblPlayingSong.setText("Error playing media");
            }
        } else {
            System.out.println("No song selected");
            lblPlayingSong.setText("No song selected");
            sldSongSlider.setDisable(true); // Disable the slider when no song is selected
        }
    }

    @FXML
    private void onLastButtonPressed(ActionEvent actionEvent) {
        if (lstSongsOnList.getSelectionModel().getSelectedItem() != null) {
            lstSongsOnList.getSelectionModel().selectPrevious();
            selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        } else {
            tblSongs.getSelectionModel().selectPrevious();
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        }
        playSong();
    }

    @FXML
    private void onNextButtonPressed(ActionEvent actionEvent) {
        if (lstSongsOnList.getSelectionModel().getSelectedItem() != null) {
            lstSongsOnList.getSelectionModel().selectNext();
            selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        } else {
            tblSongs.getSelectionModel().selectNext();
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        }
        playSong();
    }

    @FXML
    private void onNewSongButtonPressed(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/NewSongView.fxml"));
        Parent root = fxmlLoader.load();

        // Get the controller of the new song view
        NewSongController newSongController = fxmlLoader.getController();
        newSongController.setMyTunesController(this); // Pass the current controller to the new controller

        Stage stage = new Stage();
        stage.setTitle("New Song");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // Optional: makes the new window modal
        stage.show();
    }

    @FXML
    private void onEditSongButtonPressed(ActionEvent actionEvent) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSongView.fxml"));
        Parent root = fxmlLoader.load();

        // Get the controller of the new song view
        EditSongController editSongController = fxmlLoader.getController();
        editSongController.setMyTunesController(this); // Pass the current controller to the new controller

        Stage stage = new Stage();
        stage.setTitle("Edit Song");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // Optional: makes the new window modal
        stage.show();
    }
        
    @FXML
    private void onDeleteSongButtonPressed(ActionEvent actionEvent) throws Exception {
        MyTunes selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null)
        {
            // Delete song in DAL layer (through the layers)
            myTunesModel.deleteSong(selectedSong);
        }
    }

    public void tableRefresh() {
        System.out.println("tableRefresh called");
        try {
            myTunesModel.refreshSongs();
        } catch (Exception e) {
            displayError(e);
        }
        ObservableList<MyTunes> currentSongs = myTunesModel.getObservableSongs();
        System.out.println("Number of songs: " + currentSongs.size());
        tblSongs.setItems(null); // Clear the table
        tblSongs.setItems(currentSongs); // Reset the items
        tblSongs.refresh(); // Refresh the table
    }

    public void refreshPlaylists() {
        try {
            tblPlaylists.setItems((myTunesModel.getAllPlaylists()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewPlaylistPressed(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/PlaylistView.fxml"));
        Parent root = fxmlLoader.load();

        PlaylistViewController playlistController = fxmlLoader.getController();
        playlistController.setMyTunesModel(myTunesModel);
        playlistController.setMyTunesController(this);

        Stage stage = new Stage();
        stage.setTitle("Create New Playlist");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }


    
}