package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context) {
    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
    }

    suspend fun syncUserProfile(
        userId: String,
        name: String,
        email: String,
        avatarUrl: String,
        handle: String,
        genres: List<String>,
        quality: String
    ): Boolean {
        return try {
            val userMap = hashMapOf(
                "id" to userId,
                "name" to name,
                "email" to email,
                "avatarUrl" to avatarUrl,
                "handle" to handle,
                "favoriteGenres" to genres,
                "audioQuality" to quality,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(userId)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d("FirestoreService", "User profile synced to Firestore: $userId")
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing profile to Firestore: ${e.message}")
            false
        }
    }

    suspend fun syncUserPlaylist(
        userId: String,
        playlist: Playlist
    ): Boolean {
        return try {
            val plMap = hashMapOf(
                "id" to playlist.id,
                "userId" to userId,
                "name" to playlist.name,
                "description" to playlist.description,
                "coverColorHex" to playlist.coverColorHex,
                "createdAt" to playlist.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(userId)
                .collection("playlists")
                .document(playlist.id)
                .set(plMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing playlist: ${e.message}")
            false
        }
    }

    suspend fun syncFavoriteSong(
        userId: String,
        songId: String,
        isFavorite: Boolean
    ): Boolean {
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(songId)
            if (isFavorite) {
                docRef.set(
                    hashMapOf(
                        "id" to songId,
                        "userId" to userId,
                        "songId" to songId,
                        "addedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            } else {
                docRef.delete().await()
            }
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing favorite: ${e.message}")
            false
        }
    }

    suspend fun syncListeningHistory(
        userId: String,
        song: Song
    ): Boolean {
        return try {
            val historyId = "hist_${System.currentTimeMillis()}_${song.id}"
            val historyMap = hashMapOf(
                "id" to historyId,
                "userId" to userId,
                "songId" to song.id,
                "songTitle" to song.title,
                "artist" to song.artist,
                "playedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(userId)
                .collection("history")
                .document(historyId)
                .set(historyMap)
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing history: ${e.message}")
            false
        }
    }

    suspend fun syncGlobalSongs(songs: List<Song>): Boolean {
        return try {
            val batch = firestore.batch()
            for (song in songs.take(20)) {
                val docRef = firestore.collection("songs").document(song.id)
                val songMap = hashMapOf(
                    "id" to song.id,
                    "title" to song.title,
                    "artist" to song.artist,
                    "album" to song.album,
                    "durationMs" to song.durationMs,
                    "imageUrl" to song.imageUrl,
                    "genre" to song.genre
                )
                batch.set(docRef, songMap, SetOptions.merge())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing songs: ${e.message}")
            false
        }
    }

    suspend fun syncUserReview(userId: String, review: AppReview): Boolean {
        return try {
            val revMap = hashMapOf(
                "id" to review.id,
                "userId" to userId,
                "userName" to review.userName,
                "userEmail" to review.userEmail,
                "rating" to review.rating,
                "comment" to review.comment,
                "dateLabel" to review.dateLabel,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users")
                .document(userId)
                .collection("reviews")
                .document(review.id)
                .set(revMap)
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing review: ${e.message}")
            false
        }
    }
}
