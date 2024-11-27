package dk.easv.mytunes.BLL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.DAL.ISongDataAccess;
import dk.easv.mytunes.DAL.MyTunesDAO;

import java.io.IOException;
import java.util.List;

public class MyTunesManager {

    private ISongDataAccess songsDAO;

    public MyTunesManager() throws IOException {
        songsDAO = new MyTunesDAO();
    }

    public List<MyTunes> getAllSongs() throws Exception {
        return songsDAO.getAllSongs();
    }
}
