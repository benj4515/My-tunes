package dk.easv.mytunes.BLL.Util;

import dk.easv.mytunes.BE.MyTunes;

import java.util.ArrayList;
import java.util.List;

public class SongSearcher {
    public List<MyTunes> search(List<MyTunes> searchBase, String query) {
        // this method makes a query in the search field in the fxml. Then it compares the input with the values in the tableview
        List<MyTunes> searchResult = new ArrayList<>();

        for (MyTunes song : searchBase) {
            if (compareToSongTitle(query, song) || compareToSongArtist(query, song)) {
                searchResult.add(song);
            }
        }

        return searchResult;
    }

    private boolean compareToSongArtist(String query, MyTunes song) {
        // this method compares the searchbar with the artist values
        return song.getArtist().toLowerCase().contains(query.toLowerCase());
    }

    private boolean compareToSongTitle(String query, MyTunes song) {
        // this method compares the searchbar with the artist values
        return song.getTitle().toLowerCase().contains(query.toLowerCase());
    }
}
