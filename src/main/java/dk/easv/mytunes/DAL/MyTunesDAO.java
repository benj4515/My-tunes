package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.DAL.ISongDataAccess;
import dk.easv.mytunes.GUI.Controller.MyTunesController;
import dk.easv.mytunes.GUI.Controller.NewSongController;

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
                int time = rs.getInt("Time");

                MyTunes song = new MyTunes(id, title, artist, category, address, time);
                allSongs.add(song);
            }
            return allSongs;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get songs from database", ex);
        }

    }

    @Override
    public MyTunes createSong(MyTunes song) throws Exception {
        String sql = "INSERT INTO dbo.Songs (Title, Artist, Category, Address, Time) VALUES ( ?, ?, ?, ?, ?)";
        DBConnector dbConnector = new DBConnector();

        try (Connection conn = dbConnector.getConnection()) {
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

             // Bind parameters
             stmt.setString(1, song.getTitle());
             stmt.setString(2, song.getArtist());
             stmt.setString(3, song.getCategory());
             stmt.setString(4, song.getAddress());
             stmt.setInt(5, song.getTime());

             // Run the specified SQL statement
             stmt.executeUpdate();

             // Get the generated ID from the DB
             ResultSet rs = stmt.getGeneratedKeys();
             int id = 0;

             if (rs.next()) {
                 id = rs.getInt(1);
             }

             // Create song object and send up the layers
             MyTunes createdSong = new MyTunes(id, song.getTitle(), song.getArtist(), song.getCategory(), song.getAddress(), song.getTime());

            return createdSong;
        } catch (SQLException ex){
            ex.printStackTrace();
            throw new Exception("Could not create song", ex);

        }

    }
}
