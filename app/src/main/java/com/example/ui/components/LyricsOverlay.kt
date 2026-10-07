package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.util.AppStrings
import kotlinx.coroutines.launch

@Composable
fun LyricsOverlay(
    viewModel: MusicViewModel,
    song: Song,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedLyrics by viewModel.parsedLyrics.collectAsState()
    val activeIdx by viewModel.activeLyricIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Smoothly auto-scroll when active lyric line updates
    LaunchedEffect(activeIdx) {
        if (activeIdx >= 0 && activeIdx < parsedLyrics.size) {
            val target = (activeIdx - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    val bgGradient = remember(song.coverGradientStart, song.coverGradientEnd) {
        Brush.verticalGradient(
            colors = listOf(
                Color(song.coverGradientStart),
                Color(song.coverGradientEnd).copy(alpha = 0.8f),
                Color(0xFF101010)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_lyrics_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_lyrics_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Lyrics",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = {
                        val activeText = parsedLyrics.getOrNull(activeIdx)?.text ?: song.title
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🎵 \"$activeText\"\n\n— ${song.title} by ${song.artist} on Beatify"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Lyric Quote"))
                    },
                    modifier = Modifier.testTag("share_lyrics_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Lyric Line",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle info
            Text(
                text = "Tap any line to jump to that moment",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Synchronized Lyric Lines List
            if (parsedLyrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.get("no_lyrics", lang),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    itemsIndexed(parsedLyrics) { index, line ->
                        val isActive = index == activeIdx
                        val isPassed = index < activeIdx

                        val textColor by animateColorAsState(
                            targetValue = when {
                                isActive -> Color.White
                                isPassed -> Color.White.copy(alpha = 0.85f)
                                else -> Color.White.copy(alpha = 0.35f)
                            },
                            label = "lyric_color"
                        )

                        val textSize = if (isActive) 26.sp else 20.sp
                        val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.seekTo(line.timestampMs)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("lyric_line_$index")
                        ) {
                            Text(
                                text = line.text,
                                fontSize = textSize,
                                fontWeight = fontWeight,
                                color = textColor,
                                lineHeight = 34.sp
                            )
                        }
                    }
                }
            }

            // Quick Play/Pause bar at bottom of lyrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = AppStrings.get("now_playing", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Play",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
