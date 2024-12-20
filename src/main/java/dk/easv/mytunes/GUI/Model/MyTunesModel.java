package dk.easv.mytunes.GUI.Model;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BLL.MyTunesManager;
import dk.easv.mytunes.DAL.MyTunesDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class MyTunesModel {

    private final ObservableList<MyTunes> songsToBeViewed;

    private final MyTunesManager myTunesManager;

    private final MyTunesDAO myTunesDAO = new MyTunesDAO();

    public MyTunesModel() throws Exception {
        myTunesManager = new MyTunesManager();
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(myTunesManager.getAllSongs());
    }

    public ObservableList<MyTunes> getObservableSongs() {
        return songsToBeViewed;
    }

    public void searchSong(String query) throws Exception {
        // this method search for songs with the method from the mytunesManager
        List<MyTunes> searchResults = myTunesManager.searchSongs(query);
        songsToBeViewed.clear();
        songsToBeViewed.addAll(searchResults);
    }

    public void createSong(MyTunes newSong) throws Exception {
        // this method creates song with the method from myTunesManager
        MyTunes songCreated = myTunesManager.createSong(newSong);
        songsToBeViewed.add(songCreated);
        System.out.println("Song added to ObservableList" + songCreated);
    }

    public void updateSong(MyTunes updatedSong) throws Exception {
        try {
            // Update song in DAL layer (through the layers)
            myTunesManager.updateSong(updatedSong);
            songsToBeViewed.add(updatedSong);

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

        // remove from observable list (and UI)
        songsToBeViewed.remove(selectedSong);
    }

    public void refreshSongs() throws Exception {
        // this refreshes the tableview with the method from myTunesManager
        songsToBeViewed.clear();
        songsToBeViewed.addAll(myTunesManager.getAllSongs());
    }

    public void createPlaylist(String playlistName, ObservableList<MyTunes> selectedSongs) throws Exception {
        // this method creates playlist with the method from myTunesManager
        myTunesManager.createPlaylist(playlistName, selectedSongs);
    }

    public ObservableList<MyTunes> getSongsForPlaylist(Playlist playlist) throws Exception {
        // this method collects the songs for playlist with the method from myTunesManager
        List<MyTunes> songs = myTunesManager.getSongsForPlaylist(playlist.getId());
        return FXCollections.observableArrayList(songs);
    }

    public ObservableList<Playlist> getAllPlaylists() throws Exception {
        // this method collects all playlists with the method from myTunesManager
        List<Playlist> playlists = myTunesManager.getAllPlaylists();
        return FXCollections.observableArrayList(playlists);
    }

    public void deletePlaylist(Playlist playlist) throws Exception {
        // this method deletes playlist from the dal layer with the method from myTunesDAO
        myTunesDAO.deletePlaylist(playlist);
    }

    public void addSongToPlaylist(MyTunes song, Playlist playlist) throws Exception {
        // this method adds songs to playlist in the dal layer through the method in myTunesDAO
        myTunesDAO.addSongToPlaylist(song, playlist);
    }

    public void deleteSongFromPlaylist(MyTunes song, Playlist playlist) throws Exception {
        // this method deletes songs from playlist through the method in myTunesDAO
        myTunesDAO.deleteSongFromPlaylist(song, playlist.getId());
    }

    // MyTunesModel.java
    public void moveSongUpInPlaylist(MyTunes song, Playlist playlist) throws Exception {
        // this method moves the order of the selected song one field/number up through the method in myTunesDAO
        myTunesDAO.moveSongUpInPlaylist(song, playlist.getId());
    }

    public void moveSongDownInPlaylist(MyTunes song, Playlist playlist) throws Exception {
        // this method moves the order of the selected song one field/number down through the method in myTunesDAO
        myTunesDAO.moveSongDownInPlaylist(song, playlist.getId());
    }

    public Playlist getPlaylistById(int id) throws Exception {
        // this method gets the playlist through its id number into a readable list for the gui. it does this with a method from myTunesManager
        List<Playlist> playlists = myTunesManager.getAllPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getId() == id) {
                return playlist;
            }
        }
        return null;
    }

    public void updatePlaylist(Playlist playlist, ObservableList<MyTunes> songs) throws Exception {
        // this method updates the view of the content of a playlist with a method from myTubesManager
        myTunesManager.updatePlaylist(playlist, songs);
    }
}
