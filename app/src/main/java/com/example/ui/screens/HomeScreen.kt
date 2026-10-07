package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Song
import com.example.data.AppReview
import com.example.ui.components.AlbumArtBox
import com.example.ui.components.ArtistCelebrationOverlay
import com.example.ui.components.VoiceSearchDialog
import com.example.util.TactileFeedbackHelper
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.AppStrings
import java.util.Calendar

data class HomeArtist(
    val name: String,
    val imageUrl: String,
    val monthlyListeners: String,
    val topGenre: String
)

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val downloadingIds by viewModel.downloadingSongIds.collectAsState()
    val isCelebrationVisible by viewModel.isCelebrationVisible.collectAsState()
    val celebrationArtist by viewModel.celebrationArtist.collectAsState()
    val appReviews by viewModel.appReviews.collectAsState()

    val dailySongs by viewModel.dailyTrendingSongs.collectAsState()
    val isRefreshingDaily by viewModel.isRefreshingDaily.collectAsState()
    val dailyRefreshMessage by viewModel.dailyRefreshMessage.collectAsState()
    var isVoiceSearchOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val displayedSongs = if (isOfflineMode) downloadedSongs else allSongs

    val greeting = remember(lang) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> AppStrings.get("greeting_morning", lang)
            in 12..17 -> AppStrings.get("greeting_afternoon", lang)
            else -> AppStrings.get("greeting_evening", lang)
        }
    }

    // Curated catalog of celebrated artists
    val curatedArtists = remember {
        listOf(
            HomeArtist(
                name = "Arijit Singh",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/9f/13/ca/9f13ca3b-e533-03e0-f19a-f0aaa774581d/196589311191.jpg/600x600bb.jpg",
                monthlyListeners = "46.8M",
                topGenre = "Bollywood Melodies"
            ),
            HomeArtist(
                name = "Diljit Dosanjh",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/8a/89/e4/8a89e445-d2c6-f8ac-a828-27818b0c1afe/859749638209_cover.jpg/600x600bb.jpg",
                monthlyListeners = "32.4M",
                topGenre = "Punjabi Pop"
            ),
            HomeArtist(
                name = "Sidhu Moose Wala",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/97/69/58/976958ae-725e-bd41-6755-f0921c697840/810063889609_cover.jpg/600x600bb.jpg",
                monthlyListeners = "28.9M",
                topGenre = "Punjabi Hip-Hop"
            ),
            HomeArtist(
                name = "The Weeknd",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/b5/92/bb/b592bb72-52e3-e756-9b26-9f56d08f47ab/16UMGIM67864.rgb.jpg/600x600bb.jpg",
                monthlyListeners = "108M",
                topGenre = "R&B / Synthwave"
            ),
            HomeArtist(
                name = "Karan Aujla",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/d3/08/bc/d308bc6a-20e1-6532-d933-35d1b429210e/5054197755538.jpg/600x600bb.jpg",
                monthlyListeners = "25.1M",
                topGenre = "Punjabi Wave"
            ),
            HomeArtist(
                name = "Taylor Swift",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/49/3d/ab/493dab54-f920-9043-6181-80993b8116c9/19UMGIM53909.rgb.jpg/600x600bb.jpg",
                monthlyListeners = "98.5M",
                topGenre = "Pop"
            ),
            HomeArtist(
                name = "Pritam",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/2d/a1/b8/2da1b888-ff70-edc8-1c2e-01d251a5ca10/197189341342.jpg/600x600bb.jpg",
                monthlyListeners = "38.2M",
                topGenre = "Bollywood Composer"
            ),
            HomeArtist(
                name = "Shubh",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/dc/46/a9/dc46a9c9-794e-2d7a-1afb-97eb4ae0fff6/197188915704.jpg/600x600bb.jpg",
                monthlyListeners = "19.7M",
                topGenre = "Punjabi Trap"
            ),
            HomeArtist(
                name = "Ed Sheeran",
                imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg",
                monthlyListeners = "78.4M",
                topGenre = "Acoustic Pop"
            )
        )
    }

    // Genre-filtered lists for the different rich shelves
    val bollywoodSongs = remember(displayedSongs) {
        displayedSongs.filter { it.genre.equals("Bollywood", ignoreCase = true) || it.language.equals("Hindi", ignoreCase = true) }
    }

    val punjabiSongs = remember(displayedSongs) {
        displayedSongs.filter { it.genre.equals("Punjabi", ignoreCase = true) || it.language.equals("Punjabi", ignoreCase = true) }
    }

    val globalHits = remember(displayedSongs) {
        displayedSongs.filter { it.language.equals("English", ignoreCase = true) || it.genre.equals("Pop", ignoreCase = true) || it.genre.equals("Synthwave", ignoreCase = true) }
    }

    val romanceSongs = remember(displayedSongs) {
        displayedSongs.filter {
            it.title.contains("Kesariya", ignoreCase = true) ||
            it.title.contains("Apna Bana Le", ignoreCase = true) ||
            it.title.contains("Lover", ignoreCase = true) ||
            it.title.contains("Tum Hi Ho", ignoreCase = true) ||
            it.title.contains("Raataan", ignoreCase = true) ||
            it.title.contains("Ve Kamleya", ignoreCase = true) ||
            it.title.contains("Tere Pyaar Mein", ignoreCase = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = isRefreshingDaily,
            onRefresh = { viewModel.refreshDailyTracks() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Top Bar & Branding (Clean, copyright-safe, modern premium aesthetic)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(SpotifyGreen, Color(0xFF00B377))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = "Beatify",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = greeting,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Discover & Stream Music",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpotifyGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Voice Search shortcut
                                IconButton(
                                    onClick = { isVoiceSearchOpen = true },
                                    modifier = Modifier.testTag("home_voice_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Voice Search",
                                        tint = SpotifyGreen
                                    )
                                }

                                // Refresh daily songs shortcut
                                IconButton(
                                    onClick = {
                                        TactileFeedbackHelper.performTick(context, haptic)
                                        viewModel.refreshDailyTracks()
                                    },
                                    modifier = Modifier.testTag("home_refresh_daily_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Daily Hits",
                                        tint = if (isRefreshingDaily) SpotifyGreen else TextSecondary
                                    )
                                }

                                // Quick offline mode toggle icon
                                IconButton(
                                    onClick = { viewModel.isOfflineMode.value = !isOfflineMode },
                                    modifier = Modifier.testTag("offline_toggle_icon")
                                ) {
                                    Icon(
                                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                                        contentDescription = "Offline Mode",
                                        tint = if (isOfflineMode) SpotifyGreen else TextSecondary
                                    )
                                }

                                // Settings shortcut
                                IconButton(
                                    onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                                    modifier = Modifier.testTag("home_settings_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }

                        // Daily Refresh Live Banner
                        if (isRefreshingDaily || dailyRefreshMessage != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                color = SpotifyGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isRefreshingDaily) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = SpotifyGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = dailyRefreshMessage ?: "Fetching today's new trending songs...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                    // Network Streaming & Offline Banner
                    if (isOfflineMode) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = SpotifyGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Offline Mode Active • Playing Downloaded Songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "High-Fidelity Live Internet Streaming • Studio Masters",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick 6-Grid cards (Spotify iconic top grid)
            item {
                val quickItems = displayedSongs.take(6)
                if (quickItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        val rows = quickItems.chunked(2)
                        rows.forEach { rowSongs ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSongs.forEach { song ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.playSong(song, displayedSongs) }
                                            .testTag("quick_card_${song.id}"),
                                        color = DarkCard
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AlbumArtBox(
                                                startColorHex = song.coverGradientStart,
                                                endColorHex = song.coverGradientEnd,
                                                imageUrl = song.imageUrl,
                                                modifier = Modifier.size(56.dp),
                                                title = song.title,
                                                iconSize = 20
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentSong?.id == song.id) SpotifyGreen else TextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 6.dp)
                                            )
                                            if (currentSong?.id == song.id && isPlaying) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Playing",
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowSongs.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: ARTISTS YOU FOLLOW (फॉलोअर्स लिस्ट)
            // ========================================================
            item {
                SectionHeader(
                    title = "⭐ Artists You Follow (फॉलोअर्स लिस्ट)",
                    subtitle = "Tap like/follow for big emoji celebration animation"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(curatedArtists) { artist ->
                        val isFollowed = followedArtists.contains(artist.name)
                        ArtistFollowCard(
                            artist = artist,
                            isFollowed = isFollowed,
                            onToggleFollow = {
                                viewModel.toggleFollowArtist(artist.name)
                            },
                            onArtistClick = {
                                viewModel.openArtist(artist.name)
                            }
                        )
                    }
                }
            }

            // ========================================================
            // SECTION: TODAY'S FRESH DAILY SONGS (दैनिक नए गाने - रिफ्रेश होने वाले)
            // ========================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (lang == com.example.util.AppLanguage.HINDI) "✨ आज के नए गाने (Daily Fresh Hits)" else "✨ Today's Daily Fresh Drop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (lang == com.example.util.AppLanguage.HINDI) "नीचे स्क्रॉल / रिफ्रेश करने पर नए गाने अपडेट होते हैं" else "Auto-rotates & updates on pull-down refresh",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyGreen,
                            fontSize = 11.sp
                        )
                    }

                    // Soft interactive refresh button
                    IconButton(
                        onClick = {
                            TactileFeedbackHelper.performTick(context, haptic)
                            viewModel.refreshDailyTracks()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Daily Songs",
                            tint = if (isRefreshingDaily) SpotifyGreen else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                val dailyTracks = if (dailySongs.isNotEmpty()) dailySongs else displayedSongs.shuffled()
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(dailyTracks.take(8)) { song ->
                        HomeSongCard(
                            song = song,
                            isCurrent = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            isDownloading = downloadingIds.contains(song.id),
                            onPlay = { viewModel.playSongWithAutoInsert(song, dailyTracks) },
                            onFavorite = { viewModel.toggleFavorite(song) },
                            onDownload = { viewModel.toggleDownload(song) }
                        )
                    }
                }
            }

            // ========================================================
            // SECTION: TRENDING LIST (ट्रेंडिंग लिस्ट वाला रहे)
            // ========================================================
            item {
                SectionHeader(
                    title = "🔥 Trending Right Now (Top 50 Chart)",
                    subtitle = "Real-time streaming hits across the internet"
                )
            }

            val trendingTracks = displayedSongs.take(8)
            itemsIndexed(trendingTracks) { index, song ->
                TrendingSongRankRow(
                    rank = index + 1,
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isPlaying = isPlaying,
                    isDownloading = downloadingIds.contains(song.id),
                    onPlay = { viewModel.playSong(song, displayedSongs) },
                    onFavorite = { viewModel.toggleFavorite(song) },
                    onDownload = { viewModel.toggleDownload(song) }
                )
            }

            // ========================================================
            // SECTION: BOLLYWOOD BLOCKBUSTERS (बॉलीवुड टॉप हिट्स)
            // ========================================================
            if (bollywoodSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🎬 Bollywood Blockbusters (बॉलीवुड हिट्स)",
                        subtitle = "Arijit Singh, Pritam, Vishal Mishra, Jubin Nautiyal"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(bollywoodSongs) { song ->
                            HomeSongCard(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                isDownloading = downloadingIds.contains(song.id),
                                onPlay = { viewModel.playSong(song, displayedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) }
                            )
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: PUNJABI WAVE (पंजाबी हिट्स)
            // ========================================================
            if (punjabiSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "⚡ Punjabi Wave (पंजाबी हिट्स)",
                        subtitle = "Diljit Dosanjh, Sidhu Moose Wala, Karan Aujla, Shubh"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(punjabiSongs) { song ->
                            HomeSongCard(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                isDownloading = downloadingIds.contains(song.id),
                                onPlay = { viewModel.playSong(song, displayedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) }
                            )
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: GLOBAL CHARTBUSTERS (इंटरनेशनल टॉप)
            // ========================================================
            if (globalHits.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🌍 Global Chartbusters (ग्लोबल हिट्स)",
                        subtitle = "The Weeknd, Taylor Swift, Ed Sheeran, Dua Lipa"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(globalHits) { song ->
                            HomeSongCard(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                isDownloading = downloadingIds.contains(song.id),
                                onPlay = { viewModel.playSong(song, displayedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) }
                            )
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: ROMANCE & MELODIES (रोमांटिक गाने)
            // ========================================================
            if (romanceSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "💖 Romance & Pure Melodies",
                        subtitle = "Timeless love songs streaming in high quality"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(romanceSongs) { song ->
                            HomeSongCard(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                isDownloading = downloadingIds.contains(song.id),
                                onPlay = { viewModel.playSong(song, displayedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) }
                            )
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: OFFLINE DOWNLOADED SONGS (डाउनलोड किए गए गाने)
            // ========================================================
            if (downloadedSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "📥 Offline Songs (डाउनलोड किए गए गाने)",
                        subtitle = "${downloadedSongs.size} songs saved to device • Play without internet"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(downloadedSongs) { song ->
                            HomeSongCard(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                isDownloading = false,
                                onPlay = { viewModel.playSong(song, downloadedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) }
                            )
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: FEATURED PLAYLISTS
            // ========================================================
            if (playlists.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🎧 Handcrafted Playlists",
                        subtitle = "Curated playlists for every mood"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(playlists) { pl ->
                            Surface(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.openPlaylist(pl) }
                                    .testTag("home_playlist_${pl.id}"),
                                color = DarkCard
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(126.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(pl.coverColorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueueMusic,
                                            contentDescription = pl.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = pl.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pl.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ========================================================
            // SECTION: JUMP BACK IN (हाल ही में सुने गए गाने)
            // ========================================================
            item {
                SectionHeader(
                    title = "Recently Played & More Hits",
                    subtitle = "Pick up right where you left off"
                )
            }

            items(displayedSongs.drop(8).take(10)) { song ->
                SongListItemWithDownload(
                    song = song,
                    isCurrent = currentSong?.id == song.id,
                    isPlaying = isPlaying,
                    isDownloading = downloadingIds.contains(song.id),
                    onPlay = { viewModel.playSong(song, displayedSongs) },
                    onFavorite = { viewModel.toggleFavorite(song) },
                    onDownload = { viewModel.toggleDownload(song) },
                    onShare = { viewModel.openShareDialog(song) }
                )
            }

            // ========================================================
            // SECTION: HOME SCREEN WIDGETS SHOWCASE (चार-पांच टाइप के विजेट्स)
            // ========================================================
            item {
                com.example.ui.components.HomeWidgetShowcaseSection(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    onPrev = { viewModel.playPrevious() }
                )
            }

            // ========================================================
            // SECTION: USER REVIEWS & RATINGS (10-12 डेली रिव्यू जो कंटिन्यू चलते रहें)
            // ========================================================
            item {
                UserReviewsSection(
                    reviews = appReviews,
                    onWriteReviewClick = {
                        viewModel.navigateTo(Screen.SETTINGS)
                    }
                )
            }
        }
    }

        // Big Celebratory Emoji Animation Overlay when Artist is Liked/Followed
        ArtistCelebrationOverlay(
            artistName = celebrationArtist,
            isVisible = isCelebrationVisible,
            onDismiss = { viewModel.dismissCelebration() }
        )

        // Voice to Song Search Overlay Dialog
        VoiceSearchDialog(
            isOpen = isVoiceSearchOpen,
            onDismiss = { isVoiceSearchOpen = false },
            onSpeechResult = { recognized ->
                viewModel.searchOnline(recognized)
                viewModel.navigateTo(Screen.SEARCH)
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

// Artist Card for "Artists You Follow" section with like button, follower count, and big emoji trigger
@Composable
fun ArtistFollowCard(
    artist: HomeArtist,
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onArtistClick() }
            .testTag("artist_card_${artist.name}"),
        color = DarkCard,
        border = BorderStroke(1.dp, if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            // Circular Artist Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified",
                    tint = SpotifyGreen,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = "${artist.monthlyListeners} Listeners",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Like Heart + Follow Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Heart Button
                IconButton(
                    onClick = onToggleFollow,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isFollowed) Color(0xFFFF2A68).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                        .testTag("like_artist_${artist.name}")
                ) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Artist",
                        tint = if (isFollowed) Color(0xFFFF2A68) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Follow Button
                Button(
                    onClick = onToggleFollow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.15f),
                        contentColor = if (isFollowed) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isFollowed) "Following" else "Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Numbered Trending Row for Trending Top 50 Chart
@Composable
fun TrendingSongRankRow(
    rank: Int,
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextMuted
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .testTag("trending_rank_$rank"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number badge
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = rankBadgeColor
            )
        }

        AlbumArtBox(
            startColorHex = song.coverGradientStart,
            endColorHex = song.coverGradientEnd,
            imageUrl = song.imageUrl,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            title = song.title,
            iconSize = 20
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) SpotifyGreen else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isDownloaded) {
                    Icon(
                        imageVector = Icons.Filled.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                } else {
                    Text(
                        text = "LIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        modifier = Modifier
                            .background(SpotifyGreen.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Download Action Button
        IconButton(onClick = onDownload) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = SpotifyGreen,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (song.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.CloudDownload,
                    contentDescription = if (song.isDownloaded) "Downloaded" else "Download",
                    tint = if (song.isDownloaded) SpotifyGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Favorite Action Button
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (song.isFavorite) SpotifyGreen else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// High-fidelity Song Card for horizontal shelves
@Composable
fun HomeSongCard(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(155.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .testTag("home_song_card_${song.id}"),
        color = DarkCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AlbumArtBox(
                    startColorHex = song.coverGradientStart,
                    endColorHex = song.coverGradientEnd,
                    imageUrl = song.imageUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    title = song.title,
                    iconSize = 36
                )

                // Play / Pause badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) SpotifyGreen else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = song.genre,
                    style = MaterialTheme.typography.labelSmall,
                    color = SpotifyGreen,
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Download icon button
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(26.dp)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = SpotifyGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (song.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.CloudDownload,
                                contentDescription = "Download",
                                tint = if (song.isDownloaded) SpotifyGreen else TextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = onFavorite,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (song.isFavorite) SpotifyGreen else TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

// Vertical list item with download & favorite
@Composable
fun SongListItemWithDownload(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtBox(
            startColorHex = song.coverGradientStart,
            endColorHex = song.coverGradientEnd,
            imageUrl = song.imageUrl,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            title = song.title,
            iconSize = 20
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) SpotifyGreen else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "${song.artist} • ${song.genre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(onClick = onDownload) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = SpotifyGreen,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (song.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.CloudDownload,
                    contentDescription = "Download",
                    tint = if (song.isDownloaded) SpotifyGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (song.isFavorite) SpotifyGreen else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    SongListItemWithDownload(
        song = song,
        isCurrent = isCurrent,
        isPlaying = isPlaying,
        isDownloading = false,
        onPlay = onPlay,
        onFavorite = onFavorite,
        onDownload = onDownload,
        onShare = onShare,
        modifier = modifier
    )
}

@Composable
fun UserReviewsSection(
    reviews: List<AppReview>,
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⭐ Listener Reviews & Ratings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "4.9 ★ Rating from 24,000+ Listeners • Daily Live Reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyGreen
                )
            }

            TextButton(
                onClick = onWriteReviewClick,
                colors = ButtonDefaults.textButtonColors(contentColor = SpotifyGreen)
            ) {
                Text(
                    text = "Write Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(reviews.take(15)) { review ->
                ReviewCardItem(review = review)
            }
        }
    }
}

@Composable
fun ReviewCardItem(
    review: AppReview,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag("review_card_${review.id}"),
        color = DarkCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Avatar, Name, Verified Badge, Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(review.avatarGradientStart), Color(review.avatarGradientEnd))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (review.verifiedUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified Listener",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = review.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Star Rating (1 to 5 gold stars)
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { starIdx ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (starIdx < review.rating) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${review.rating}.0",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment text
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

