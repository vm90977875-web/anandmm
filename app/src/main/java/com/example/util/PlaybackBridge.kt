package com.example.util

object PlaybackBridge {
    var onPlayPause: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
    var onPrev: (() -> Unit)? = null
    var onSkip10: (() -> Unit)? = null
    var onStop: (() -> Unit)? = null
}
