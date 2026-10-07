package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val genre: String,
    val language: String,
    val lyricsLrc: String,
    val imageUrl: String = "",
    val streamUrl: String = "",
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadSizeMb: Double = 6.8,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val bitrateKbps: Int = 320,
    val year: Int = 2024,
    val localFilePath: String = ""
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverColorHex: Long = 0xFF1DB954,
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = true
)

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "listening_stats")
data class ListeningStat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val songTitle: String,
    val artist: String,
    val genre: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)
