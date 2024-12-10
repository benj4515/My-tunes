package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;

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

    @Override
    public void updateSong(MyTunes song) throws Exception {
        String sql = "UPDATE dbo.Songs SET Title = ?, Artist = ?, Category = ?, Address = ?, Time = ? WHERE ID = ?";

        try (Connection conn = dbConnector.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getCategory());
            stmt.setString(4, song.getAddress());
            stmt.setInt(5, song.getTime());
            stmt.setInt(6, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not get movies from database.", ex);
        }
    }

    public void deleteSong(MyTunes song) throws Exception {
        String sql = "DELETE FROM dbo.Songs WHERE ID = ?" +
                " DELETE FROM dbo.PlaylistSongs WHERE IdSong = ?";
        try (Connection conn = dbConnector.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, song.getId());
            stmt.setInt(2, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        } catch (SQLException ex){
            throw new Exception("Could not delete song", ex);
        }
    }
    public void createPlaylist(String playlistName, List<MyTunes> selectedSongs) throws Exception {
        String insertPlaylist = "INSERT INTO dbo.Playlist (PlaylistName) VALUES (?)";
        String insertPlaylistSongs = "INSERT INTO dbo.PlaylistSongs (PlaylistId, IdSong, SongName) VALUES (?, ?, ?)";

        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            int playlistId;
            try (PreparedStatement stmt = conn.prepareStatement(insertPlaylist, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, playlistName);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        playlistId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated Playlist ID.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertPlaylistSongs)) {
                for (MyTunes song : selectedSongs) {
                    stmt.setInt(1, playlistId);
                    stmt.setInt(2, song.getId());
                    stmt.setString(3, song.getTitle());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not create playlist", ex);
        }
    }

    public List<Playlist> getAllPlaylists() throws Exception {
        String query = "SELECT * FROM dbo.Playlist";
        List<Playlist> playlists = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("PlaylistName");
                playlists.add(new Playlist(id, name)); // Adjust constructor if needed
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not fetch playlists", ex);
        }
        return playlists;
    }

    public List<MyTunes> getSongsForPlaylist(int playlistId) throws Exception {
        String query = "SELECT s.Id, s.Title, s.Artist, s.Category, s.Address, s.Time " +
                "FROM dbo.Songs s " +
                "JOIN dbo.PlaylistSongs ps ON s.Id = ps.IdSong " +
                "WHERE ps.PlaylistId = ?";
        List<MyTunes> songs = new ArrayList<>();

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String title = rs.getString("Title");
                    String artist = rs.getString("Artist");
                    String category = rs.getString("Category");
                    String address = rs.getString("Address");
                    int time = rs.getInt("Time");

                    songs.add(new MyTunes(id, title, artist, category, address, time));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not fetch songs for playlist", ex);
        }
        return songs;
    }

}
