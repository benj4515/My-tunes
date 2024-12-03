package dk.easv.mytunes.GUI.Model;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BLL.MyTunesManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public MyTunes createSong(MyTunes newSong) throws Exception {
        MyTunes songCreated = myTunesManager.createSong(newSong);
        songsToBeViewed.add(songCreated);
        System.out.println("Song added to ObservableList" + songCreated);
        return songCreated;
    }
    public void refreshSongs() throws Exception {
        songsToBeViewed.clear();
        songsToBeViewed.addAll(myTunesManager.getAllSongs());
    }
}
