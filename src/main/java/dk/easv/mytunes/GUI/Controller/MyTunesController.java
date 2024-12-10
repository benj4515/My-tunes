package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;
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
    public Button btnDeleteSongOnPlaylist;
    public Button btnDeletePlaylist;


    @FXML
    private TableView<Playlist> tblPlaylists;

    @FXML
    private TableColumn<Playlist, String> colName;

    @FXML
    private TableView<MyTunes> tblSongs;

    @FXML
    private TableColumn<MyTunes, String> colTitle;

    @FXML
    private TableColumn<MyTunes, String> colArtist;

    @FXML
    private TableColumn<MyTunes, String> colCategory;

    @FXML
    private TableColumn<MyTunes, Integer> colTime;

    @FXML
    private TableColumn<Playlist, Integer> colSongCount;

    @FXML
    private TableColumn<Playlist, String> colTotalTime;

    private MyTunesModel myTunesModel;

    @FXML
    private Button btnPlay;

    private MyTunes selectedSong;
    private MediaPlayer mediaPlayer;
    @FXML
    private Label lblPlayingSong;

    public MyTunesController() {

        // makes new mytunesmodel object
        try {
            myTunesModel = new MyTunesModel();
        } catch (Exception e) {
            displayError(e);
            e.printStackTrace();
        }
    }

    private void displayError(Throwable t) {

        // displays if any error occours
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

        // Custom cell value factory for colTime to display minutes and seconds
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colTime.setCellFactory(column -> new TableCell<MyTunes, Integer>() {
            @Override
            protected void updateItem(Integer timeInSeconds, boolean empty) {
                super.updateItem(timeInSeconds, empty);
                if (empty || timeInSeconds == null) {
                    setText(null);
                } else {
                    int minutes = timeInSeconds / 60;
                    int seconds = timeInSeconds % 60;
                    setText(String.format("%d:%02d", minutes, seconds));
                }
            }
        });

        // setup columns in tableview
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // New column for song count
        colSongCount.setCellValueFactory(cellData -> {
            Playlist playlist = cellData.getValue();
            try {
                return new SimpleIntegerProperty(myTunesModel.getSongsForPlaylist(playlist).size()).asObject();
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        // New column for total time
        colTotalTime.setCellValueFactory(cellData -> {
            Playlist playlist = cellData.getValue();
            try {
                int totalSeconds = myTunesModel.getSongsForPlaylist(playlist).stream().mapToInt(MyTunes::getTime).sum();
                int hours = totalSeconds / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;
                return new SimpleStringProperty(String.format("%d:%02d:%02d", hours, minutes, seconds));
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("0:00:00");
            }
        });

        try {
            tblPlaylists.setItems(myTunesModel.getAllPlaylists());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // connect tableview + listview to the ObservableList
        tblSongs.setItems(myTunesModel.getObservableSongs());

        // table view listener (when user selects a song in the tableview)
        tblSongs.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                selectedSong = newValue; // Set the selected song
                System.out.println("Selected song: " + selectedSong.getAddress());
                lblPlayingSong.setText("Selected: " + selectedSong.getTitle());
            }
        });

        txtSearch.textProperty().addListener((_, _, newValue) -> {
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
        sldSongSlider.valueProperty().addListener((_, _, newValue) -> {
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

        tblPlaylists.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                try {
                    lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(newValue));
                    lstSongsOnList.getSelectionModel().selectedItemProperty().addListener((_, _, newSong) -> {
                        if (newSong != null) {
                            selectedSong = (MyTunes) newSong; // Set the selected song from the playlist
                            System.out.println("Selected song from playlist: " + selectedSong.getAddress());
                            lblPlayingSong.setText("Selected: " + selectedSong.getTitle());
                        }
                    });
                } catch (Exception e) {
                    displayError(e);
                }
            } else {
                lstSongsOnList.setItems(FXCollections.observableArrayList()); // Clear the list when no playlist is selected
            }
        });
    }

    public MyTunes getSelectedSong() {
        return selectedSong;
    }

    @FXML
    private void onPlayButtonPressed() {
        //runs the playsong method of the selected song and change the label of the play pause button
        if (selectedSong != null) {
            if (mediaPlayer == null || !mediaPlayer.getMedia().getSource().equals(new File("src/main/resources/" + selectedSong.getAddress()).toURI().toString())) {
                playSong();
                btnPlay.setText("Pause");
            } else {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    btnPlay.setText("Play");
                } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.STOPPED) {
                    mediaPlayer.play();
                    btnPlay.setText("Pause");
                }
            }
        } else {
            System.out.println("No song selected");
            lblPlayingSong.setText("No song selected");
        }
    }

    private void playSong() {

        //this is the method that plays the actual song with the java media player - it checks whether the song is playing and should be paused, start a new song or continue playing the previous song
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
                mediaPlayer.currentTimeProperty().addListener((_, _, newValue) -> {
                    if (!sldSongSlider.isValueChanging()) {
                        sldSongSlider.setValue(newValue.toSeconds());
                    }
                });
                mediaPlayer.setOnEndOfMedia(() -> {
                    if (lstSongsOnList.getSelectionModel().getSelectedItem() != null) {
                        int currentIndex = lstSongsOnList.getSelectionModel().getSelectedIndex();
                        if (currentIndex < lstSongsOnList.getItems().size() - 1) {
                            lstSongsOnList.getSelectionModel().selectNext();
                        } else {
                            lstSongsOnList.getSelectionModel().selectFirst();
                        }
                        selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
                    } else {
                        int currentIndex = tblSongs.getSelectionModel().getSelectedIndex();
                        if (currentIndex < tblSongs.getItems().size() - 1) {
                            tblSongs.getSelectionModel().selectNext();
                        } else {
                            tblSongs.getSelectionModel().selectFirst();
                        }
                        selectedSong = tblSongs.getSelectionModel().getSelectedItem();
                    }
                    playSong();
                    btnPlay.setText("Pause");
                });
                mediaPlayer.play();
                btnPlay.setText("Pause");
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
    private void onLastButtonPressed() {
        // this button takes the selected song id and goes to the previous song on the list
        if (lstSongsOnList.getSelectionModel().getSelectedItem() != null) {
            int currentIndex = lstSongsOnList.getSelectionModel().getSelectedIndex();
            if (currentIndex > 0) {
                lstSongsOnList.getSelectionModel().selectPrevious();
            } else {
                lstSongsOnList.getSelectionModel().selectLast();
            }
            selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        } else {
            int currentIndex = tblSongs.getSelectionModel().getSelectedIndex();
            if (currentIndex > 0) {
                tblSongs.getSelectionModel().selectPrevious();
            } else {
                tblSongs.getSelectionModel().selectLast();
            }
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        }
        playSong();
    }

    @FXML
    private void onNextButtonPressed() {
        // this button takes the selected song id and goes to the next song on the list
        if (lstSongsOnList.getSelectionModel().getSelectedItem() != null) {
            int currentIndex = lstSongsOnList.getSelectionModel().getSelectedIndex();
            if (currentIndex < lstSongsOnList.getItems().size() - 1) {
                lstSongsOnList.getSelectionModel().selectNext();
            } else {
                lstSongsOnList.getSelectionModel().selectFirst();
            }
            selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        } else {
            int currentIndex = tblSongs.getSelectionModel().getSelectedIndex();
            if (currentIndex < tblSongs.getItems().size() - 1) {
                tblSongs.getSelectionModel().selectNext();
            } else {
                tblSongs.getSelectionModel().selectFirst();
            }
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        }
        playSong();
    }

    @FXML
    private void onNewSongButtonPressed() throws IOException {
        // this button makes a new stage where a new windows pops up where you need to fullfill it with the necessary info
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
    private void onEditSongButtonPressed() throws Exception {
        // this button makes a new stage where a new windows pops up where you can edit the selected songs info
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
    private void onDeleteSongButtonPressed() throws Exception {
        // Show a confirmation dialog
        int answer = JOptionPane.showConfirmDialog(null, "Are you sure?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            // Get the selected song
            MyTunes selectedSong = tblSongs.getSelectionModel().getSelectedItem();

            if (selectedSong != null) {
                // Delete song in DAL layer (through the layers)
                myTunesModel.deleteSong(selectedSong);
                System.out.println("Song deleted successfully.");
            } else {
                // Inform the user that no song was selected
                JOptionPane.showMessageDialog(null, "No song selected. Please select a song to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void tableRefresh() {
        // this method updates the tableview with songs with latest dataset
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
        // this method updates the tableview with playlists with latest dataset
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem(); // Store the currently selected playlist
        try {
            tblPlaylists.setItems(myTunesModel.getAllPlaylists());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (selectedPlaylist != null) {
            tblPlaylists.getSelectionModel().select(selectedPlaylist); // Reselect the previously selected playlist
        }
    }

    @FXML
    private void onNewPlaylistPressed() throws IOException {
        // this makes a new windows where you can make a new playlist and add songs to the playlist
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


    @FXML
    private void onDeleteSongOnPlaylistPressed() {
        // this button deletes the selected song from the selected playlist
        MyTunes selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedSong != null && selectedPlaylist != null) {
            try {
                myTunesModel.deleteSongFromPlaylist(selectedSong, selectedPlaylist);
                lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(selectedPlaylist)); // Refresh the songs in the playlist
                refreshPlaylists(); // Refresh the playlist table
            } catch (Exception e) {
                displayError(e);
            }
        } else {
            System.out.println("No song or playlist selected");
        }
    }

    public void onDeletePlaylistPressed() throws Exception {

        // this button deletes the whole playlist
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null) {
            myTunesModel.deletePlaylist(selectedPlaylist);
            refreshPlaylists(); // Refresh the playlist table
        } else {
            System.out.println("No playlist selected");
        }
    }

    @FXML
    private void onEditPlaylistPressed() throws Exception {

        // this button makes a new window where you can edit the selected playlist add new songs or delete song from playlist
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/PlaylistView.fxml"));
            Parent root = fxmlLoader.load();

            PlaylistViewController playlistController = fxmlLoader.getController();
            playlistController.setMyTunesModel(myTunesModel);
            playlistController.setMyTunesController(this);
            playlistController.loadPlaylistData(selectedPlaylist, myTunesModel.getSongsForPlaylist(selectedPlaylist));

            Stage stage = new Stage();
            stage.setTitle("Edit Playlist");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } else {
            System.out.println("No playlist selected");
        }
    }

    @FXML
    public void onMoveSongUpPressed() {
        // this button edits the order of songs in the playlist, so that the selected song goes one up in the song order when pressed
        MyTunes selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedSong != null && selectedPlaylist != null) {
            try {
                myTunesModel.moveSongUpInPlaylist(selectedSong, selectedPlaylist);
                lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(selectedPlaylist)); // Refresh the songs in the playlist
                refreshPlaylists(); // Refresh the playlist table
            } catch (Exception e) {
                displayError(e);
            }
        } else {
            System.out.println("No song or playlist selected");
        }
    }

    @FXML
    public void onMoveSongDownPressed() {
        // this button edits the order of songs in the playlist, so that the selected song goes one down in the song order when pressed
        MyTunes selectedSong = (MyTunes) lstSongsOnList.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedSong != null && selectedPlaylist != null) {
            try {
                myTunesModel.moveSongDownInPlaylist(selectedSong, selectedPlaylist);
                lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(selectedPlaylist)); // Refresh the songs in the playlist
                refreshPlaylists(); // Refresh the playlist table
            } catch (Exception e) {
                displayError(e);
            }
        } else {
            System.out.println("No song or playlist selected");
        }
    }

    public void onMoveToPlaylistPressed() {
        // this button adds the selected song from tableview to the selected playlist
        MyTunes selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedSong != null && selectedPlaylist != null) {
            try {
                myTunesModel.addSongToPlaylist(selectedSong, selectedPlaylist);
                lstSongsOnList.setItems(myTunesModel.getSongsForPlaylist(selectedPlaylist)); // Refresh the songs in the playlist
                refreshPlaylists(); // Refresh the playlist table
            } catch (Exception e) {
                displayError(e);
            }
        } else {
            System.out.println("No song or playlist selected");
        }
    }
}