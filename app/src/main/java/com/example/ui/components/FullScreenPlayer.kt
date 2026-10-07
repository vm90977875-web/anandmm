package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.RepeatMode
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.AppStrings
import com.example.util.TactileFeedbackHelper

@Composable
fun FullScreenPlayer(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.songDurationMs.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val streamingQuality by viewModel.streamingQuality.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val isLyricsExpanded by viewModel.isLyricsExpanded.collectAsState()
    val activeLyricIdx by viewModel.activeLyricIndex.collectAsState()
    val parsedLyrics by viewModel.parsedLyrics.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    if (song == null) return
    val s = song!!

    val cleanArtist = remember(s.artist) {
        s.artist.split(",", "&", "feat.", "ft.", "/").firstOrNull()?.trim() ?: s.artist.trim()
    }

    val artistPlaylist = remember(cleanArtist, allSongs) {
        val matches = allSongs.filter {
            it.artist.contains(cleanArtist, ignoreCase = true) || it.title.contains(cleanArtist, ignoreCase = true)
        }
        if (matches.isNotEmpty()) matches else listOf(s)
    }

    BackHandler {
        if (isLyricsExpanded) {
            viewModel.isLyricsExpanded.value = false
        } else {
            viewModel.isFullScreenPlayerOpen.value = false
        }
    }

    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableFloatStateOf(0f) }

    val effectivePosition = if (isUserDraggingSlider) {
        sliderTempPosition.toLong()
    } else {
        currentPositionMs
    }

    val sliderValue = if (durationMs > 0) {
        (effectivePosition.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val bgGradient = remember(s.coverGradientStart, s.coverGradientEnd) {
        Brush.verticalGradient(
            colors = listOf(
                Color(s.coverGradientStart).copy(alpha = 0.65f),
                Color(0xFF161616),
                Color(0xFF121212)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("fullscreen_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.isFullScreenPlayerOpen.value = false },
                    modifier = Modifier.testTag("close_player_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM COLLECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = s.album,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.openShareDialog(s) },
                    modifier = Modifier.testTag("player_share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Album Artwork
            AlbumArtBox(
                startColorHex = s.coverGradientStart,
                endColorHex = s.coverGradientEnd,
                imageUrl = s.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                title = s.title,
                iconSize = 64,
                cornerRadius = 16
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Track Title, Artist, and Like / Download Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                viewModel.openArtist(s.artist)
                                viewModel.isFullScreenPlayerOpen.value = false
                            }
                            .padding(vertical = 2.dp)
                            .testTag("player_artist_name_clickable")
                    ) {
                        Text(
                            text = s.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Artist Playlist",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Download indicator / button
                    IconButton(
                        onClick = { viewModel.toggleDownload(s) },
                        modifier = Modifier.testTag("player_download_btn")
                    ) {
                        Icon(
                            imageVector = if (s.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                            contentDescription = "Download",
                            tint = if (s.isDownloaded) SpotifyGreen else TextSecondary
                        )
                    }

                    // Like Button
                    IconButton(
                        onClick = {
                            TactileFeedbackHelper.performHeartbeatHaptic(context, haptic)
                            viewModel.toggleFavorite(s)
                        },
                        modifier = Modifier.testTag("player_favorite_btn")
                    ) {
                        Icon(
                            imageVector = if (s.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (s.isFavorite) SpotifyGreen else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrub Bar Slider
            Slider(
                value = sliderValue,
                onValueChange = { frac ->
                    isUserDraggingSlider = true
                    sliderTempPosition = frac * durationMs
                },
                onValueChangeFinished = {
                    viewModel.seekTo(sliderTempPosition.toLong())
                    isUserDraggingSlider = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = TextPrimary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_scrub_bar")
            )

            // Timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(effectivePosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = formatMs(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.testTag("shuffle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) SpotifyGreen else TextSecondary
                    )
                }

                // Previous button
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier.testTag("prev_song_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause central button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("main_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next button
                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier.testTag("next_song_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat button
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.testTag("repeat_btn")
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            RepeatMode.ALL -> Icons.Default.Repeat
                            RepeatMode.OFF -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) SpotifyGreen else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Quality & Playlist Add Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Quality & Equalizer Studio Shortcut Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        viewModel.isFullScreenPlayerOpen.value = false
                        viewModel.navigateTo(Screen.EQUALIZER)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer & Booster",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EQ & Booster • $streamingQuality",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.openAddToPlaylistDialog(s) },
                    modifier = Modifier.testTag("add_to_playlist_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Add to playlist",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-Time Lyrics Card preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.isLyricsExpanded.value = true }
                    .testTag("lyrics_card_preview"),
                color = Color(s.coverGradientEnd).copy(alpha = 0.35f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("lyrics", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FULL SCREEN",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Expand Lyrics",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Show current active lyric lines
                    if (parsedLyrics.isNotEmpty()) {
                        val activeLine = parsedLyrics.getOrNull(activeLyricIdx)?.text ?: parsedLyrics.first().text
                        val nextLine = parsedLyrics.getOrNull(activeLyricIdx + 1)?.text ?: ""

                        Text(
                            text = activeLine,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        if (nextLine.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nextLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = AppStrings.get("no_lyrics", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================================
            // ARTIST PLAYLIST (और जब कोई सॉन्ग चले तो नीचे भी उस आर्टिस्ट के सारे गाने प्लेलिस्ट के रूप में हों)
            // ==========================================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("player_artist_playlist_card"),
                color = Color(0xFF1E1E1E),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "More by $cleanArtist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Artist Songs Playlist • इस आर्टिस्ट के सभी गाने",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.openArtist(s.artist)
                                viewModel.isFullScreenPlayerOpen.value = false
                            }
                        ) {
                            Text(
                                text = "View All",
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of songs by the artist
                    val displayList = artistPlaylist.take(6)
                    displayList.forEachIndexed { idx, track ->
                        val isTrackPlaying = s.id == track.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.playSong(track, artistPlaylist)
                                }
                                .background(if (isTrackPlaying) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AlbumArtBox(
                                startColorHex = track.coverGradientStart,
                                endColorHex = track.coverGradientEnd,
                                imageUrl = track.imageUrl,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                title = track.title,
                                iconSize = 16
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isTrackPlaying) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTrackPlaying) SpotifyGreen else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.album,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { viewModel.playSong(track, artistPlaylist) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTrackPlaying && isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                    contentDescription = "Play",
                                    tint = if (isTrackPlaying) SpotifyGreen else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Full-screen lyrics sheet overlay
        if (isLyricsExpanded) {
            LyricsOverlay(
                viewModel = viewModel,
                song = s,
                onClose = { viewModel.isLyricsExpanded.value = false }
            )
        }
    }
}

fun formatMs(ms: Long): String {
    val totalSec = ms / 1000L
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
