package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.ArtistCelebrationOverlay
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullScreenPlayer
import com.example.ui.components.MiniPlayer
import com.example.ui.components.ShareSongDialog
import com.example.ui.screens.*
import com.example.ui.theme.BeatifyTheme
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.AppStrings

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
            val isPureBlack by viewModel.isPureBlack.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val currentSong by viewModel.currentSong.collectAsState()
            val isFullScreenOpen by viewModel.isFullScreenPlayerOpen.collectAsState()
            val isShareOpen by viewModel.isShareDialogOpen.collectAsState()
            val isAddToPlaylistOpen by viewModel.isAddToPlaylistDialogOpen.collectAsState()
            val isCreatePlaylistOpen by viewModel.isCreatePlaylistDialogOpen.collectAsState()
            val actionSong by viewModel.actionSong.collectAsState()
            val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
            val lang by viewModel.currentLanguage.collectAsState()
            val isCelebrationVisible by viewModel.isCelebrationVisible.collectAsState()
            val celebrationArtist by viewModel.celebrationArtist.collectAsState()
            val isHeartCelebrationVisible by viewModel.isHeartCelebrationVisible.collectAsState()
            val celebrationLikedSong by viewModel.celebrationLikedSong.collectAsState()

            BeatifyTheme(pureBlack = isPureBlack) {
                val context = LocalContext.current
                var lastBackPressTime by remember { mutableLongStateOf(0L) }

                // System Back Button & Gesture Handler
                BackHandler {
                    if (viewModel.canNavigateBack()) {
                        viewModel.handleBackPress()
                    } else {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastBackPressTime < 2000L) {
                            (context as? ComponentActivity)?.finish()
                        } else {
                            lastBackPressTime = currentTime
                            val msg = if (lang == com.example.util.AppLanguage.HINDI) "ऐप बंद करने के लिए दोबारा बैक दबाएं" else "Press back again to exit"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                if (!isOnboardingCompleted) {
                    OnboardingAuthScreen(viewModel = viewModel)
                } else {
                    Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
                            // Mini Player directly docked above Bottom Navigation
                            if (currentSong != null && !isFullScreenOpen) {
                                MiniPlayer(viewModel = viewModel)
                            }

                            // Standard M3 Bottom Navigation
                            NavigationBar(
                                containerColor = if (isPureBlack) Color.Black else Color(0xFF101010),
                                contentColor = TextPrimary,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("main_bottom_nav")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.HOME,
                                    onClick = { viewModel.navigateTo(Screen.HOME) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen == Screen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = AppStrings.get("nav_home", lang)
                                        )
                                    },
                                    label = { Text(AppStrings.get("nav_home", lang)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SpotifyGreen,
                                        selectedTextColor = SpotifyGreen,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_item_home")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == Screen.SEARCH,
                                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen == Screen.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                            contentDescription = AppStrings.get("nav_search", lang)
                                        )
                                    },
                                    label = { Text(AppStrings.get("nav_search", lang)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SpotifyGreen,
                                        selectedTextColor = SpotifyGreen,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_item_search")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == Screen.LIBRARY || currentScreen == Screen.PLAYLIST_DETAIL,
                                    onClick = { viewModel.navigateTo(Screen.LIBRARY) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen == Screen.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                            contentDescription = AppStrings.get("nav_library", lang)
                                        )
                                    },
                                    label = { Text(AppStrings.get("nav_library", lang)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SpotifyGreen,
                                        selectedTextColor = SpotifyGreen,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_item_library")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == Screen.EQUALIZER,
                                    onClick = { viewModel.navigateTo(Screen.EQUALIZER) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen == Screen.EQUALIZER) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                                            contentDescription = AppStrings.get("nav_equalizer", lang)
                                        )
                                    },
                                    label = { Text(AppStrings.get("nav_equalizer", lang)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SpotifyGreen,
                                        selectedTextColor = SpotifyGreen,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_item_equalizer")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == Screen.SETTINGS,
                                    onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = AppStrings.get("nav_settings", lang)
                                        )
                                    },
                                    label = { Text(AppStrings.get("nav_settings", lang)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SpotifyGreen,
                                        selectedTextColor = SpotifyGreen,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(viewModel = viewModel)
                            Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                            Screen.LIBRARY -> LibraryScreen(viewModel = viewModel)
                            Screen.EQUALIZER -> EqualizerBoosterScreen(viewModel = viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            Screen.PLAYLIST_DETAIL -> {
                                if (selectedPlaylist != null) {
                                    PlaylistDetailScreen(
                                        playlist = selectedPlaylist!!,
                                        viewModel = viewModel
                                    )
                                } else {
                                    LibraryScreen(viewModel = viewModel)
                                }
                            }
                            Screen.ARTIST_PROFILE -> ArtistProfileScreen(viewModel = viewModel)
                        }
                    }
                }

                // Full Screen Player Modal Slide-up
                AnimatedVisibility(
                    visible = isFullScreenOpen,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    FullScreenPlayer(viewModel = viewModel)
                }

                // Share Song Dialog
                if (isShareOpen && actionSong != null) {
                    ShareSongDialog(
                        song = actionSong!!,
                        language = lang,
                        onDismiss = { viewModel.isShareDialogOpen.value = false }
                    )
                }

                // Add to Playlist Dialog
                if (isAddToPlaylistOpen && actionSong != null) {
                    AddToPlaylistDialog(
                        viewModel = viewModel,
                        song = actionSong!!,
                        onDismiss = { viewModel.isAddToPlaylistDialogOpen.value = false }
                    )
                }

                // Create Playlist Dialog
                if (isCreatePlaylistOpen) {
                    CreatePlaylistDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.isCreatePlaylistDialogOpen.value = false }
                    )
                }

                // Big Artist Follow Celebration Emoji Animation Overlay
                ArtistCelebrationOverlay(
                    artistName = celebrationArtist,
                    isVisible = isCelebrationVisible,
                    onDismiss = { viewModel.dismissCelebration() }
                )

                // Heart Emoji Celebration when user likes any song
                com.example.ui.components.HeartCelebrationOverlay(
                    songTitle = celebrationLikedSong,
                    isVisible = isHeartCelebrationVisible,
                    onDismiss = { viewModel.dismissHeartCelebration() }
                )
            }
        }
        }
    }
}
