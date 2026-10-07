package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        ListeningStat::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun listeningStatDao(): ListeningStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beatify_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                populateInitialData(getDatabase(context, scope))
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            scope.launch(Dispatchers.IO) {
                                getDatabase(context, scope).songDao().insertSongs(DefaultSongs.songs)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val songDao = database.songDao()
            val playlistDao = database.playlistDao()
            val statDao = database.listeningStatDao()

            songDao.insertSongs(DefaultSongs.songs)

            for (pl in DefaultSongs.defaultPlaylists) {
                playlistDao.insertPlaylist(pl)
            }

            // Populate some initial cross references
            val allSongs = DefaultSongs.songs
            if (allSongs.isNotEmpty()) {
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_discover_weekly", allSongs[0].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_discover_weekly", allSongs[2].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_discover_weekly", allSongs[3].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_discover_weekly", allSongs[4].id))

                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_daily_mix_hindi", allSongs[0].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_daily_mix_hindi", allSongs[1].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_daily_mix_hindi", allSongs[6].id))

                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_punjabi_heat", allSongs[2].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_punjabi_heat", allSongs[5].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_punjabi_heat", allSongs[11].id))

                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_midnight_vibes", allSongs[8].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_midnight_vibes", allSongs[1].id))
                playlistDao.insertSongToPlaylist(PlaylistSongCrossRef("pl_midnight_vibes", allSongs[7].id))
            }

            // Seed initial listening stats for rich analytics dashboard
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            statDao.insertStat(ListeningStat(songId = "s_kesariya", songTitle = "Kesariya (Midnight Mix)", artist = "Arijit Singh", genre = "Bollywood", durationSeconds = 268, timestamp = now - 2 * dayMs))
            statDao.insertStat(ListeningStat(songId = "s_brown_munde", songTitle = "Brown Munde", artist = "AP Dhillon", genre = "Punjabi", durationSeconds = 247, timestamp = now - 1 * dayMs))
            statDao.insertStat(ListeningStat(songId = "s_blinding_lights", songTitle = "Blinding Lights", artist = "The Weeknd", genre = "Pop", durationSeconds = 200, timestamp = now - 3 * dayMs))
            statDao.insertStat(ListeningStat(songId = "s_apna_bana_le", songTitle = "Apna Bana Le", artist = "Arijit Singh", genre = "Bollywood", durationSeconds = 261, timestamp = now - 4 * dayMs))
            statDao.insertStat(ListeningStat(songId = "s_lofi_midnight", songTitle = "Chai & Midnight Rain", artist = "Lo-Fi Beats Collective", genre = "Lo-Fi", durationSeconds = 195, timestamp = now))
        }
    }
}
