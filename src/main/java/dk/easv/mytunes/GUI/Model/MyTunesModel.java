package dk.easv.mytunes.GUI.Model;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BLL.MyTunesManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class MyTunesModel {

    private ObservableList<MyTunes> songsToBeViewed;

    private MyTunesManager myTunesManager;

    public MyTunesModel() throws Exception {
        myTunesManager = new MyTunesManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(myTunesManager.getAllSongs());
    }

    public ObservableList<MyTunes> getObservableSongs() {
        return songsToBeViewed;
    }

    public void searchSong(String query) throws Exception {
        List<MyTunes> searchResults = myTunesManager.searchSongs(query);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(searchResults);
    }

    public MyTunes createSong(MyTunes newSong) throws Exception {
        MyTunes songCreated = myTunesManager.createSong(newSong);
        songsToBeViewed.add(songCreated);
        System.out.println("Song added to ObservableList" + songCreated);
        return songCreated;
    }
    public void updateSong(MyTunes updatedSong) throws Exception {
        try {
            // Update song in DAL layer (through the layers)
            myTunesManager.updateSong(updatedSong);
            songsToBeViewed.add(updatedSong); // en fucking linje

            // Find the song in the observable list
            int index = songsToBeViewed.indexOf(updatedSong);

            if (index != -1) {
                // Song found, update its properties
                MyTunes s = songsToBeViewed.get(index);
                s.setTitle(updatedSong.getTitle());
                s.setArtist(updatedSong.getArtist());
                s.setCategory(updatedSong.getCategory());
                s.setAddress(updatedSong.getAddress());
                s.setTime(updatedSong.getTime());
            } else {
                // Song not found in the observable list
                throw new Exception("Song not found in the observable list.");
            }
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();

            // Optionally rethrow the exception to handle it at a higher level
            throw new Exception("Failed to update the song: " + e.getMessage(), e);
        }
    }


    public void deleteSong(MyTunes selectedSong) throws Exception {
        // delete song in DAL layer (through the layers)
        myTunesManager.deleteSong(selectedSong);

        // remove from obervable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }

    public void refreshSongs() throws Exception {
        songsToBeViewed.clear();
        songsToBeViewed.addAll(myTunesManager.getAllSongs());
    }
    public void createPlaylist(String playlistName, ObservableList<MyTunes> selectedSongs) throws Exception {
        myTunesManager.createPlaylist(playlistName, selectedSongs);
    }

    public ObservableList<MyTunes> getSongsForPlaylist(int playlistId) throws Exception {
        List<MyTunes> playlistSongs = myTunesManager.getSongsForPlaylist(playlistId);
        return FXCollections.observableArrayList(playlistSongs);
    }

    public ObservableList<MyTunes> getAllPlaylists() throws Exception {
        List<MyTunes> playlists = myTunesManager.getAllPlaylists();
        return FXCollections.observableArrayList(playlists);
    }
}
