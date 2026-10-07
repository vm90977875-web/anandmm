package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import com.example.util.AppStrings

@Composable
fun AnalyticsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.listeningStats.collectAsState()
    val totalSec by viewModel.totalListeningSeconds.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val totalMinutes = (totalSec ?: 3600L) / 60
    val totalHours = totalMinutes / 60

    // Compute top genres
    val genreCounts = remember(stats, allSongs) {
        if (stats.isNotEmpty()) {
            stats.groupingBy { it.genre }.eachCount()
        } else {
            mapOf("Bollywood" to 28, "Punjabi" to 22, "Pop" to 15, "Lo-Fi" to 10)
        }
    }
    val totalGenrePlays = genreCounts.values.sum().coerceAtLeast(1)

    // Compute top artists
    val topArtists = remember(stats, allSongs) {
        if (stats.isNotEmpty()) {
            stats.groupingBy { it.artist }.eachCount().toList().sortedByDescending { it.second }.take(5)
        } else {
            listOf("Arijit Singh" to 14, "AP Dhillon" to 11, "The Weeknd" to 9, "Lo-Fi Collective" to 6, "Luis Fonsi" to 4)
        }
    }

    BackHandler {
        viewModel.handleBackPress()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wrapped Hero Banner
        item {
            val heroBrush = Brush.linearGradient(
                colors = listOf(SpotifyGreen, Color(0xFF11998E), Color(0xFF38EF7D))
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkCard
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SpotifyGreen.copy(alpha = 0.25f), Color(0xFF1E1E1E))
                            )
                        )
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BEATIFY WRAPPED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SpotifyGreen,
                                letterSpacing = 2.sp
                            )
                            Surface(
                                shape = CircleShape,
                                color = SpotifyGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "2026 INSIGHTS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = AppStrings.get("analytics_title", lang),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Music Personality: The Late-Night Melody Explorer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val top1 = topArtists.firstOrNull()?.first ?: "Arijit Singh"
                                val shareText = """
                                    🎵 My 2026 Beatify Wrapped!
                                    ⏱️ $totalMinutes Total Minutes Listened
                                    🏆 Top Artist: $top1
                                    ✨ Persona: Late-Night Melody Explorer
                                    
                                    Track your music journey with Beatify!
                                """.trimIndent()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Wrapped"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("share_wrapped_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Your Wrapped", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats Quick Grid: Minutes Listened & Songs Played
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Minutes Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    color = DarkCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalMinutes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = AppStrings.get("total_time", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Songs Played Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    color = DarkCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(stats.size + 18).coerceAtLeast(18)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = AppStrings.get("songs_played", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Top Artists Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("top_artists", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    topArtists.forEachIndexed { index, (artist, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (index == 0) SpotifyGreen else Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) Color.Black else TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = artist,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$count plays",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Top Genres Section (Visual Bar breakdown)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("top_genres", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    genreCounts.entries.sortedByDescending { it.value }.take(4).forEach { (genre, count) ->
                        val percent = ((count.toFloat() / totalGenrePlays) * 100).toInt()
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / totalGenrePlays },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SpotifyGreen,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
