package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Playlist
import com.example.data.Song
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.util.AppStrings

enum class LibraryTab {
    PLAYLISTS, DOWNLOADED, FAVORITES, ARTISTS
}

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }
    val playlists by viewModel.allPlaylists.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val totalStorageMb = remember(downloadedSongs) {
        val sum = downloadedSongs.sumOf { it.downloadSizeMb }
        Math.round(sum * 10.0) / 10.0
    }

    BackHandler {
        if (selectedTab != LibraryTab.PLAYLISTS) {
            selectedTab = LibraryTab.PLAYLISTS
        } else {
            viewModel.handleBackPress()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Header
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
                Text(
                    text = AppStrings.get("nav_library", lang),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = { viewModel.isCreatePlaylistDialogOpen.value = true },
                    modifier = Modifier.testTag("create_playlist_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Offline Mode Toggle Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = DarkCard
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (isOfflineMode) SpotifyGreen else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.get("offline_mode", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${AppStrings.get("storage_used", lang)}: $totalStorageMb MB (${downloadedSongs.size} tracks)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { viewModel.isOfflineMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = SpotifyGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Row (Playlists, Downloaded, Liked)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == LibraryTab.PLAYLISTS,
                    onClick = { selectedTab = LibraryTab.PLAYLISTS },
                    label = { Text("Playlists (${playlists.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkCard,
                        labelColor = TextPrimary
                    )
                )

                FilterChip(
                    selected = selectedTab == LibraryTab.DOWNLOADED,
                    onClick = { selectedTab = LibraryTab.DOWNLOADED },
                    label = { Text("${AppStrings.get("downloaded", lang)} (${downloadedSongs.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkCard,
                        labelColor = TextPrimary
                    )
                )

                FilterChip(
                    selected = selectedTab == LibraryTab.FAVORITES,
                    onClick = { selectedTab = LibraryTab.FAVORITES },
                    label = { Text("${AppStrings.get("favorites", lang)} (${favoriteSongs.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkCard,
                        labelColor = TextPrimary
                    )
                )

                FilterChip(
                    selected = selectedTab == LibraryTab.ARTISTS,
                    onClick = { selectedTab = LibraryTab.ARTISTS },
                    label = { Text("Artists (${followedArtists.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkCard,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            LibraryTab.PLAYLISTS -> {
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(AppStrings.get("empty_playlists", lang), color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(playlists) { pl ->
                            PlaylistItemRow(
                                playlist = pl,
                                onClick = { viewModel.openPlaylist(pl) },
                                onDelete = { viewModel.deletePlaylist(pl.id) }
                            )
                        }
                    }
                }
            }

            LibraryTab.DOWNLOADED -> {
                if (downloadedSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.get("empty_downloads", lang),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(downloadedSongs) { song ->
                            SongListItem(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                onPlay = { viewModel.playSong(song, downloadedSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) },
                                onShare = { viewModel.openShareDialog(song) }
                            )
                        }
                    }
                }
            }

            LibraryTab.FAVORITES -> {
                if (favoriteSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No liked songs yet. Tap the heart on any song to save it here!", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(favoriteSongs) { song ->
                            SongListItem(
                                song = song,
                                isCurrent = currentSong?.id == song.id,
                                isPlaying = isPlaying,
                                onPlay = { viewModel.playSong(song, favoriteSongs) },
                                onFavorite = { viewModel.toggleFavorite(song) },
                                onDownload = { viewModel.toggleDownload(song) },
                                onShare = { viewModel.openShareDialog(song) }
                            )
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                if (followedArtists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No followed artists yet. Follow artists to see their songs & playlists here!", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(followedArtists.toList()) { artistName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openArtist(artistName) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .testTag("followed_artist_row_$artistName"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SpotifyGreen, Color(0xFF101010))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artistName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 22.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = artistName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Artist • Tap to view all songs & playlist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SpotifyGreen
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open Artist",
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("playlist_row_${playlist.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(playlist.coverColorHex)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = playlist.name,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = playlist.description.ifEmpty { "Created by you" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (playlist.isCustom) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Playlist",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
