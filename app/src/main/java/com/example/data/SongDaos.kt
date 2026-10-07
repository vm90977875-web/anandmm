package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY playCount DESC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1")
    fun getDownloadedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET isDownloaded = :isDownloaded, downloadSizeMb = :sizeMb, localFilePath = :localFilePath WHERE id = :songId")
    suspend fun updateDownloadStatus(songId: String, isDownloaded: Boolean, sizeMb: Double, localFilePath: String)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, timestamp: Long)

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE genre = :genre")
    fun getSongsByGenre(genre: String): Flow<List<Song>>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref ref ON s.id = ref.songId
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.addedAt ASC
    """)
    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: String): Flow<Int>
}

@Dao
interface ListeningStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: ListeningStat)

    @Query("SELECT * FROM listening_stats ORDER BY timestamp DESC LIMIT 200")
    fun getRecentStats(): Flow<List<ListeningStat>>

    @Query("SELECT SUM(durationSeconds) FROM listening_stats")
    fun getTotalListeningSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM listening_stats")
    fun getTotalSessionsCount(): Flow<Int>
}
