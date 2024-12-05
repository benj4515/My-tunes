package dk.easv.mytunes.BLL.Util;

import dk.easv.mytunes.BE.MyTunes;

import java.util.ArrayList;
import java.util.List;

public class SongSearcher {
    public List<MyTunes> search(List<MyTunes> searchBase, String query) {
        List<MyTunes> searchResult = new ArrayList<>();

        for (MyTunes song : searchBase) {
            if (compareToSongTitle(query, song) || compareToSongArtist(query, song))
            {
                searchResult.add(song);
            }
        }

        return searchResult;
    }

    private boolean compareToSongArtist(String query, MyTunes song) {
        return song.getArtist().toLowerCase().contains(query.toLowerCase());
    }
    private boolean compareToSongTitle(String query, MyTunes song) {
        return song.getTitle().toLowerCase().contains(query.toLowerCase());
    }
}
