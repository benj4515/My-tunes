package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.DAL.ISongDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyTunesDAO implements ISongDataAccess {

    private DBConnector dbConnector = new DBConnector();


    public MyTunesDAO() throws IOException {
    }

    @Override
    public List<MyTunes> getAllSongs() throws Exception {

        ArrayList<MyTunes> allSongs = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM dbo.Songs;";
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {

                int id = rs.getInt("Id");
                String title = rs.getString("Title");
                String artist = rs.getString("Artist");
                String category = rs.getString("Category");
                String address = rs.getString("Address");

                MyTunes song = new MyTunes(id, title, artist, category, address);
                allSongs.add(song);
            }
            return allSongs;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get songs from database", ex);
        }

    }
}
