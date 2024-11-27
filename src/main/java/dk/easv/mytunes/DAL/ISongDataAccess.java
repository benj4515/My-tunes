package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;

import java.util.List;

public interface ISongDataAccess {

    List<MyTunes> getAllSongs() throws Exception;
}
