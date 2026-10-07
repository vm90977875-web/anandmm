package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class OnlineMusicService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchOnlineSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=song&limit=50"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BeatifyMusic/1.0 (Android)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("OnlineMusicService", "iTunes search returned code ${response.code}")
                return@withContext emptyList()
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return@withContext emptyList()
            val songList = mutableListOf<Song>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val trackId = item.optLong("trackId", 0L)
                val trackName = item.optString("trackName", "")
                val artistName = item.optString("artistName", "")
                val collectionName = item.optString("collectionName", "Single")
                val previewUrl = item.optString("previewUrl", "")
                val rawArtwork = item.optString("artworkUrl100", "")
                // Upscale artwork to 600x600 for crisp visual presentation
                val highResArtwork = if (rawArtwork.isNotBlank()) {
                    rawArtwork.replace("100x100bb.jpg", "600x600bb.jpg")
                } else ""
                val genre = item.optString("primaryGenreName", "Pop")
                val trackTimeMillis = item.optLong("trackTimeMillis", 210000L)
                val releaseDate = item.optString("releaseDate", "2024")
                val year = try { releaseDate.take(4).toInt() } catch (e: Exception) { 2024 }

                if (trackName.isNotBlank() && previewUrl.isNotBlank()) {
                    val sId = "online_$trackId"
                    val (gStart, gEnd) = getGradientsForIndex(Math.abs(sId.hashCode()))
                    val isDl = checkIfDownloaded(sId)
                    val localPath = if (isDl) getLocalDownloadPath(sId) else ""

                    songList.add(
                        Song(
                            id = sId,
                            title = trackName,
                            artist = artistName,
                            album = collectionName,
                            durationMs = trackTimeMillis,
                            coverGradientStart = gStart,
                            coverGradientEnd = gEnd,
                            genre = genre,
                            language = detectLanguage(artistName, genre),
                            lyricsLrc = generateLrc(trackName, artistName),
                            imageUrl = highResArtwork,
                            streamUrl = previewUrl,
                            isDownloaded = isDl,
                            localFilePath = localPath,
                            downloadSizeMb = 3.8,
                            bitrateKbps = 320,
                            year = year
                        )
                    )
                }
            }
            songList
        } catch (e: Exception) {
            Log.e("OnlineMusicService", "Error during online music search: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun downloadSong(
        song: Song,
        onProgress: (Int) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (song.streamUrl.isBlank()) return@withContext null
            val dir = File(context.filesDir, "downloads")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${song.id}.m4a")
            if (file.exists() && file.length() > 5000) {
                return@withContext file.absolutePath
            }

            val request = Request.Builder().url(song.streamUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("OnlineMusicService", "Failed to download song ${song.title}: ${e.message}")
            null
        }
    }

    fun deleteDownloadedSong(songId: String): Boolean {
        val file = File(File(context.filesDir, "downloads"), "$songId.m4a")
        return if (file.exists()) file.delete() else false
    }

    fun checkIfDownloaded(songId: String): Boolean {
        val file = File(File(context.filesDir, "downloads"), "$songId.m4a")
        return file.exists() && file.length() > 5000
    }

    fun getLocalDownloadPath(songId: String): String {
        val file = File(File(context.filesDir, "downloads"), "$songId.m4a")
        return if (file.exists()) file.absolutePath else ""
    }

    private fun detectLanguage(artist: String, genre: String): String {
        val lower = (artist + " " + genre).lowercase()
        return when {
            lower.contains("bollywood") || lower.contains("hindi") || lower.contains("arijit") ||
            lower.contains("pritam") || lower.contains("neha") || lower.contains("shreya") -> "Hindi"
            lower.contains("punjabi") || lower.contains("dhillon") || lower.contains("sidhu") ||
            lower.contains("karan aujla") || lower.contains("diljit") -> "Punjabi"
            lower.contains("spanish") || lower.contains("latin") || lower.contains("reggaeton") -> "Spanish"
            else -> "English"
        }
    }

    private fun generateLrc(title: String, artist: String): String {
        return """
[00:01.00] ♫ $title ♫
[00:04.00] Artist: $artist
[00:08.00] Streaming in High-Fidelity Audio
[00:15.00] Enjoy the pure musical experience
[00:22.00] Beatify • Music for every moment
        """.trimIndent()
    }

    private fun getGradientsForIndex(hash: Int): Pair<Long, Long> {
        val gradients = listOf(
            0xFFFF5E3A to 0xFFFF2A68,
            0xFF11998E to 0xFF38EF7D,
            0xFF654EA3 to 0xFFEAAFC8,
            0xFF4A00E0 to 0xFF8E2DE2,
            0xFFFF7E5F to 0xFFFEB47B,
            0xFF00C9FF to 0xFF92FE9D,
            0xFFED213A to 0xFF93291E,
            0xFF56CCF2 to 0xFF2F80ED,
            0xFFF7971E to 0xFFFFD200
        )
        return gradients[hash % gradients.size]
    }
}
