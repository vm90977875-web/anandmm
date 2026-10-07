package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppReview
import com.example.data.DefaultReviews
import com.example.data.DefaultSongs
import com.example.data.FirestoreService
import com.example.data.MusicRepository
import com.example.data.OnlineMusicService
import com.example.data.Playlist
import com.example.data.Song
import com.example.player.AndroidAudioPlayer
import com.example.player.LyricLine
import com.example.player.LyricsParser
import com.example.player.RepeatMode
import com.example.receiver.BeatifyWidgetProvider
import com.example.util.AppLanguage
import com.example.util.MusicNotificationHelper
import com.example.util.PlaybackBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfile(
    val isSignedIn: Boolean = true,
    val name: String = "Music Lover",
    val email: String = "user@beatify.app",
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80",
    val handle: String = "music_lover",
    val favoriteGenres: List<String> = listOf("Bollywood", "Punjabi", "Pop"),
    val audioQuality: String = "High (320 kbps)",
    val isPremium: Boolean = true,
    val lastSyncedTime: String = "Just now"
)

enum class Screen {
    HOME, SEARCH, LIBRARY, EQUALIZER, SETTINGS, PLAYLIST_DETAIL, ARTIST_PROFILE
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("beatify_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val onlineMusicService = OnlineMusicService(application)
    val repository = MusicRepository(
        database.songDao(),
        database.playlistDao(),
        database.listeningStatDao(),
        onlineMusicService
    )

    val firestoreService = FirestoreService(application)
    private val player = AndroidAudioPlayer(application, viewModelScope)

    // Online Live Music Search State
    val onlineSearchResults = MutableStateFlow<List<Song>>(emptyList())
    val isSearchingOnline = MutableStateFlow(false)
    private var searchJob: Job? = null

    // Live Download tracking
    val downloadingSongIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Followed Artists & Celebration Animation
    val followedArtists = MutableStateFlow<Set<String>>(emptySet())
    val celebrationArtist = MutableStateFlow<String?>(null)
    val isCelebrationVisible = MutableStateFlow(false)

    // Heart Emoji Liked Song Celebration
    val celebrationLikedSong = MutableStateFlow<String?>(null)
    val isHeartCelebrationVisible = MutableStateFlow(false)

    // Onboarding Status (False on first app launch, True after user signs in & sets up profile)
    val isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("KEY_ONBOARDING_COMPLETED", false)
    )

    // Navigation
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedArtistName = MutableStateFlow<String?>(null)
    val selectedArtistName: StateFlow<String?> = _selectedArtistName.asStateFlow()

    private val _artistSongs = MutableStateFlow<List<Song>>(emptyList())
    val artistSongs: StateFlow<List<Song>> = _artistSongs.asStateFlow()

    private val _isArtistLoading = MutableStateFlow(false)
    val isArtistLoading: StateFlow<Boolean> = _isArtistLoading.asStateFlow()

    // App Reviews StateFlow
    val appReviews = MutableStateFlow<List<AppReview>>(DefaultReviews.initialReviews)

    // Preferences & Settings
    val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val isDarkMode = MutableStateFlow(true)
    val isPureBlack = MutableStateFlow(false)
    val isOfflineMode = MutableStateFlow(false)
    val streamingQuality = MutableStateFlow("High (320 kbps)")
    val downloadQuality = MutableStateFlow("High (320 kbps)")
    val equalizerPreset = MutableStateFlow("Bass Boost")
    val equalizerBands = MutableStateFlow(listOf(4f, 6f, 2f, 1f, 3f)) // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val isEqualizerEnabled = MutableStateFlow(true)
    val bassBoostLevel = MutableStateFlow(80f) // 0 to 100
    val virtualizerLevel = MutableStateFlow(65f) // 0 to 100
    val loudnessBoostLevel = MutableStateFlow(40f) // 0 to 100
    val vocalClarityLevel = MutableStateFlow(75f) // 0 to 100
    val reverbPreset = MutableStateFlow("Studio")

    // Daily Fresh Tracks & Pull-to-refresh
    val dailyTrendingSongs = MutableStateFlow<List<Song>>(emptyList())
    val isRefreshingDaily = MutableStateFlow(false)
    val dailyRefreshMessage = MutableStateFlow<String?>(null)

    val crossfadeSeconds = MutableStateFlow(3)
    val volumeNormalization = MutableStateFlow(true)
    val dataSaver = MutableStateFlow(false)

    // User Profile
    val userProfile = MutableStateFlow(UserProfile())

    // Database Flows
    val allSongs = repository.allSongs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val downloadedSongs = repository.downloadedSongs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val favoriteSongs = repository.favoriteSongs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allPlaylists = repository.allPlaylists.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val listeningStats = repository.recentStats.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val totalListeningSeconds = repository.totalListeningSeconds.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        3600L
    )

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedGenreFilter = MutableStateFlow<String?>(null)
    val selectedLanguageFilter = MutableStateFlow<String?>(null)

    // Player State
    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val currentPositionMs = MutableStateFlow(0L)
    val songDurationMs = MutableStateFlow(240000L)
    val repeatMode = MutableStateFlow(RepeatMode.ALL)
    val isShuffle = MutableStateFlow(false)
    val playbackQueue = MutableStateFlow<List<Song>>(emptyList())
    val parsedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val activeLyricIndex = MutableStateFlow(0)

    // UI Modals
    val isFullScreenPlayerOpen = MutableStateFlow(false)
    val isLyricsExpanded = MutableStateFlow(false)
    val isShareDialogOpen = MutableStateFlow(false)
    val isAddToPlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val actionSong = MutableStateFlow<Song?>(null)

    init {
        player.onPositionUpdated = { posMs ->
            currentPositionMs.value = posMs
            val activeIdx = LyricsParser.findActiveIndex(parsedLyrics.value, posMs)
            if (activeIdx != activeLyricIndex.value) {
                activeLyricIndex.value = activeIdx
            }
        }

        player.onSongCompleted = {
            handleSongEnd()
        }

        // Sync default songs to database and Firestore catalog
        viewModelScope.launch(Dispatchers.IO) {
            database.songDao().insertSongs(DefaultSongs.songs)
            firestoreService.syncGlobalSongs(DefaultSongs.songs)
        }
        viewModelScope.launch {
            allSongs.collect { songs ->
                if (songs.isNotEmpty() && currentSong.value == null) {
                    currentSong.value = songs.first()
                    playbackQueue.value = songs
                    parsedLyrics.value = LyricsParser.parse(songs.first().lyricsLrc)
                    songDurationMs.value = songs.first().durationMs
                }
            }
        }

        // Restore user profile from SharedPreferences
        if (prefs.getBoolean("KEY_ONBOARDING_COMPLETED", false)) {
            val name = prefs.getString("KEY_USER_NAME", "Music Lover") ?: "Music Lover"
            val email = prefs.getString("KEY_USER_EMAIL", "user@beatify.app") ?: "user@beatify.app"
            val avatar = prefs.getString("KEY_USER_AVATAR", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80"
            val handle = prefs.getString("KEY_USER_HANDLE", "music_lover") ?: "music_lover"
            val genresStr = prefs.getString("KEY_USER_GENRES", "Bollywood,Punjabi,Pop") ?: "Bollywood,Punjabi,Pop"
            val quality = prefs.getString("KEY_USER_QUALITY", "High (320 kbps)") ?: "High (320 kbps)"
            val isSignedIn = prefs.getBoolean("KEY_USER_SIGNED_IN", true)

            userProfile.value = UserProfile(
                isSignedIn = isSignedIn,
                name = name,
                email = email,
                avatarUrl = avatar,
                handle = handle,
                favoriteGenres = genresStr.split(",").filter { it.isNotBlank() },
                audioQuality = quality,
                isPremium = true
            )
            streamingQuality.value = quality
        }

        val savedArtists = prefs.getStringSet("KEY_FOLLOWED_ARTISTS", null)
        if (savedArtists != null) {
            followedArtists.value = savedArtists
        } else {
            val initial = setOf("Arijit Singh", "Diljit Dosanjh", "Sidhu Moose Wala")
            followedArtists.value = initial
            prefs.edit().putStringSet("KEY_FOLLOWED_ARTISTS", initial).apply()
        }

        // Load saved user reviews if any
        val savedReviewsJson = prefs.getString("KEY_USER_REVIEWS_JSON", null)
        if (!savedReviewsJson.isNullOrBlank()) {
            try {
                val jsonArray = org.json.JSONArray(savedReviewsJson)
                val userList = mutableListOf<AppReview>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    userList.add(
                        AppReview(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            userName = obj.optString("userName", "Anonymous"),
                            userEmail = obj.optString("userEmail", ""),
                            rating = obj.optInt("rating", 5),
                            comment = obj.optString("comment", ""),
                            dateLabel = obj.optString("dateLabel", "Today"),
                            verifiedUser = true,
                            avatarGradientStart = obj.optLong("avatarGradientStart", 0xFF1DB954),
                            avatarGradientEnd = obj.optLong("avatarGradientEnd", 0xFF191414),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                appReviews.value = userList + DefaultReviews.initialReviews
            } catch (e: Exception) {
                // Keep default reviews
            }
        }

        // Connect PlaybackBridge for system status bar notification actions & widget controls
        PlaybackBridge.onPlayPause = {
            viewModelScope.launch(Dispatchers.Main) {
                togglePlayPause()
            }
        }
        PlaybackBridge.onNext = {
            viewModelScope.launch(Dispatchers.Main) {
                playNext()
            }
        }
        PlaybackBridge.onPrev = {
            viewModelScope.launch(Dispatchers.Main) {
                playPrevious()
            }
        }
        PlaybackBridge.onSkip10 = {
            viewModelScope.launch(Dispatchers.Main) {
                val newPos = (currentPositionMs.value + 10000L).coerceAtMost(songDurationMs.value)
                seekTo(newPos)
            }
        }
        PlaybackBridge.onStop = {
            viewModelScope.launch(Dispatchers.Main) {
                stopPlayback()
            }
        }

        // Restore sound booster settings
        isEqualizerEnabled.value = prefs.getBoolean("KEY_EQ_ENABLED", true)
        bassBoostLevel.value = prefs.getFloat("KEY_BASS_BOOST", 80f)
        virtualizerLevel.value = prefs.getFloat("KEY_VIRTUALIZER", 65f)
        loudnessBoostLevel.value = prefs.getFloat("KEY_LOUDNESS_BOOST", 40f)
        vocalClarityLevel.value = prefs.getFloat("KEY_VOCAL_CLARITY", 75f)
        reverbPreset.value = prefs.getString("KEY_REVERB_PRESET", "Studio") ?: "Studio"

        // Initial daily track generation
        refreshDailyTracks()
    }

    // Navigation History Stack
    private val screenStack = ArrayDeque<Screen>().apply { add(Screen.HOME) }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            if (screen == Screen.HOME) {
                screenStack.clear()
                screenStack.add(Screen.HOME)
            } else {
                if (screenStack.isEmpty() || screenStack.last() != screen) {
                    screenStack.add(screen)
                }
            }
            _currentScreen.value = screen
        }
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        if (_currentScreen.value != Screen.PLAYLIST_DETAIL) {
            screenStack.add(Screen.PLAYLIST_DETAIL)
            _currentScreen.value = Screen.PLAYLIST_DETAIL
        }
    }

    fun openArtist(artistName: String) {
        if (artistName.isBlank()) return
        val cleanName = artistName.split(",", "&", "feat.", "ft.", "/").firstOrNull()?.trim() ?: artistName.trim()
        _selectedArtistName.value = cleanName

        // Immediate local match for zero latency
        val currentAll = allSongs.value
        val localMatches = currentAll.filter {
            it.artist.contains(cleanName, ignoreCase = true) || it.title.contains(cleanName, ignoreCase = true)
        }
        _artistSongs.value = localMatches

        // Fetch online tracks for this artist dynamically
        viewModelScope.launch(Dispatchers.IO) {
            _isArtistLoading.value = true
            try {
                val onlineResults = onlineMusicService.searchOnlineSongs(cleanName)
                val existingIds = localMatches.map { it.id }.toSet()
                val existingTitles = localMatches.map { it.title.lowercase().trim() }.toSet()

                val newUnique = onlineResults.filter { it.id !in existingIds && it.title.lowercase().trim() !in existingTitles }
                val combined = localMatches + newUnique
                _artistSongs.value = combined

                if (newUnique.isNotEmpty()) {
                    database.songDao().insertSongs(newUnique)
                }
            } catch (e: Exception) {
                // Fallback to local
            } finally {
                _isArtistLoading.value = false
            }
        }

        if (_currentScreen.value != Screen.ARTIST_PROFILE) {
            screenStack.add(Screen.ARTIST_PROFILE)
            _currentScreen.value = Screen.ARTIST_PROFILE
        }
    }

    fun submitReview(name: String, email: String, rating: Int, comment: String): Boolean {
        if (comment.isBlank()) return false
        val newReview = AppReview(
            id = "rev_user_${System.currentTimeMillis()}",
            userName = if (name.isNotBlank()) name.trim() else userProfile.value.name,
            userEmail = if (email.isNotBlank()) email.trim() else userProfile.value.email,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            dateLabel = "Just now",
            verifiedUser = true,
            avatarGradientStart = 0xFF1DB954,
            avatarGradientEnd = 0xFF191414,
            timestamp = System.currentTimeMillis()
        )

        val updatedList = listOf(newReview) + appReviews.value
        appReviews.value = updatedList

        // Persist locally in SharedPreferences
        try {
            val jsonArray = org.json.JSONArray()
            updatedList.filter { it.id.startsWith("rev_user_") }.take(20).forEach { r ->
                val obj = org.json.JSONObject()
                obj.put("id", r.id)
                obj.put("userName", r.userName)
                obj.put("userEmail", r.userEmail)
                obj.put("rating", r.rating)
                obj.put("comment", r.comment)
                obj.put("dateLabel", r.dateLabel)
                obj.put("avatarGradientStart", r.avatarGradientStart)
                obj.put("avatarGradientEnd", r.avatarGradientEnd)
                obj.put("timestamp", r.timestamp)
                jsonArray.put(obj)
            }
            prefs.edit().putString("KEY_USER_REVIEWS_JSON", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // ignore
        }

        // Sync with Firestore
        viewModelScope.launch(Dispatchers.IO) {
            val uid = newReview.userEmail.ifBlank { "anonymous" }.replace(".", "_").replace("@", "_")
            firestoreService.syncUserReview(uid, newReview)
        }
        return true
    }

    fun canNavigateBack(): Boolean {
        return isCelebrationVisible.value ||
               isShareDialogOpen.value ||
               isAddToPlaylistDialogOpen.value ||
               isCreatePlaylistDialogOpen.value ||
               isLyricsExpanded.value ||
               isFullScreenPlayerOpen.value ||
               _currentScreen.value == Screen.ARTIST_PROFILE ||
               _currentScreen.value == Screen.PLAYLIST_DETAIL ||
               _currentScreen.value != Screen.HOME ||
               screenStack.size > 1
    }

    fun handleBackPress(): Boolean {
        // 1. Close overlay modals
        if (isCelebrationVisible.value) {
            isCelebrationVisible.value = false
            return true
        }
        if (isShareDialogOpen.value) {
            isShareDialogOpen.value = false
            return true
        }
        if (isAddToPlaylistDialogOpen.value) {
            isAddToPlaylistDialogOpen.value = false
            return true
        }
        if (isCreatePlaylistDialogOpen.value) {
            isCreatePlaylistDialogOpen.value = false
            return true
        }

        // 2. Close FullScreenPlayer / Lyrics
        if (isLyricsExpanded.value) {
            isLyricsExpanded.value = false
            return true
        }
        if (isFullScreenPlayerOpen.value) {
            isFullScreenPlayerOpen.value = false
            return true
        }

        // 3. Artist profile page
        if (_currentScreen.value == Screen.ARTIST_PROFILE) {
            _selectedArtistName.value = null
            if (screenStack.isNotEmpty() && screenStack.last() == Screen.ARTIST_PROFILE) {
                screenStack.removeLast()
            }
            val prev = if (screenStack.isNotEmpty()) screenStack.last() else Screen.HOME
            _currentScreen.value = prev
            return true
        }

        // 4. Playlist detail page
        if (_currentScreen.value == Screen.PLAYLIST_DETAIL) {
            _selectedPlaylist.value = null
            if (screenStack.isNotEmpty() && screenStack.last() == Screen.PLAYLIST_DETAIL) {
                screenStack.removeLast()
            }
            val prev = if (screenStack.isNotEmpty()) screenStack.last() else Screen.LIBRARY
            _currentScreen.value = prev
            return true
        }

        // 5. Multi-screen back stack
        if (screenStack.size > 1) {
            screenStack.removeLast()
            val prev = screenStack.last()
            _currentScreen.value = prev
            return true
        } else if (_currentScreen.value != Screen.HOME) {
            screenStack.clear()
            screenStack.add(Screen.HOME)
            _currentScreen.value = Screen.HOME
            return true
        }

        return false
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        if (isOfflineMode.value && !song.isDownloaded) {
            // Can't play non-downloaded songs in offline mode
            return
        }

        currentSong.value = song
        val actualQueue = if (queue.isNotEmpty()) queue else (if (isOfflineMode.value) downloadedSongs.value else allSongs.value)
        playbackQueue.value = actualQueue
        parsedLyrics.value = LyricsParser.parse(song.lyricsLrc)
        activeLyricIndex.value = 0
        songDurationMs.value = song.durationMs
        currentPositionMs.value = 0L

        player.playSong(song, 0L)
        isPlaying.value = true

        // Update Android Status Bar Notification & Home Screen Widgets
        try {
            MusicNotificationHelper.showPlaybackNotification(getApplication(), song, isPlaying = true)
            BeatifyWidgetProvider.updateAllWidgets(getApplication(), song.title, song.artist, isPlaying = true)
        } catch (e: Exception) {
            // Ignored
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.recordSongPlay(song)
            val uid = userProfile.value.email.replace(".", "_").replace("@", "_")
            firestoreService.syncListeningHistory(uid, song)
        }
    }

    fun togglePlayPause() {
        val song = currentSong.value ?: return
        if (isPlaying.value) {
            player.pause()
            isPlaying.value = false
            try {
                MusicNotificationHelper.showPlaybackNotification(getApplication(), song, isPlaying = false)
                BeatifyWidgetProvider.updateAllWidgets(getApplication(), song.title, song.artist, isPlaying = false)
            } catch (e: Exception) {
                // Ignored
            }
        } else {
            if (player.currentPositionMs == 0L) {
                player.playSong(song, currentPositionMs.value)
            } else {
                player.resume()
            }
            isPlaying.value = true
            try {
                MusicNotificationHelper.showPlaybackNotification(getApplication(), song, isPlaying = true)
                BeatifyWidgetProvider.updateAllWidgets(getApplication(), song.title, song.artist, isPlaying = true)
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        currentPositionMs.value = positionMs
        activeLyricIndex.value = LyricsParser.findActiveIndex(parsedLyrics.value, positionMs)
    }

    fun playNext() {
        val queue = playbackQueue.value
        if (queue.isEmpty()) return
        val currentIdx = queue.indexOfFirst { it.id == currentSong.value?.id }

        val nextSong = if (isShuffle.value) {
            queue.filter { it.id != currentSong.value?.id }.randomOrNull() ?: queue.first()
        } else {
            val nextIdx = (currentIdx + 1) % queue.size
            queue[nextIdx]
        }
        playSong(nextSong, queue)
    }

    fun playPrevious() {
        if (currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val queue = playbackQueue.value
        if (queue.isEmpty()) return
        val currentIdx = queue.indexOfFirst { it.id == currentSong.value?.id }
        val prevIdx = if (currentIdx > 0) currentIdx - 1 else queue.size - 1
        playSong(queue[prevIdx], queue)
    }

    fun toggleShuffle() {
        isShuffle.value = !isShuffle.value
    }

    fun toggleRepeat() {
        repeatMode.value = when (repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    private fun handleSongEnd() {
        when (repeatMode.value) {
            RepeatMode.ONE -> {
                currentSong.value?.let { playSong(it, playbackQueue.value) }
            }
            RepeatMode.ALL -> playNext()
            RepeatMode.OFF -> {
                val queue = playbackQueue.value
                val currentIdx = queue.indexOfFirst { it.id == currentSong.value?.id }
                if (currentIdx < queue.size - 1) {
                    playNext()
                } else {
                    isPlaying.value = false
                    player.pause()
                }
            }
        }
    }

    fun toggleFavorite(song: Song) {
        val willBeFavorite = !song.isFavorite
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdateSong(song)
            repository.toggleFavorite(song.id, song.isFavorite)
            val uid = userProfile.value.email.replace(".", "_").replace("@", "_")
            firestoreService.syncFavoriteSong(uid, song.id, willBeFavorite)
        }
        if (willBeFavorite) {
            celebrationLikedSong.value = song.title
            isHeartCelebrationVisible.value = true
        }
    }

    fun toggleDownload(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val songId = song.id
            if (downloadingSongIds.value.contains(songId)) return@launch

            // Ensure song is saved in database
            repository.insertOrUpdateSong(song)

            downloadingSongIds.value = downloadingSongIds.value + songId
            repository.toggleDownload(song) { progress ->
                downloadProgress.value = downloadProgress.value + (songId to progress)
            }
            downloadingSongIds.value = downloadingSongIds.value - songId
            downloadProgress.value = downloadProgress.value - songId
        }
    }

    fun searchOnline(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            onlineSearchResults.value = emptyList()
            isSearchingOnline.value = false
            return
        }
        searchJob = viewModelScope.launch {
            isSearchingOnline.value = true
            delay(350)
            val results = repository.searchOnlineSongs(query)
            onlineSearchResults.value = results
            isSearchingOnline.value = false
        }
    }

    fun playSongWithAutoInsert(song: Song, queue: List<Song> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdateSong(song)
        }
        playSong(song, queue.ifEmpty { listOf(song) })
    }

    fun toggleFollowArtist(artistName: String) {
        val current = followedArtists.value
        val isNowFollowing = !current.contains(artistName)
        val updated = if (isNowFollowing) current + artistName else current - artistName
        followedArtists.value = updated
        prefs.edit().putStringSet("KEY_FOLLOWED_ARTISTS", updated).apply()

        if (isNowFollowing) {
            // Trigger big celebratory emoji animation
            celebrationArtist.value = artistName
            isCelebrationVisible.value = true
        }
    }

    fun dismissCelebration() {
        isCelebrationVisible.value = false
    }

    fun dismissHeartCelebration() {
        isHeartCelebrationVisible.value = false
    }

    fun createPlaylist(name: String, description: String, colorHex: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.createPlaylist(name, description, colorHex)
            val pl = Playlist(id, name, description, colorHex, isCustom = true)
            val uid = userProfile.value.email.replace(".", "_").replace("@", "_")
            firestoreService.syncUserPlaylist(uid, pl)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
                _currentScreen.value = Screen.LIBRARY
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun openActionMenu(song: Song) {
        actionSong.value = song
    }

    fun openShareDialog(song: Song) {
        actionSong.value = song
        isShareDialogOpen.value = true
    }

    fun openAddToPlaylistDialog(song: Song) {
        actionSong.value = song
        isAddToPlaylistDialogOpen.value = true
    }

    fun completeOnboarding(
        name: String,
        email: String,
        avatar: String,
        handle: String,
        genres: List<String>,
        quality: String
    ) {
        prefs.edit()
            .putBoolean("KEY_ONBOARDING_COMPLETED", true)
            .putBoolean("KEY_USER_SIGNED_IN", true)
            .putString("KEY_USER_NAME", name)
            .putString("KEY_USER_EMAIL", email)
            .putString("KEY_USER_AVATAR", avatar)
            .putString("KEY_USER_HANDLE", handle)
            .putString("KEY_USER_GENRES", genres.joinToString(","))
            .putString("KEY_USER_QUALITY", quality)
            .apply()

        userProfile.value = UserProfile(
            isSignedIn = true,
            name = name,
            email = email,
            avatarUrl = avatar,
            handle = handle,
            favoriteGenres = genres,
            audioQuality = quality,
            isPremium = true
        )
        streamingQuality.value = quality
        downloadQuality.value = quality
        isOnboardingCompleted.value = true
        _currentScreen.value = Screen.HOME

        viewModelScope.launch(Dispatchers.IO) {
            val uid = email.replace(".", "_").replace("@", "_")
            firestoreService.syncUserProfile(uid, name, email, avatar, handle, genres, quality)
        }
    }

    fun updateProfile(name: String, handle: String, avatar: String, genres: List<String>) {
        val current = userProfile.value
        val updated = current.copy(
            name = name,
            handle = handle,
            avatarUrl = avatar,
            favoriteGenres = genres
        )
        userProfile.value = updated
        prefs.edit()
            .putString("KEY_USER_NAME", name)
            .putString("KEY_USER_HANDLE", handle)
            .putString("KEY_USER_AVATAR", avatar)
            .putString("KEY_USER_GENRES", genres.joinToString(","))
            .apply()

        viewModelScope.launch(Dispatchers.IO) {
            val uid = updated.email.replace(".", "_").replace("@", "_")
            firestoreService.syncUserProfile(uid, name, updated.email, avatar, handle, genres, updated.audioQuality)
        }
    }

    fun resetOnboardingForDemo() {
        prefs.edit()
            .putBoolean("KEY_ONBOARDING_COMPLETED", false)
            .apply()
        isOnboardingCompleted.value = false
    }

    fun toggleGoogleSignIn() {
        val current = userProfile.value
        val newSignedIn = !current.isSignedIn
        userProfile.value = current.copy(
            isSignedIn = newSignedIn,
            name = if (newSignedIn) "Music Lover" else "Guest Listener",
            email = if (newSignedIn) "user@beatify.app" else "guest@beatify.app"
        )
        prefs.edit().putBoolean("KEY_USER_SIGNED_IN", newSignedIn).apply()
    }

    fun setEqualizerPreset(preset: String) {
        equalizerPreset.value = preset
        equalizerBands.value = when (preset) {
            "Bass Boost" -> listOf(7f, 5f, 2f, 0f, 1f)
            "Vocal Booster" -> listOf(0f, 3f, 6f, 5f, 2f)
            "Electronic" -> listOf(6f, 4f, 0f, 4f, 6f)
            "Rock" -> listOf(5f, 3f, -1f, 3f, 5f)
            "Acoustic" -> listOf(3f, 2f, 3f, 4f, 3f)
            "Jazz" -> listOf(4f, 2f, 1f, 3f, 4f)
            else -> listOf(0f, 0f, 0f, 0f, 0f) // Flat
        }
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val current = equalizerBands.value.toMutableList()
        if (index in current.indices) {
            current[index] = gainDb
            equalizerBands.value = current
            equalizerPreset.value = "Custom"
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        isEqualizerEnabled.value = enabled
        prefs.edit().putBoolean("KEY_EQ_ENABLED", enabled).apply()
    }

    fun setBassBoost(level: Float) {
        bassBoostLevel.value = level
        prefs.edit().putFloat("KEY_BASS_BOOST", level).apply()
    }

    fun setVirtualizer(level: Float) {
        virtualizerLevel.value = level
        prefs.edit().putFloat("KEY_VIRTUALIZER", level).apply()
    }

    fun setLoudnessBoost(level: Float) {
        loudnessBoostLevel.value = level
        prefs.edit().putFloat("KEY_LOUDNESS_BOOST", level).apply()
    }

    fun setVocalClarity(level: Float) {
        vocalClarityLevel.value = level
        prefs.edit().putFloat("KEY_VOCAL_CLARITY", level).apply()
    }

    fun setReverbPreset(preset: String) {
        reverbPreset.value = preset
        prefs.edit().putString("KEY_REVERB_PRESET", preset).apply()
    }

    fun stopPlayback() {
        isPlaying.value = false
        player.pause()
        currentPositionMs.value = 0L
        MusicNotificationHelper.cancelNotification(getApplication())
        val current = currentSong.value
        if (current != null) {
            BeatifyWidgetProvider.updateAllWidgets(getApplication(), current.title, current.artist, isPlaying = false)
        }
    }

    fun refreshDailyTracks() {
        if (isRefreshingDaily.value) return
        viewModelScope.launch {
            isRefreshingDaily.value = true
            dailyRefreshMessage.value = "Refreshing today's hits..."
            try {
                val seedQueries = listOf(
                    "Latest Bollywood Hits",
                    "Arijit Singh Top",
                    "Punjabi Trending 2025",
                    "Viral Hits Global",
                    "Diljit Dosanjh Hits",
                    "Romantic Hindi Songs",
                    "Badshah New Songs",
                    "Indie Pop India"
                )
                val randomQuery = seedQueries.random()
                val onlineSongs = onlineMusicService.searchOnlineSongs(randomQuery)
                if (onlineSongs.isNotEmpty()) {
                    dailyTrendingSongs.value = onlineSongs.shuffled().take(12)
                    dailyRefreshMessage.value = "Updated with today's trending songs!"
                } else {
                    val pool = allSongs.value.ifEmpty { DefaultSongs.songs }
                    dailyTrendingSongs.value = pool.shuffled().take(12)
                    dailyRefreshMessage.value = "Refreshed with fresh daily mix!"
                }
            } catch (e: Exception) {
                val pool = allSongs.value.ifEmpty { DefaultSongs.songs }
                dailyTrendingSongs.value = pool.shuffled().take(12)
                dailyRefreshMessage.value = "Refreshed with daily tracks!"
            } finally {
                delay(600)
                isRefreshingDaily.value = false
                delay(2000)
                dailyRefreshMessage.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
