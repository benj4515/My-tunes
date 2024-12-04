package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;

import java.util.List;

public interface ISongDataAccess {

    List<MyTunes> getAllSongs() throws Exception;
    MyTunes createSong(MyTunes newSong) throws Exception;
    void deleteSong(MyTunes myTunes) throws Exception;
    void updateSong(MyTunes myTunes) throws Exception;
}
