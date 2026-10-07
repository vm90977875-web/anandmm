package com.example.player

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LyricsParser {
    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\](.*)")

        lrcContent.lineSequence().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msPart = match.groupValues[3]
                val ms = when (msPart.length) {
                    2 -> (msPart.toLongOrNull() ?: 0L) * 10
                    3 -> msPart.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    fun findActiveIndex(lyrics: List<LyricLine>, currentPositionMs: Long): Int {
        if (lyrics.isEmpty()) return -1
        var active = -1
        for (i in lyrics.indices) {
            if (currentPositionMs >= lyrics[i].timestampMs) {
                active = i
            } else {
                break
            }
        }
        return active
    }
}
