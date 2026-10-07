package com.example.data

import java.util.UUID

data class AppReview(
    val id: String = UUID.randomUUID().toString(),
    val userName: String,
    val userEmail: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val dateLabel: String = "Today",
    val verifiedUser: Boolean = true,
    val avatarGradientStart: Long = 0xFF1DB954,
    val avatarGradientEnd: Long = 0xFF191414,
    val timestamp: Long = System.currentTimeMillis()
)

object DefaultReviews {
    val initialReviews = listOf(
        AppReview(
            id = "rev_1",
            userName = "Rahul Verma",
            userEmail = "rahul.v@gmail.com",
            rating = 5,
            comment = "Best sound quality! Arijit Singh & Diljit songs stream super fast without any ads. Loved the clean dark UI.",
            dateLabel = "Today",
            avatarGradientStart = 0xFFFF5E3A,
            avatarGradientEnd = 0xFFFF2A68
        ),
        AppReview(
            id = "rev_2",
            userName = "Priya Sharma",
            userEmail = "priya.sharma99@gmail.com",
            rating = 5,
            comment = "Offline download feature is a lifesaver during train journeys! All my downloaded tracks play smoothly without internet.",
            dateLabel = "Today",
            avatarGradientStart = 0xFF11998E,
            avatarGradientEnd = 0xFF38EF7D
        ),
        AppReview(
            id = "rev_3",
            userName = "Simranjit Kaur",
            userEmail = "simran.k@yahoo.com",
            rating = 5,
            comment = "All Punjabi hits from Sidhu Moose Wala, Karan Aujla, AP Dhillon! And the big heart emoji animation on following artists is amazing!",
            dateLabel = "Today",
            avatarGradientStart = 0xFF8E0E00,
            avatarGradientEnd = 0xFF1F1C18
        ),
        AppReview(
            id = "rev_4",
            userName = "Aman Deep Singh",
            userEmail = "aman.beats@gmail.com",
            rating = 5,
            comment = "Top notch design, looks and feels even smoother than Spotify. Back navigation works perfectly now.",
            dateLabel = "Yesterday",
            avatarGradientStart = 0xFF654EA3,
            avatarGradientEnd = 0xFFEAAFC8
        ),
        AppReview(
            id = "rev_5",
            userName = "Neha Gupta",
            userEmail = "neha.music@outlook.com",
            rating = 5,
            comment = "No demo ringtones, only pure original studio quality songs. The lyrics sync is accurate word by word.",
            dateLabel = "Yesterday",
            avatarGradientStart = 0xFF4A00E0,
            avatarGradientEnd = 0xFF8E2DE2
        ),
        AppReview(
            id = "rev_6",
            userName = "Rohit Malhotra",
            userEmail = "rohit.m@gmail.com",
            rating = 5,
            comment = "Bass Boost equalizer is incredible with heavy headphones. Sound output is crisp 320 kbps.",
            dateLabel = "2 days ago",
            avatarGradientStart = 0xFFF7971E,
            avatarGradientEnd = 0xFFFFD200
        ),
        AppReview(
            id = "rev_7",
            userName = "Ananya Roy",
            userEmail = "ananya.roy@gmail.com",
            rating = 5,
            comment = "The artist profile page with all songs is so convenient. Tapping on any artist opens their full playlist instantly.",
            dateLabel = "2 days ago",
            avatarGradientStart = 0xFF00C9FF,
            avatarGradientEnd = 0xFF92FE9D
        ),
        AppReview(
            id = "rev_8",
            userName = "Vikram Patel",
            userEmail = "vikram.p@gmail.com",
            rating = 5,
            comment = "Offline playback works seamlessly. Downloaded 20 songs and they all play with zero buffering.",
            dateLabel = "3 days ago",
            avatarGradientStart = 0xFF56CCF2,
            avatarGradientEnd = 0xFF2F80ED
        ),
        AppReview(
            id = "rev_9",
            userName = "Jaspreet Bains",
            userEmail = "jaspreet.b@gmail.com",
            rating = 5,
            comment = "Diljit's Lover and Born to Shine sound amazing. Love being able to review inside settings and see it on home screen.",
            dateLabel = "3 days ago",
            avatarGradientStart = 0xFFED213A,
            avatarGradientEnd = 0xFF93291E
        ),
        AppReview(
            id = "rev_10",
            userName = "Kavita Reddy",
            userEmail = "kavita.r@gmail.com",
            rating = 5,
            comment = "Cleanest music player app for Android. Hindi and English songs both categorized beautifully.",
            dateLabel = "4 days ago",
            avatarGradientStart = 0xFFFF7E5F,
            avatarGradientEnd = 0xFFFEB47B
        ),
        AppReview(
            id = "rev_11",
            userName = "Aditya Joshi",
            userEmail = "aditya.j@gmail.com",
            rating = 5,
            comment = "Google sync and Firebase cloud backup worked instantly. Highly recommended for daily listening!",
            dateLabel = "5 days ago",
            avatarGradientStart = 0xFF1DB954,
            avatarGradientEnd = 0xFF191414
        ),
        AppReview(
            id = "rev_12",
            userName = "Sneha Kulkarni",
            userEmail = "sneha.k@gmail.com",
            rating = 5,
            comment = "The daily recommendations and trending chart keep getting better every morning.",
            dateLabel = "5 days ago",
            avatarGradientStart = 0xFF9B51E0,
            avatarGradientEnd = 0xFF333333
        )
    )
}
