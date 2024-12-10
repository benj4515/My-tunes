package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.MyTunes;
import dk.easv.mytunes.BE.Playlist;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyTunesDAO implements ISongDataAccess {

    private final DBConnector dbConnector = new DBConnector();


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
            return new MyTunes(id, song.getTitle(), song.getArtist(), song.getCategory(), song.getAddress(), song.getTime());
        } catch (SQLException ex) {
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
        } catch (SQLException ex) {
            throw new Exception("Could not delete song", ex);
        }
    }

    public void createPlaylist(String playlistName, List<MyTunes> selectedSongs) throws Exception {
        String insertPlaylist = "INSERT INTO dbo.Playlist (PlaylistName) VALUES (?)";
        String insertPlaylistSongs = "INSERT INTO dbo.PlaylistSongs (PlaylistId, IdSong, SongName, Position) VALUES (?, ?, ?, ?)";

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
                int position = 1;
                for (MyTunes song : selectedSongs) {
                    stmt.setInt(1, playlistId);
                    stmt.setInt(2, song.getId());
                    stmt.setString(3, song.getTitle());
                    stmt.setInt(4, position++); // Set the position and increment it
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

    // MyTunesDAO.java
    public void deletePlaylist(Playlist playlist) throws Exception {
        String deletePlaylistSongs = "DELETE FROM dbo.PlaylistSongs WHERE PlaylistId = ?";
        String deletePlaylist = "DELETE FROM dbo.Playlist WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(deletePlaylistSongs)) {
                stmt.setInt(1, playlist.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(deletePlaylist)) {
                stmt.setInt(1, playlist.getId());
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not delete playlist", ex);
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
        String query = "SELECT s.Id, s.Title, s.Artist, s.Category, s.Address, s.Time, ps.Position " +
                "FROM dbo.Songs s " +
                "JOIN dbo.PlaylistSongs ps ON s.Id = ps.IdSong " +
                "WHERE ps.PlaylistId = ? " +
                "ORDER BY ps.Position";
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

    public void addSongToPlaylist(MyTunes song, Playlist playlist) throws Exception {
        String checkSongExistsSql = "SELECT COUNT(*) FROM dbo.PlaylistSongs WHERE PlaylistId = ? AND IdSong = ?";
        String insertSongSql = "INSERT INTO dbo.PlaylistSongs (PlaylistId, IdSong, SongName, Position) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSongExistsSql)) {
                checkStmt.setInt(1, playlist.getId());
                checkStmt.setInt(2, song.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new Exception("Song already exists in the playlist");
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSongSql)) {
                insertStmt.setInt(1, playlist.getId());
                insertStmt.setInt(2, song.getId());
                insertStmt.setString(3, song.getTitle());
                insertStmt.setInt(4, getNextPosition(playlist.getId())); // Set the next position
                insertStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not add song to playlist", ex);
        }
    }

    private int getNextPosition(int playlistId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(Position), 0) + 1 AS NextPosition FROM dbo.PlaylistSongs WHERE PlaylistId = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("NextPosition");
                }
            }
        }
        return 1; // Default to 1 if no songs are in the playlist
    }

    public void deleteSongFromPlaylist(MyTunes song, int playlistId) throws Exception {
        String sql = "DELETE FROM dbo.PlaylistSongs WHERE IdSong = ? AND PlaylistId = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, song.getId());
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not delete song from playlist", ex);
        }
    }

    public void moveSongUpInPlaylist(MyTunes song, int playlistId) throws Exception {
        String getCurrentPositionSql = "SELECT Position FROM dbo.PlaylistSongs WHERE IdSong = ? AND PlaylistId = ?";
        String getAdjacentSongSql = "SELECT IdSong FROM dbo.PlaylistSongs WHERE PlaylistId = ? AND Position = ?";
        String updatePositionSql = "UPDATE dbo.PlaylistSongs SET Position = ? WHERE IdSong = ? AND PlaylistId = ?";

        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false);

            int currentPosition;
            try (PreparedStatement stmt = conn.prepareStatement(getCurrentPositionSql)) {
                stmt.setInt(1, song.getId());
                stmt.setInt(2, playlistId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentPosition = rs.getInt("Position");
                    } else {
                        throw new SQLException("Song not found in playlist");
                    }
                }
            }

            int newPosition = currentPosition - 1;
            if (newPosition < 1) {
                conn.rollback();
                return; // Already at the top
            }

            int adjacentSongId;
            try (PreparedStatement stmt = conn.prepareStatement(getAdjacentSongSql)) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, newPosition);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        adjacentSongId = rs.getInt("IdSong");
                    } else {
                        conn.rollback();
                        return; // No adjacent song found
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(updatePositionSql)) {
                stmt.setInt(1, newPosition);
                stmt.setInt(2, song.getId());
                stmt.setInt(3, playlistId);
                stmt.executeUpdate();

                stmt.setInt(1, currentPosition);
                stmt.setInt(2, adjacentSongId);
                stmt.setInt(3, playlistId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not move song up in playlist", ex);
        }
    }

    public void moveSongDownInPlaylist(MyTunes song, int playlistId) throws Exception {
        String getCurrentPositionSql = "SELECT Position FROM dbo.PlaylistSongs WHERE IdSong = ? AND PlaylistId = ?";
        String getAdjacentSongSql = "SELECT IdSong FROM dbo.PlaylistSongs WHERE PlaylistId = ? AND Position = ?";
        String updatePositionSql = "UPDATE dbo.PlaylistSongs SET Position = ? WHERE IdSong = ? AND PlaylistId = ?";

        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false);

            int currentPosition;
            try (PreparedStatement stmt = conn.prepareStatement(getCurrentPositionSql)) {
                stmt.setInt(1, song.getId());
                stmt.setInt(2, playlistId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentPosition = rs.getInt("Position");
                    } else {
                        throw new SQLException("Song not found in playlist");
                    }
                }
            }

            int newPosition = currentPosition + 1;

            int adjacentSongId;
            try (PreparedStatement stmt = conn.prepareStatement(getAdjacentSongSql)) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, newPosition);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        adjacentSongId = rs.getInt("IdSong");
                    } else {
                        conn.rollback();
                        return; // No adjacent song found
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(updatePositionSql)) {
                stmt.setInt(1, newPosition);
                stmt.setInt(2, song.getId());
                stmt.setInt(3, playlistId);
                stmt.executeUpdate();

                stmt.setInt(1, currentPosition);
                stmt.setInt(2, adjacentSongId);
                stmt.setInt(3, playlistId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not move song down in playlist", ex);
        }
    }

    public void updatePlaylist(Playlist playlist, ObservableList<MyTunes> songs) throws SQLException {
        String updatePlaylistSql = "UPDATE dbo.Playlist SET PlaylistName = ? WHERE Id = ?";
        String deletePlaylistSongsSql = "DELETE FROM dbo.PlaylistSongs WHERE PlaylistId = ?";
        String insertPlaylistSongsSql = "INSERT INTO dbo.PlaylistSongs (PlaylistId, IdSong, SongName, Position) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Update playlist name
            try (PreparedStatement stmt = conn.prepareStatement(updatePlaylistSql)) {
                stmt.setString(1, playlist.getName());
                stmt.setInt(2, playlist.getId());
                stmt.executeUpdate();
            }

            // Delete existing songs in the playlist
            try (PreparedStatement stmt = conn.prepareStatement(deletePlaylistSongsSql)) {
                stmt.setInt(1, playlist.getId());
                stmt.executeUpdate();
            }

            // Insert new songs into the playlist
            try (PreparedStatement stmt = conn.prepareStatement(insertPlaylistSongsSql)) {
                int position = 1;
                for (MyTunes song : songs) {
                    stmt.setInt(1, playlist.getId());
                    stmt.setInt(2, song.getId());
                    stmt.setString(3, song.getTitle());
                    stmt.setInt(4, position++);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new SQLException("Could not update playlist", ex);
        }
    }
}
