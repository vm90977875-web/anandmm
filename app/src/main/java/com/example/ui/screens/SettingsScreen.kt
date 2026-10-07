package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val streamingQuality by viewModel.streamingQuality.collectAsState()
    val downloadQuality by viewModel.downloadQuality.collectAsState()
    val equalizerPreset by viewModel.equalizerPreset.collectAsState()
    val equalizerBands by viewModel.equalizerBands.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val isPureBlack by viewModel.isPureBlack.collectAsState()
    val crossfade by viewModel.crossfadeSeconds.collectAsState()
    val volumeNorm by viewModel.volumeNormalization.collectAsState()
    val dataSaver by viewModel.dataSaver.collectAsState()
    val context = LocalContext.current

    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewName by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var reviewEmail by remember(userProfile.email) { mutableStateOf(userProfile.email) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewSubmitted by remember { mutableStateOf(false) }

    val streamingOptions = listOf("Data Saver (96 kbps)", "Normal (160 kbps)", "High (320 kbps)", "Hi-Fi Lossless (FLAC)")
    val downloadOptions = listOf("Normal (160 kbps)", "High (320 kbps)", "Extreme Lossless")
    val eqPresets = listOf("Flat", "Bass Boost", "Vocal Booster", "Electronic", "Rock", "Acoustic", "Jazz")

    BackHandler {
        viewModel.handleBackPress()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = AppStrings.get("nav_settings", lang),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Google Account Integration Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("google_account", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (userProfile.isSignedIn) SpotifyGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (userProfile.isSignedIn) "Premium Active" else "Free Plan",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (userProfile.isSignedIn) SpotifyGreen else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar with Image
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (userProfile.isSignedIn) SpotifyGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                                .border(2.dp, if (userProfile.isSignedIn) SpotifyGreen else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userProfile.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "@${userProfile.handle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpotifyGreen
                                )
                            }
                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (userProfile.isSignedIn) {
                                Text(
                                    text = "Google Account Linked • Profile Configured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpotifyGreen
                                )
                            }
                        }
                    }

                    if (userProfile.favoriteGenres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(userProfile.favoriteGenres) { genre ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SpotifyGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpotifyGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.toggleGoogleSignIn()
                                Toast.makeText(
                                    context,
                                    if (userProfile.isSignedIn) "Logged out of Google" else "Signed in with Google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userProfile.isSignedIn) Color.White.copy(alpha = 0.15f) else SpotifyGreen
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("google_auth_btn")
                        ) {
                            Text(
                                text = if (userProfile.isSignedIn) AppStrings.get("sign_out", lang) else AppStrings.get("sign_in_google", lang),
                                color = if (userProfile.isSignedIn) TextPrimary else Color.Black,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.resetOnboardingForDemo()
                                Toast.makeText(context, "First-launch onboarding reset! Showing Google Login & Profile creation.", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).testTag("reset_onboarding_demo_btn")
                        ) {
                            Text(
                                text = "Reset First-Run",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Multi-Language Support Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("language", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { appLang ->
                            val isSelected = appLang == lang
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.currentLanguage.value = appLang
                                    }
                                    .padding(vertical = 4.dp),
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = appLang.nativeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                    Text(
                                        text = appLang.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Audio Quality Settings Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("audio_quality", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Streaming Quality Selector
                    Text(
                        text = AppStrings.get("streaming_quality", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    streamingOptions.forEach { opt ->
                        val isSelected = opt == streamingQuality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.streamingQuality.value = opt }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.streamingQuality.value = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = SpotifyGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // Download Quality Selector
                    Text(
                        text = AppStrings.get("download_quality", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    downloadOptions.forEach { opt ->
                        val isSelected = opt == downloadQuality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.downloadQuality.value = opt }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.downloadQuality.value = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = SpotifyGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Equalizer & Studio Tone Section with Rotary Knobs
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "🎛️ " + AppStrings.get("equalizer", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Touch & rotate knobs to tune Bass & Frequency bands",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Presets Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eqPresets) { preset ->
                            val isSelected = preset == equalizerPreset
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setEqualizerPreset(preset) },
                                label = { Text(preset) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpotifyGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5-Band Rotary Tactile Knobs (60 Hz Bass, 230 Hz Low-Mid, 910 Hz Mid, 3.6 kHz Presence, 14 kHz Air)
                    val bandLabels = listOf("60Hz Bass", "230Hz Low", "910Hz Mid", "3.6k Tone", "14k Air")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        equalizerBands.forEachIndexed { index, gain ->
                            com.example.ui.components.RotaryEqualizerKnob(
                                value = gain,
                                onValueChange = { viewModel.updateEqualizerBand(index, it) },
                                label = bandLabels.getOrElse(index) { "" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Playback & Night Listening Settings
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = DarkCard
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Playback & Night Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pure OLED Black toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("pure_black", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pitch-black canvas for OLED screens & late night sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = isPureBlack,
                            onCheckedChange = { viewModel.isPureBlack.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SpotifyGreen
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Normalize Volume toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("volume_normal", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Set the same volume level for all songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = volumeNorm,
                            onCheckedChange = { viewModel.volumeNormalization.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SpotifyGreen
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Data Saver toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("data_saver", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sets music quality to low and disables previews",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = dataSaver,
                            onCheckedChange = { viewModel.dataSaver.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SpotifyGreen
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                    // Crossfade
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = AppStrings.get("crossfade", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${crossfade}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = crossfade.toFloat(),
                            onValueChange = { viewModel.crossfadeSeconds.value = it.toInt() },
                            valueRange = 0f..12f,
                            colors = SliderDefaults.colors(
                                thumbColor = SpotifyGreen,
                                activeTrackColor = SpotifyGreen
                            )
                        )
                    }
                }
            }
        }

        // ========================================================
        // SECTION: APP REVIEW & RATING (सेटिंग में लास्ट में स्क्रॉल करने पर रिव्यू रहे)
        // ========================================================
        item {
            Text(
                text = "⭐ Rate & Review App (समीक्षा और रेटिंग दें)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Share your rating and feedback. Approved reviews appear on the Home Screen reviews wall!",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("app_review_card"),
                color = DarkCard,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Star Rating Picker (1 to 5 Stars)
                    Text(
                        text = "Select Star Rating (स्टार्स चुनें):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starNum = index + 1
                            IconButton(
                                onClick = { reviewRating = starNum },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("star_btn_$starNum")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "$starNum Stars",
                                    tint = if (starNum <= reviewRating) Color(0xFFFFD700) else Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = when (reviewRating) {
                                5 -> "5.0 ★ Excellent (सर्वश्रेष्ठ)"
                                4 -> "4.0 ★ Very Good (बहुत अच्छा)"
                                3 -> "3.0 ★ Good (अच्छा)"
                                2 -> "2.0 ★ Fair (सामान्य)"
                                else -> "1.0 ★ Needs Work"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Name Field
                    OutlinedTextField(
                        value = reviewName,
                        onValueChange = { reviewName = it },
                        label = { Text("Your Name (नाम)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // User Email Field
                    OutlinedTextField(
                        value = reviewEmail,
                        onValueChange = { reviewEmail = it },
                        label = { Text("Email (ईमेल)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Review Message Field
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Your Review / Feedback (अपनी समीक्षा लिखें)") },
                        placeholder = { Text("Music streaming quality, artist profiles, offline songs...", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("review_comment_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Success Feedback Alert
                    if (reviewSubmitted) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .padding(bottom = 12.dp),
                            color = SpotifyGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "धन्यवाद! आपकी समीक्षा सबमिट हो गई है और अब यह होम पेज के रिव्यूज में दिख रही है।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (reviewComment.isBlank()) {
                                Toast.makeText(context, "कृपया अपनी समीक्षा लिखें (Please write a review comment)", Toast.LENGTH_SHORT).show()
                            } else {
                                val success = viewModel.submitReview(
                                    name = reviewName,
                                    email = reviewEmail,
                                    rating = reviewRating,
                                    comment = reviewComment
                                )
                                if (success) {
                                    reviewSubmitted = true
                                    reviewComment = ""
                                    Toast.makeText(context, "रिव्यू सफलतापूर्वक भेजा गया! (Review Submitted)", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_review_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Review (रिव्यू भेजें)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
