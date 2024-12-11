package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class PlaylistViewController {

    @FXML
    private TextField playlistNameField;
    @FXML
    private TableView<MyTunes> tblAvailableSongs;
    @FXML
    private TableColumn<MyTunes, String> colTitle;
    @FXML
    private TableColumn<MyTunes, String> colArtist;
    @FXML
    private TableColumn<MyTunes, String> colCategory;
    @FXML
    private ListView<MyTunes> lstSelectedSongs;

    private final ObservableList<MyTunes> availableSongs = FXCollections.observableArrayList();
    private final ObservableList<MyTunes> selectedSongs = FXCollections.observableArrayList();

    private MyTunesModel myTunesModel;
    private MyTunesController myTunesController;
    private int currentPlaylistId;

    public void setMyTunesModel(MyTunesModel model) {
        this.myTunesModel = model;
        loadAvailableSongs();
    }

    public void loadPlaylistData(Playlist playlist, ObservableList<MyTunes> songs) {
        // this method
        playlistNameField.setText(playlist.getName());
        selectedSongs.setAll(songs);
        currentPlaylistId = playlist.getId();
    }

    public void setMyTunesController(MyTunesController myTunesController) {
        this.myTunesController = myTunesController;
    }

    @FXML
    public void initialize() {
        // this method sets the value of the textfields in the window
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        tblAvailableSongs.setItems(availableSongs);
        lstSelectedSongs.setItems(selectedSongs);
    }

    private void loadAvailableSongs() {
        // This method loads all available songs from the songs list
        try {
            availableSongs.setAll(myTunesModel.getObservableSongs());
        } catch (Exception e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSong() {
        // this button adds the selected song from available song to the made or selected playlist
        MyTunes selectedSong = tblAvailableSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            boolean songExists = selectedSongs.stream()
                    .anyMatch(song -> song.getId() == selectedSong.getId());
            if (!songExists) {
                selectedSongs.add(selectedSong);
            }
        }
    }

    @FXML
    private void onRemoveSong() {
        // this button removes the selected song from the selected playlist
        MyTunes selectedSong = lstSelectedSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            selectedSongs.remove(selectedSong);
        }
    }

    @FXML
    private void onSavePlaylist() {
        // this button saves the collection of the playlist to the list and database if new it makes a new playlist if existing it updates with new dataset
        String playlistName = playlistNameField.getText();
        if (playlistName.isEmpty()) {
            showError("Playlist name is required!");
            return;
        }

        try {
            Playlist existingPlaylist = myTunesModel.getPlaylistById(currentPlaylistId);
            if (existingPlaylist != null) {
                // Update the existing playlist
                existingPlaylist.setName(playlistName);
                myTunesModel.updatePlaylist(existingPlaylist, selectedSongs);
            } else {
                // Create a new playlist
                myTunesModel.createPlaylist(playlistName, selectedSongs);
            }
            myTunesController.refreshPlaylists();
            closeWindow();
        } catch (Exception e) {
            showError("Error saving playlist: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        // this closes the window
        Stage stage = (Stage) playlistNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        // this shows error if any occurs
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
