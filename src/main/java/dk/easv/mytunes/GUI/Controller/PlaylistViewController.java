package dk.easv.mytunes.GUI.Controller;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.GUI.Model.MyTunesModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

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

    private ObservableList<MyTunes> availableSongs = FXCollections.observableArrayList();
    private ObservableList<MyTunes> selectedSongs = FXCollections.observableArrayList();

    private MyTunesModel myTunesModel;

    public void setMyTunesModel(MyTunesModel model) {
        this.myTunesModel = model;
        loadAvailableSongs();
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        tblAvailableSongs.setItems(availableSongs);
        lstSelectedSongs.setItems(selectedSongs);
    }

    private void loadAvailableSongs() {
        try {
            availableSongs.setAll(myTunesModel.getObservableSongs());
        } catch (Exception e) {
            showError("Error loading songs: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSong() {
        MyTunes selectedSong = tblAvailableSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null && !selectedSongs.contains(selectedSong.getTitle())) {
            selectedSongs.add(selectedSong);
        }
    }

    @FXML
    private void onRemoveSong() {
        MyTunes selectedSong = lstSelectedSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            selectedSongs.remove(selectedSong);
        }
    }

    @FXML
    private void onSavePlaylist() {
        String playlistName = playlistNameField.getText();
        if (playlistName.isEmpty()) {
            showError("Playlist name is required!");
            return;
        }

        try {
            myTunesModel.createPlaylist(playlistName, selectedSongs);
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
        Stage stage = (Stage) playlistNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
