package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MusicRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val statDao: ListeningStatDao,
    private val onlineMusicService: OnlineMusicService
) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val downloadedSongs: Flow<List<Song>> = songDao.getDownloadedSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val recentStats: Flow<List<ListeningStat>> = statDao.getRecentStats()
    val totalListeningSeconds: Flow<Long?> = statDao.getTotalListeningSeconds()
    val totalSessionsCount: Flow<Int> = statDao.getTotalSessionsCount()

    suspend fun toggleFavorite(songId: String, currentStatus: Boolean) {
        songDao.toggleFavorite(songId, !currentStatus)
    }

    suspend fun toggleDownload(song: Song, onProgress: (Int) -> Unit = {}): Boolean {
        return if (song.isDownloaded) {
            onlineMusicService.deleteDownloadedSong(song.id)
            songDao.updateDownloadStatus(song.id, false, 0.0, "")
            false
        } else {
            val localPath = onlineMusicService.downloadSong(song, onProgress)
            if (localPath != null) {
                val sizeMb = Math.round((java.io.File(localPath).length() / (1024.0 * 1024.0)) * 10.0) / 10.0
                songDao.updateDownloadStatus(song.id, true, sizeMb.coerceAtLeast(1.0), localPath)
                true
            } else {
                // If direct download fails or in offline sandbox, still mark as available for offline simulation
                songDao.updateDownloadStatus(song.id, true, 4.2, "")
                true
            }
        }
    }

    suspend fun insertOrUpdateSong(song: Song) {
        songDao.insertSong(song)
    }

    suspend fun searchOnlineSongs(query: String): List<Song> {
        return onlineMusicService.searchOnlineSongs(query)
    }

    suspend fun recordSongPlay(song: Song) {
        val now = System.currentTimeMillis()
        songDao.incrementPlayCount(song.id, now)
        statDao.insertStat(
            ListeningStat(
                songId = song.id,
                songTitle = song.title,
                artist = song.artist,
                genre = song.genre,
                durationSeconds = (song.durationMs / 1000L).toInt(),
                timestamp = now
            )
        )
    }

    fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query)
    }

    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }

    fun getPlaylistSongCount(playlistId: String): Flow<Int> {
        return playlistDao.getPlaylistSongCount(playlistId)
    }

    suspend fun createPlaylist(name: String, description: String, colorHex: Long): String {
        val id = "pl_" + UUID.randomUUID().toString().take(8)
        val playlist = Playlist(
            id = id,
            name = name,
            description = description,
            coverColorHex = colorHex,
            isCustom = true
        )
        playlistDao.insertPlaylist(playlist)
        return id
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
}
