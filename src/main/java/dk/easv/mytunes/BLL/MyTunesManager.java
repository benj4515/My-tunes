package dk.easv.mytunes.BLL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BLL.Util.SongSearcher;
import dk.easv.mytunes.DAL.ISongDataAccess;
import dk.easv.mytunes.DAL.MyTunesDAO;

import java.io.IOException;
import java.util.List;

public class MyTunesManager {

    private ISongDataAccess songsDAO;
    private SongSearcher songSearcher = new SongSearcher();

    public MyTunesManager() throws IOException {
        songsDAO = new MyTunesDAO();
    }

    public List<MyTunes> getAllSongs() throws Exception {
        return songsDAO.getAllSongs();
    }

    public List<MyTunes> searchSongs(String query) throws Exception {
        List<MyTunes> allSongs = getAllSongs();
        List<MyTunes> searchResult = songSearcher.search(allSongs, query);
        return searchResult;
    }

    public MyTunes createSong(MyTunes newSong) throws Exception {
        return songsDAO.createSong(newSong);
    }

    public void deleteSong(MyTunes selectedSong) throws Exception {
        songsDAO.deleteSong(selectedSong);
    }

    public void updateSong(MyTunes updatedSong) throws Exception {
        songsDAO.updateSong(updatedSong);
    }
    public void createPlaylist(String playlistName, List<MyTunes> selectedSongs) throws Exception {
        songsDAO.createPlaylist(playlistName, selectedSongs);
    }

    public List<MyTunes> getSongsForPlaylist(int playlistId) throws Exception {
        return songsDAO.getSongsForPlaylist(playlistId);
    }

    public List<Playlist> getAllPlaylists() throws Exception {
        return songsDAO.getAllPlaylists();
    }
}
