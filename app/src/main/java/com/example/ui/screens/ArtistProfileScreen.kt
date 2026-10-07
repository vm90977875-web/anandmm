package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Song
import com.example.ui.components.AlbumArtBox
import com.example.ui.components.ArtistCelebrationOverlay
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val artistName by viewModel.selectedArtistName.collectAsState()
    val artistSongs by viewModel.artistSongs.collectAsState()
    val isArtistLoading by viewModel.isArtistLoading.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val downloadingIds by viewModel.downloadingSongIds.collectAsState()
    val isCelebrationVisible by viewModel.isCelebrationVisible.collectAsState()
    val celebrationArtist by viewModel.celebrationArtist.collectAsState()

    val currentArtist = artistName ?: "Artist"
    val isFollowed = followedArtists.contains(currentArtist)

    // Intercept back button to navigate back cleanly without exiting app
    BackHandler {
        viewModel.handleBackPress()
    }

    // Curated artist artwork and metadata map for high-fidelity presentation
    val artistImageUrl = remember(currentArtist, artistSongs) {
        val songWithImage = artistSongs.firstOrNull { it.imageUrl.isNotBlank() }
        if (songWithImage != null && songWithImage.imageUrl.isNotBlank()) {
            songWithImage.imageUrl
        } else {
            when {
                currentArtist.contains("Arijit", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/9f/13/ca/9f13ca3b-e533-03e0-f19a-f0aaa774581d/196589311191.jpg/600x600bb.jpg"
                currentArtist.contains("Diljit", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/8a/89/e4/8a89e445-d2c6-f8ac-a828-27818b0c1afe/859749638209_cover.jpg/600x600bb.jpg"
                currentArtist.contains("Sidhu", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/97/69/58/976958ae-725e-bd41-6755-f0921c697840/810063889609_cover.jpg/600x600bb.jpg"
                currentArtist.contains("Karan Aujla", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/d3/08/bc/d308bc6a-20e1-6532-d933-35d1b429210e/5054197755538.jpg/600x600bb.jpg"
                currentArtist.contains("Weeknd", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/b5/92/bb/b592bb72-52e3-e756-9b26-9f56d08f47ab/16UMGIM67864.rgb.jpg/600x600bb.jpg"
                currentArtist.contains("Taylor", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/49/3d/ab/493dab54-f920-9043-6181-80993b8116c9/19UMGIM53909.rgb.jpg/600x600bb.jpg"
                currentArtist.contains("Pritam", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/2d/a1/b8/2da1b888-ff70-edc8-1c2e-01d251a5ca10/197189341342.jpg/600x600bb.jpg"
                currentArtist.contains("Shubh", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/dc/46/a9/dc46a9c9-794e-2d7a-1afb-97eb4ae0fff6/197188915704.jpg/600x600bb.jpg"
                currentArtist.contains("Ed Sheeran", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg"
                else -> ""
            }
        }
    }

    val monthlyListeners = remember(currentArtist) {
        when {
            currentArtist.contains("Arijit", ignoreCase = true) -> "48.6M Monthly Listeners"
            currentArtist.contains("Diljit", ignoreCase = true) -> "33.2M Monthly Listeners"
            currentArtist.contains("Sidhu", ignoreCase = true) -> "29.4M Monthly Listeners"
            currentArtist.contains("Weeknd", ignoreCase = true) -> "108.5M Monthly Listeners"
            currentArtist.contains("Taylor", ignoreCase = true) -> "98.2M Monthly Listeners"
            currentArtist.contains("Karan Aujla", ignoreCase = true) -> "25.8M Monthly Listeners"
            currentArtist.contains("Pritam", ignoreCase = true) -> "38.1M Monthly Listeners"
            else -> "22.5M Monthly Listeners"
        }
    }

    val otherArtists = remember {
        listOf("Arijit Singh", "Diljit Dosanjh", "Sidhu Moose Wala", "Karan Aujla", "The Weeknd", "Taylor Swift", "Pritam", "Shubh")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("artist_profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Hero Header Section with Artwork & Gradient
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    // Artist Banner Artwork
                    if (artistImageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artistImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentArtist,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF121212))
                                    )
                                )
                        )
                    }

                    // Scrim gradient for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.2f),
                                        Color(0xFF121212)
                                    )
                                )
                            )
                    )

                    // Navigation Bar (Back Arrow & Options)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.handleBackPress() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("artist_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { /* Share Artist */ },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share Artist",
                                tint = Color.White
                            )
                        }
                    }

                    // Artist Info overlay at bottom of banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified Artist",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Artist • सत्यापित कलाकार",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentArtist,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = monthlyListeners,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Interactive Controls Row (Follow, Play All, Shuffle)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Follow / Following Button (Triggers Big Celebratory Emoji Animation)
                        Button(
                            onClick = { viewModel.toggleFollowArtist(currentArtist) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.14f),
                                contentColor = if (isFollowed) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("artist_follow_btn")
                        ) {
                            Text(
                                text = if (isFollowed) "Following (फॉलो किया)" else "Follow (फॉलो करें)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Heart / Like Artist Button
                        IconButton(
                            onClick = { viewModel.toggleFollowArtist(currentArtist) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFollowed) Color(0xFFFF2A68).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f)
                                )
                                .testTag("artist_like_heart_btn")
                        ) {
                            Icon(
                                imageVector = if (isFollowed) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like Artist",
                                tint = if (isFollowed) Color(0xFFFF2A68) else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Shuffle Button
                        IconButton(
                            onClick = {
                                if (artistSongs.isNotEmpty()) {
                                    val shuffled = artistSongs.shuffled()
                                    viewModel.playSong(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Big Play All FAB
                        FloatingActionButton(
                            onClick = {
                                if (artistSongs.isNotEmpty()) {
                                    viewModel.playSong(artistSongs.first(), artistSongs)
                                }
                            },
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("artist_play_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Playlist Header
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
                            text = "All Songs & Tracks (कलाकार के सभी गाने)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${artistSongs.size} Songs available • High-Fidelity Master Audio",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    if (isArtistLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SpotifyGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // Empty state if no songs found
            if (artistSongs.isEmpty() && !isArtistLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Finding songs by $currentArtist...",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Artist Songs Playlist List
            itemsIndexed(artistSongs) { index, song ->
                val isCurrent = currentSong?.id == song.id
                val isDl = downloadingIds.contains(song.id)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playSong(song, artistSongs) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("artist_song_${song.id}"),
                    color = if (isCurrent) DarkCard.copy(alpha = 0.8f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track Number or Equalizer indicator
                        Box(
                            modifier = Modifier.width(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent && isPlaying) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Playing",
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCurrent) SpotifyGreen else TextMuted,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Artwork Thumbnail
                        AlbumArtBox(
                            startColorHex = song.coverGradientStart,
                            endColorHex = song.coverGradientEnd,
                            imageUrl = song.imageUrl,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            title = song.title,
                            iconSize = 20
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Album
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
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "${song.album} • ${formatDuration(song.durationMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Download Button
                        IconButton(
                            onClick = { viewModel.toggleDownload(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isDl) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
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

                        // Like Heart Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (song.isFavorite) SpotifyGreen else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Options / 3 dots menu
                        IconButton(
                            onClick = { viewModel.openAddToPlaylistDialog(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Fans Also Like Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Fans Also Like (अन्य लोकप्रिय कलाकार)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explore similar trending artists",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(otherArtists.filter { !it.equals(currentArtist, ignoreCase = true) }) { relatedName ->
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.openArtist(relatedName) },
                            color = DarkCard
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(Color(0xFF1DB954), Color(0xFF191414)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = relatedName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 22.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = relatedName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Artist",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Celebratory Emoji Overlay when artist is followed
        ArtistCelebrationOverlay(
            artistName = celebrationArtist,
            isVisible = isCelebrationVisible,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
