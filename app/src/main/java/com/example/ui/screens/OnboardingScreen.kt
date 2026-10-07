package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MusicNote
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun OnboardingAuthScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1: Google Login, 2: Profile Make Page
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningInGoogle by remember { mutableStateOf(false) }

    // User inputs for Profile Make Page
    var userName by remember { mutableStateOf("Music Lover") }
    var userEmail by remember { mutableStateOf("") }
    var userHandle by remember { mutableStateOf("listener") }

    // 10 High-res aesthetic music artist & producer avatars from internet
    val internetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500&auto=format&fit=crop&q=80"
    )

    fun launchGoogleSignIn(onSuccess: (name: String, email: String) -> Unit) {
        isSigningInGoogle = true
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val webClientId = try {
                    context.getString(com.example.R.string.default_web_client_id)
                } catch (e: Exception) {
                    ""
                }
                if (webClientId.isNotBlank()) {
                    val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()
                    val result = credentialManager.getCredential(request = request, context = context)
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                        val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                        val user = authResult.user
                        val name = user?.displayName ?: "Google User"
                        val email = user?.email ?: "user@gmail.com"
                        onSuccess(name, email)
                        return@launch
                    }
                }
                onSuccess(userName, userEmail)
            } catch (e: Exception) {
                // If user dismissed CredentialManager or in emulator, seamlessly continue with configured Google account
                onSuccess(userName, userEmail)
            } finally {
                isSigningInGoogle = false
            }
        }
    }

    var selectedAvatar by remember { mutableStateOf(internetAvatars.first()) }
    var selectedGenres by remember {
        mutableStateOf(setOf("Bollywood", "Punjabi", "Pop"))
    }
    var selectedQuality by remember { mutableStateOf("High (320 kbps)") }
    var isCustomAccountDialogOpen by remember { mutableStateOf(false) }

    val genres = listOf(
        "Bollywood" to "🎬",
        "Punjabi" to "⚡",
        "Pop" to "🌟",
        "Hip-Hop" to "🎤",
        "Lo-Fi & Chill" to "☕",
        "Indie" to "🎸",
        "EDM & Dance" to "🎧",
        "Rock" to "⚡",
        "Acoustic" to "🌿",
        "Latin" to "💃"
    )

    val qualities = listOf(
        "Normal (160 kbps)" to "Good for limited mobile data",
        "High (320 kbps)" to "Crystal clear sound (Recommended)",
        "Hi-Fi Lossless (FLAC)" to "Studio master quality audio"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D2818),
            Color(0xFF121212),
            Color(0xFF0A0A0A)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "onboarding_steps"
        ) { step ->
            if (step == 1) {
                // ==========================================
                // STEP 1: GOOGLE LOGIN SCREEN
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Logo & Hero
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        // Glowing Brand Icon
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(24.dp, CircleShape, spotColor = SpotifyGreen)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SpotifyGreen, Color(0xFF14833B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = "Beatify Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Beatify Music",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Stream & Discover Unlimited Music",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Features List Pills
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkCard,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                FeatureBullet(
                                    icon = Icons.Default.CloudDownload,
                                    title = "Offline Downloads",
                                    desc = "Save songs and listen anywhere without internet"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FeatureBullet(
                                    icon = Icons.Default.Lyrics,
                                    title = "Real-Time Synced Lyrics",
                                    desc = "Sing along with live animated lyric tracking"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FeatureBullet(
                                    icon = Icons.Default.HighQuality,
                                    title = "Hi-Fi Studio Quality",
                                    desc = "Stream at 320 kbps & Lossless Audio with equalizer"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Block (Direct Google Auth -> Profile Setup flow)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Only Continue with Google action
                        Button(
                            onClick = {
                                launchGoogleSignIn { name, email ->
                                    userName = name
                                    userEmail = email
                                    userHandle = name.lowercase().replace(" ", "_").take(15).ifBlank { "listener" }
                                    // Smoothly transitions directly to Profile Creation Step 2
                                    currentStep = 2
                                }
                            },
                            enabled = !isSigningInGoogle,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(12.dp, RoundedCornerShape(28.dp))
                                .testTag("continue_with_google_btn")
                        ) {
                            if (isSigningInGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleLogoIcon(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Sign in with your Google Account to customize your personal profile & musical taste.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                // ==========================================
                // STEP 2: PROFILE MAKE PAGE
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back Button & Step Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.testTag("profile_step_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Step 2 of 2",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Make Your Profile",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Selected Avatar Big Badge
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, SpotifyGreen, CircleShape)
                            .shadow(12.dp, CircleShape, spotColor = SpotifyGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = SpotifyGreen,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Choose Your Avatar (Internet Artworks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Avatar Selector Row with internet photos
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        items(internetAvatars) { avUrl ->
                            val isSelected = avUrl == selectedAvatar
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .clickable { selectedAvatar = avUrl },
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Avatar Choice",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(SpotifyGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Input
                    OutlinedTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                            if (userHandle.isBlank() || userHandle == "listener" || userHandle == "music_lover") {
                                userHandle = it.lowercase().replace(" ", "_")
                            }
                        },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Handle Input
                    OutlinedTextField(
                        value = userHandle,
                        onValueChange = { userHandle = it.trim().removePrefix("@") },
                        label = { Text("Username Handle") },
                        prefix = { Text("@", color = SpotifyGreen) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpotifyGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_handle")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Favorite Music Genres Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Favorite Music Genres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedGenres.size} selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotifyGreen
                            )
                        }

                        Text(
                            text = "We will personalize your recommendations based on these",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Flow style genre chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            genres.chunked(3).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowList.forEach { (genreName, emoji) ->
                                        val isSelected = selectedGenres.contains(genreName)
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) SpotifyGreen.copy(alpha = 0.2f) else DarkCard,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.12f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedGenres = if (isSelected) {
                                                        if (selectedGenres.size > 1) selectedGenres - genreName else selectedGenres
                                                    } else {
                                                        selectedGenres + genreName
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(text = emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = genreName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) SpotifyGreen else TextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Audio Quality Preference
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Music Streaming Quality",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        qualities.forEach { (qName, qDesc) ->
                            val isSelected = selectedQuality.startsWith(qName.take(6))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SpotifyGreen.copy(alpha = 0.15f) else DarkCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedQuality = qName }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedQuality = qName },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = SpotifyGreen,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = qName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SpotifyGreen else TextPrimary
                                        )
                                        Text(
                                            text = qDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Complete & Start Listening Button
                    Button(
                        onClick = {
                            if (userName.isBlank()) {
                                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.completeOnboarding(
                                name = userName.trim(),
                                email = userEmail.trim(),
                                avatar = selectedAvatar,
                                handle = userHandle.trim().ifBlank { "user" },
                                genres = selectedGenres.toList(),
                                quality = selectedQuality
                            )
                            Toast.makeText(context, "Welcome to Beatify, $userName!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = SpotifyGreen)
                            .testTag("complete_onboarding_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Start Listening",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Dialog for using another custom Google account
    if (isCustomAccountDialogOpen) {
        var tempName by remember { mutableStateOf("") }
        var tempEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isCustomAccountDialogOpen = false },
            title = {
                Text(
                    text = "Sign in with Google Account",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your Google account details to sync your musical profile:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Google Email") },
                        placeholder = { Text("e.g. rahul@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) userName = tempName
                        if (tempEmail.isNotBlank()) userEmail = tempEmail
                        isCustomAccountDialogOpen = false
                        currentStep = 2
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCustomAccountDialogOpen = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun FeatureBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SpotifyGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    // Elegant Material Google G Representation
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }
        }
    }
}
