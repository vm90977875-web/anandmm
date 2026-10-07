package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Song
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class WidgetStyleType(val title: String, val desc: String, val sizeTag: String) {
    COMPACT_CIRCULAR("1. Mini Pod Circular", "Minimalist 1x1 circular desk badge", "1x1 Mini"),
    STANDARD_BAR("2. Slim Horizon Bar", "Ultra-thin horizontal playback bar", "4x1 Slim"),
    HERO_EXPANDED("3. Studio Cassette Deck", "Audiophile cassette & vinyl display", "4x2 Medium"),
    LYRIC_RADAR("4. Live Lyric Radar", "Shows live synced lyric snippet & artist banner", "3x3 Dynamic"),
    DISCOVERY_DISC("5. Cyber Neon Turntable", "Futuristic spinning disc with neon spectrum", "4x4 Deluxe")
}

@Composable
fun HomeWidgetShowcaseSection(
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStyle by remember { mutableStateOf(WidgetStyleType.STANDARD_BAR) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📱 Home Screen Widgets (होम स्क्रीन विजेट्स)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpotifyGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "5 Unique Styles",
                        color = SpotifyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Add these modern, attractive widgets to your Android phone home screen in any size (Mini to Deluxe)",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Widget style selection tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(WidgetStyleType.values()) { style ->
                val isSelected = style == selectedStyle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SpotifyGreen else DarkCard,
                    border = BorderStroke(1.dp, if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.clickable { selectedStyle = style }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = style.title,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = style.sizeTag,
                            color = if (isSelected) Color.Black.copy(alpha = 0.7f) else SpotifyGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live interactive preview of the selected widget
        val songTitle = currentSong?.title ?: "Kesariya (From Brahmastra)"
        val songArtist = currentSong?.artist ?: "Arijit Singh, Pritam"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedStyle) {
                WidgetStyleType.COMPACT_CIRCULAR -> {
                    // Style 1: 1x1 Mini Circular Pod
                    Surface(
                        shape = CircleShape,
                        color = DarkCard,
                        border = BorderStroke(2.dp, SpotifyGreen),
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = songTitle,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = songArtist,
                                color = SpotifyGreen,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledIconButton(
                                onClick = onPlayPause,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SpotifyGreen),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                WidgetStyleType.STANDARD_BAR -> {
                    // Style 2: 4x1 Slim Horizon Bar
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkCard,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SpotifyGreen, Color(0xFF00B377))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = songTitle,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$songArtist • 320 kbps Studio",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = onPrev) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = TextSecondary)
                            }
                            FilledIconButton(
                                onClick = onPlayPause,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = SpotifyGreen),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = onNext) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = TextSecondary)
                            }
                        }
                    }
                }
                WidgetStyleType.HERO_EXPANDED -> {
                    // Style 3: Studio Cassette & Vinyl Deck (4x2 Medium)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF161920),
                        border = BorderStroke(1.5.dp, SpotifyGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isPlaying) SpotifyGreen else Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPlaying) "PLAYING HIGH-RES STEREO" else "STANDBY",
                                        color = if (isPlaying) SpotifyGreen else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "BEATIFY PRO DECK",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = songTitle,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = songArtist,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = onPrev) {
                                    Icon(Icons.Default.FastRewind, contentDescription = null, tint = TextPrimary)
                                }
                                Button(
                                    onClick = onPlayPause,
                                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPlaying) "PAUSE" else "PLAY",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                IconButton(onClick = onNext) {
                                    Icon(Icons.Default.FastForward, contentDescription = null, tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
                WidgetStyleType.LYRIC_RADAR -> {
                    // Style 4: Live Lyric Radar (3x3 Dynamic)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = DarkCard,
                        border = BorderStroke(1.dp, Color(0xFF7000FF).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lyrics, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Synced Lyric Snippet", color = Color(0xFF00D2FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "“ केसरिया तेरा इश्क है पिया, रंग जाऊं जो मैं हाथ लगाऊं... ”",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Artist: $songArtist", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                WidgetStyleType.DISCOVERY_DISC -> {
                    // Style 5: Cyber Neon Turntable (4x4 Deluxe)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0A0C10),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(SpotifyGreen, Color(0xFF00D2FF)))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("NEON TURNTABLE DELUXE", color = Color(0xFF00D2FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("VIRTUAL VINYL 33 RPM", color = SpotifyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(SpotifyGreen, Color(0xFF7000FF), Color(0xFF00D2FF), SpotifyGreen)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(songTitle, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(songArtist, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
