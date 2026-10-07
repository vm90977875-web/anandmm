package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.util.AppStrings

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val onlineResults by viewModel.onlineSearchResults.collectAsState()
    val isSearchingOnline by viewModel.isSearchingOnline.collectAsState()
    val downloadingIds by viewModel.downloadingSongIds.collectAsState()
    val followedArtists by viewModel.followedArtists.collectAsState()
    val isCelebrationVisible by viewModel.isCelebrationVisible.collectAsState()
    val celebrationArtist by viewModel.celebrationArtist.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isVoiceSearchOpen by remember { mutableStateOf(false) }

    val basePool = if (isOfflineMode) downloadedSongs else allSongs

    BackHandler {
        if (query.isNotBlank()) {
            query = ""
            viewModel.searchOnline("")
        } else {
            viewModel.handleBackPress()
        }
    }

    // Trigger online search whenever query changes
    LaunchedEffect(query) {
        if (!isOfflineMode) {
            viewModel.searchOnline(query)
        }
    }

    // Local matches
    val localMatches = remember(query, basePool) {
        if (query.isBlank()) emptyList() else {
            basePool.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true) ||
                song.genre.contains(query, ignoreCase = true)
            }
        }
    }

    // Combine unique songs: online results first, plus any local matches not already included
    val combinedResults = remember(onlineResults, localMatches) {
        val seen = mutableSetOf<String>()
        val list = mutableListOf<Song>()
        for (s in onlineResults) {
            val key = s.title.lowercase().trim() + "_" + s.artist.lowercase().trim()
            if (seen.add(key)) {
                list.add(s)
            }
        }
        for (s in localMatches) {
            val key = s.title.lowercase().trim() + "_" + s.artist.lowercase().trim()
            if (seen.add(key)) {
                list.add(s)
            }
        }
        list
    }

    // Detect matched artist for Spotify-style Top Result Artist Card
    val detectedArtist = remember(query, combinedResults) {
        if (query.length >= 2) {
            val q = query.trim()
            // Check top song's artist or popular names
            combinedResults.firstOrNull()?.artist?.split(",", "&", "feat.")?.firstOrNull()?.trim() ?: q
        } else null
    }

    val popularArtists = listOf(
        "Arijit Singh",
        "Sidhu Moose Wala",
        "Diljit Dosanjh",
        "The Weeknd",
        "Taylor Swift",
        "Karan Aujla",
        "Pritam",
        "Shreya Ghoshal",
        "Badshah",
        "Dua Lipa"
    )

    val browseCategories = listOf(
        Triple("Bollywood Hits", 0xFFFF5E3A, 0xFFFF2A68),
        Triple("Punjabi Wave", 0xFF8E0E00, 0xFF1F1C18),
        Triple("Global Top 50", 0xFF654EA3, 0xFFEAAFC8),
        Triple("Lo-Fi & Chill", 0xFF2C3E50, 0xFF3498DB),
        Triple("Romance & Melodies", 0xFF11998E, 0xFF38EF7D),
        Triple("EDM & Dance", 0xFFF37335, 0xFFFDC830)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = AppStrings.get("nav_search", lang),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Spotify-style Search Input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "What do you want to listen to?",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (query.isNotBlank()) SpotifyGreen else TextSecondary
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (isSearchingOnline) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = SpotifyGreen,
                                strokeWidth = 2.dp
                            )
                        } else if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    query = ""
                                    viewModel.searchOnline("")
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Voice Search Mic Button
                        IconButton(
                            onClick = { isVoiceSearchOpen = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("voice_search_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice to Song Search",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Quick Artist Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularArtists) { artist ->
                val isSelected = query.equals(artist, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) SpotifyGreen else Color(0xFF222222),
                    border = BorderStroke(1.dp, if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            query = artist
                            viewModel.searchOnline(artist)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎤",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Area
        if (query.isBlank()) {
            // ==========================================
            // DEFAULT / EXPLORE SCREEN
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Browse all internet genres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                    )
                }

                // Grid of Browse Categories
                items(browseCategories.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        query = item.first
                                        viewModel.searchOnline(item.first)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(item.second), Color(item.third))
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    )
                                    Text(
                                        text = "🎵",
                                        fontSize = 28.sp,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Featured Library Tracks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(basePool.take(8)) { song ->
                    SearchSongRow(
                        song = song,
                        isCurrent = currentSong?.id == song.id,
                        isPlaying = isPlaying,
                        isDownloading = downloadingIds.contains(song.id),
                        onPlay = { viewModel.playSongWithAutoInsert(song, basePool) },
                        onFavorite = { viewModel.toggleFavorite(song) },
                        onDownload = { viewModel.toggleDownload(song) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            // ==========================================
            // SEARCH RESULTS SCREEN (LIVE INTERNET SONGS)
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Spotify-style Top Artist Card with Follow & Emoji Explosion Button
                if (detectedArtist != null && detectedArtist.isNotBlank()) {
                    val isFollowed = followedArtists.contains(detectedArtist)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkCard,
                            border = BorderStroke(
                                1.dp,
                                if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Artist Avatar Bubble
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF2E7D32), SpotifyGreen)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = detectedArtist.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 24.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.openArtist(detectedArtist) }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = detectedArtist,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified Artist",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "Artist • View All Songs & Playlist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SpotifyGreen
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Dedicated Big Like / Heart Button
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFollowArtist(detectedArtist)
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFollowed) Color(0xFFFF2A68).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f)
                                            )
                                            .testTag("like_artist_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowed) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Like Artist",
                                            tint = if (isFollowed) Color(0xFFFF2A68) else Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Follow Pill Button
                                    Button(
                                        onClick = {
                                            viewModel.toggleFollowArtist(detectedArtist)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowed) SpotifyGreen else Color.White.copy(alpha = 0.12f),
                                            contentColor = if (isFollowed) Color.Black else Color.White
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("follow_artist_btn")
                                    ) {
                                        Text(
                                            text = if (isFollowed) "Following" else "Follow",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Header info
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSearchingOnline) "Searching live internet catalog..." else "Songs (${combinedResults.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        if (!isOfflineMode) {
                            Text(
                                text = "🌐 Online Streaming",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (combinedResults.isEmpty() && !isSearchingOnline) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🔍",
                                    fontSize = 42.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No songs found for \"$query\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Try searching for an artist like \"Arijit Singh\" or song \"Kesariya\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(combinedResults) { song ->
                        SearchSongRow(
                            song = song,
                            isCurrent = currentSong?.id == song.id,
                            isPlaying = isPlaying,
                            isDownloading = downloadingIds.contains(song.id),
                            onPlay = {
                                viewModel.playSongWithAutoInsert(song, combinedResults)
                            },
                            onFavorite = { viewModel.toggleFavorite(song) },
                            onDownload = { viewModel.toggleDownload(song) }
                        )
                    }
                }
            }
        }
    }

    // Massive emoji explosion animation overlay on Artist Like/Follow
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
            query = recognized
            viewModel.searchOnline(recognized)
        }
    )
}
}

@Composable
fun SearchSongRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
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
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            title = song.title,
            iconSize = 22
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
                        text = "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        modifier = Modifier
                            .background(
                                SpotifyGreen.copy(alpha = 0.15f),
                                RoundedCornerShape(3.dp)
                            )
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
        IconButton(
            onClick = onDownload,
            modifier = Modifier.testTag("download_btn_${song.id}")
        ) {
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
                    modifier = Modifier.size(22.dp)
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
