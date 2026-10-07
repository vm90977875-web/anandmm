package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Beatify", appName)
  }

  @Test
  fun `verify lyrics parser parses timestamps correctly`() {
    val sampleLrc = "[00:05.50] Hello darkness my old friend\n[00:15.00] I've come to talk with you again"
    val lines = com.example.player.LyricsParser.parse(sampleLrc)
    assertEquals(2, lines.size)
    assertEquals(5500L, lines[0].timestampMs)
    assertEquals("Hello darkness my old friend", lines[0].text)
    assertEquals(15000L, lines[1].timestampMs)
  }

  @Test
  fun `verify internet songs have valid image urls and metadata`() {
    val songs = com.example.data.DefaultSongs.songs
    org.junit.Assert.assertTrue(songs.size >= 20)
    for (s in songs) {
      org.junit.Assert.assertTrue(s.imageUrl.startsWith("http"))
      org.junit.Assert.assertTrue(s.title.isNotBlank())
      org.junit.Assert.assertTrue(s.artist.isNotBlank())
    }
  }

  @Test
  fun `verify onboarding status persistence in shared preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("beatify_prefs", Context.MODE_PRIVATE)
    
    // Initially not completed
    prefs.edit().clear().commit()
    assertEquals(false, prefs.getBoolean("KEY_ONBOARDING_COMPLETED", false))
    
    // Once completed, saved to true
    prefs.edit().putBoolean("KEY_ONBOARDING_COMPLETED", true).commit()
    assertEquals(true, prefs.getBoolean("KEY_ONBOARDING_COMPLETED", false))
  }
}
