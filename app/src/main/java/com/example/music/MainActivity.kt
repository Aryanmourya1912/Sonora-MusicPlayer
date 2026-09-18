package com.example.music

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.example.music.data.LikedSongEntity
import com.example.music.data.SearchHistoryEntity
import com.example.music.data.SonoraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.max

data class FullTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val artworkUrl: String,
    val durationFormatted: String
)

private val SonoraThemeColors = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    background = Color(0xFF0C0C11),
    surface = Color(0xFF161622),
    surfaceVariant = Color(0xFF1E1E2D),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = SonoraThemeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SonoraPlayerScreen()
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}

fun sanitizeText(input: String): String {
    return input.replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&#039;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}

fun decryptMediaUrl(encryptedUrl: String): String {
    if (encryptedUrl.isBlank()) return ""
    return try {
        val key = "38346591".toByteArray(Charsets.UTF_8)
        val keySpec = SecretKeySpec(key, "DES")
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decoded = Base64.decode(encryptedUrl.trim(), Base64.DEFAULT)
        val decrypted = cipher.doFinal(decoded)
        var rawUrl = String(decrypted, Charsets.UTF_8).trim()

        if (rawUrl.startsWith("http://")) {
            rawUrl = rawUrl.replaceFirst("http://", "https://")
        }
        if (rawUrl.contains("_96.mp4")) {
            rawUrl = rawUrl.replace("_96.mp4", "_160.mp4")
        } else if (rawUrl.contains("_96.m4a")) {
            rawUrl = rawUrl.replace("_96.m4a", "_160.m4a")
        }
        rawUrl
    } catch (e: Exception) {
        ""
    }
}

suspend fun searchOfficialSongs(query: String): Pair<List<FullTrackItem>, String?> = withContext(Dispatchers.IO) {
    val resultsList = mutableListOf<FullTrackItem>()
    try {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val endpoint = "https://www.jiosaavn.com/api.php?__call=search.getResults&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=25&p=1&q=$encodedQuery"

        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        )
        connection.setRequestProperty("Accept", "application/json, text/plain, */*")
        connection.setRequestProperty("Referer", "https://www.jiosaavn.com/")
        connection.setRequestProperty("Cookie", "L=english; gdpr_acceptance=true;")

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return@withContext Pair(emptyList(), "Server response code: $responseCode")
        }

        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseText)
        val resultsArray = root.optJSONArray("results") ?: return@withContext Pair(emptyList(), "No tracks found for \"$query\"")

        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val trackId = item.optString("id", "$i")
            val rawTitle = when {
                item.has("title") && item.optString("title").isNotBlank() -> item.optString("title")
                item.has("song") && item.optString("song").isNotBlank() -> item.optString("song")
                else -> "Unknown Track"
            }
            val trackTitle = sanitizeText(rawTitle)

            val moreInfo = item.optJSONObject("more_info")

            var artistName = item.optString("subtitle", "")
            if (artistName.isBlank() && moreInfo != null) {
                artistName = moreInfo.optString("music", "")
            }
            if (artistName.isBlank()) {
                artistName = item.optString("primary_artists", "Unknown Artist")
            }
            artistName = sanitizeText(artistName)

            var artUrl = item.optString("image", "")
            if (artUrl.isBlank() && moreInfo != null) {
                artUrl = moreInfo.optString("image", "")
            }
            artUrl = artUrl.replace("150x150", "500x500").replace("50x50", "500x500")
            if (artUrl.startsWith("http://")) {
                artUrl = artUrl.replaceFirst("http://", "https://")
            }

            val encryptedUrl = when {
                moreInfo != null && moreInfo.has("encrypted_media_url") -> moreInfo.optString("encrypted_media_url")
                item.has("encrypted_media_url") -> item.optString("encrypted_media_url")
                else -> ""
            }
            val playableStream = decryptMediaUrl(encryptedUrl)

            val durationSeconds = when {
                moreInfo != null && moreInfo.has("duration") -> moreInfo.optLong("duration", 0L)
                item.has("duration") -> item.optLong("duration", 0L)
                else -> 0L
            }
            val durationLabel = formatTime(durationSeconds * 1000)

            if (trackTitle.isNotBlank() && playableStream.isNotBlank()) {
                resultsList.add(
                    FullTrackItem(
                        id = trackId,
                        title = trackTitle,
                        artist = artistName,
                        audioUrl = playableStream,
                        artworkUrl = artUrl,
                        durationFormatted = durationLabel
                    )
                )
            }
        }
        Pair(resultsList, null)
    } catch (e: Exception) {
        Pair(resultsList, e.localizedMessage ?: "Network error occurred")
    }
}

// SharedPreferences keys for persistent playback state
private const val PREFS_SONORA = "sonora_playback_state"
private const val KEY_LAST_ID = "last_id"
private const val KEY_LAST_TITLE = "last_title"
private const val KEY_LAST_ARTIST = "last_artist"
private const val KEY_LAST_AUDIO_URL = "last_audio_url"
private const val KEY_LAST_ARTWORK_URL = "last_artwork_url"
private const val KEY_LAST_DURATION_TXT = "last_duration_txt"
private const val KEY_LAST_POSITION_MS = "last_position_ms"
private const val KEY_LAST_DURATION_MS = "last_duration_ms"

@Composable
fun SonoraPlayerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prefs: SharedPreferences = remember {
        context.getSharedPreferences(PREFS_SONORA, Context.MODE_PRIVATE)
    }

    val database = remember { SonoraDatabase.getDatabase(context) }
    val dao = remember { database.sonoraDao() }

    val recentSearches by dao.getRecentSearches().collectAsState(initial = emptyList())
    val likedSongs by dao.getAllLikedSongs().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }

    // Active track details
    var activeSongId by remember { mutableStateOf("") }
    var activeTitle by remember { mutableStateOf("No Track Playing") }
    var activeArtist by remember { mutableStateOf("Search and tap any song above") }
    var activeArtworkUrl by remember { mutableStateOf("") }
    var activeAudioUrl by remember { mutableStateOf("") }
    var activeDurationFormatted by remember { mutableStateOf("00:00") }

    val isCurrentSongLiked by dao.isSongLiked(activeSongId).collectAsState(initial = false)

    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableStateOf(0f) }

    // Sleep timer state
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerRemainingSeconds by remember { mutableLongStateOf(0L) }
    var stopAfterCurrentTrack by remember { mutableStateOf(false) }
    var sleepTimerJob by remember { mutableStateOf<Job?>(null) }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerRemainingSeconds = 0L
        stopAfterCurrentTrack = false
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerRemainingSeconds = minutes * 60L
        sleepTimerJob = coroutineScope.launch {
            while (sleepTimerRemainingSeconds > 0) {
                delay(1000L)
                sleepTimerRemainingSeconds -= 1
            }
            controller?.pause()
            cancelSleepTimer()
        }
    }

    // Connect to background playback service & restore persisted playback state
    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            controller = mediaController
            isPlaying = mediaController.isPlaying

            // 1. If background service is already playing, sync state directly
            if (mediaController.mediaItemCount > 0) {
                val currentItem = mediaController.currentMediaItem
                activeSongId = currentItem?.mediaId ?: ""
                activeTitle = currentItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                activeArtist = currentItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                activeArtworkUrl = currentItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                currentPosition = max(0L, mediaController.currentPosition)
                totalDuration = if (mediaController.duration > 0) mediaController.duration else 0L
            } else {
                // 2. Otherwise, restore the last played track & timestamp from SharedPreferences
                val savedAudioUrl = prefs.getString(KEY_LAST_AUDIO_URL, "") ?: ""
                if (savedAudioUrl.isNotBlank()) {
                    val savedId = prefs.getString(KEY_LAST_ID, "") ?: ""
                    val savedTitle = prefs.getString(KEY_LAST_TITLE, "Last Played Track") ?: ""
                    val savedArtist = prefs.getString(KEY_LAST_ARTIST, "Tap play to resume") ?: ""
                    val savedArtworkUrl = prefs.getString(KEY_LAST_ARTWORK_URL, "") ?: ""
                    val savedDurationTxt = prefs.getString(KEY_LAST_DURATION_TXT, "00:00") ?: ""
                    val savedPosMs = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
                    val savedDurMs = prefs.getLong(KEY_LAST_DURATION_MS, 0L)

                    activeSongId = savedId
                    activeTitle = savedTitle
                    activeArtist = savedArtist
                    activeArtworkUrl = savedArtworkUrl
                    activeAudioUrl = savedAudioUrl
                    activeDurationFormatted = savedDurationTxt
                    currentPosition = savedPosMs
                    totalDuration = savedDurMs

                    // Preload into ExoPlayer and seek without auto-starting
                    val metadata = MediaMetadata.Builder()
                        .setTitle(savedTitle)
                        .setArtist(savedArtist)
                        .setArtworkUri(Uri.parse(savedArtworkUrl))
                        .build()

                    val mediaItem = MediaItem.Builder()
                        .setMediaId(savedId)
                        .setUri(Uri.parse(savedAudioUrl))
                        .setMediaMetadata(metadata)
                        .build()

                    mediaController.setMediaItem(mediaItem)
                    mediaController.prepare()
                    mediaController.seekTo(savedPosMs)
                    mediaController.pause()
                }
            }

            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                    // Save position when paused
                    if (!playing && mediaController.currentPosition > 0) {
                        prefs.edit()
                            .putLong(KEY_LAST_POSITION_MS, mediaController.currentPosition)
                            .apply()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val dur = mediaController.duration
                        if (dur > 0) {
                            totalDuration = dur
                            prefs.edit().putLong(KEY_LAST_DURATION_MS, dur).apply()
                        }
                    }
                    if (playbackState == Player.STATE_ENDED && stopAfterCurrentTrack) {
                        mediaController.pause()
                        cancelSleepTimer()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (stopAfterCurrentTrack && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        mediaController.pause()
                        cancelSleepTimer()
                    }
                    activeSongId = mediaItem?.mediaId ?: ""
                    activeTitle = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                    activeArtist = mediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                    activeArtworkUrl = mediaItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                    currentPosition = 0L
                }
            })
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cancelSleepTimer()
            controller?.release()
        }
    }

    // Auto-save playback position to SharedPreferences every 1 second while playing
    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            controller?.let { player ->
                val pos = max(0L, player.currentPosition)
                currentPosition = pos
                val dur = player.duration
                if (dur > 0) totalDuration = dur

                // Write to persistence
                prefs.edit()
                    .putLong(KEY_LAST_POSITION_MS, pos)
                    .putLong(KEY_LAST_DURATION_MS, totalDuration)
                    .apply()
            }
            delay(1000L)
        }
    }

    fun playTrack(
        id: String,
        title: String,
        artist: String,
        audioUrl: String,
        artworkUrl: String,
        durationFormatted: String
    ) {
        activeSongId = id
        activeTitle = title
        activeArtist = artist
        activeArtworkUrl = artworkUrl
        activeAudioUrl = audioUrl
        activeDurationFormatted = durationFormatted

        // Persist track metadata immediately
        prefs.edit()
            .putString(KEY_LAST_ID, id)
            .putString(KEY_LAST_TITLE, title)
            .putString(KEY_LAST_ARTIST, artist)
            .putString(KEY_LAST_AUDIO_URL, audioUrl)
            .putString(KEY_LAST_ARTWORK_URL, artworkUrl)
            .putString(KEY_LAST_DURATION_TXT, durationFormatted)
            .putLong(KEY_LAST_POSITION_MS, 0L)
            .apply()

        controller?.let { player ->
            val metadata = MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(Uri.parse(artworkUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(id)
                .setUri(Uri.parse(audioUrl))
                .setMediaMetadata(metadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun executeSearch(queryToSearch: String) {
        if (queryToSearch.isNotBlank()) {
            searchQuery = queryToSearch
            isSearching = true
            searchStatusMessage = null
            coroutineScope.launch {
                dao.insertSearchQuery(SearchHistoryEntity(query = queryToSearch.trim()))
                val (results, errorMsg) = searchOfficialSongs(queryToSearch)
                searchResults = results
                searchStatusMessage = errorMsg
                isSearching = false
            }
        }
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = {
                Text(text = "Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    val isTimerActive = sleepTimerRemainingSeconds > 0L || stopAfterCurrentTrack
                    if (isTimerActive) {
                        val statusText = if (stopAfterCurrentTrack) {
                            "Stopping after current track"
                        } else {
                            "Stopping in ${formatTime(sleepTimerRemainingSeconds * 1000)}"
                        }
                        Text(
                            text = "Status: $statusText",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    val minuteOptions = listOf(
                        "15 Minutes" to 15,
                        "30 Minutes" to 30,
                        "60 Minutes" to 60
                    )

                    minuteOptions.forEach { (label, mins) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    startSleepTimer(mins)
                                    showSleepTimerDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2D)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                cancelSleepTimer()
                                stopAfterCurrentTrack = true
                                showSleepTimerDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "End of Current Track",
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    if (isTimerActive) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    cancelSleepTimer()
                                    showSleepTimerDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF33161F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Turn Off Timer",
                                color = Color(0xFFFF5252),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF161622),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sonora",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (sleepTimerRemainingSeconds > 0L || stopAfterCurrentTrack) Color(0xFF1E1E2D) else Color.Transparent)
                    .clickable { showSleepTimerDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "🌙", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        sleepTimerRemainingSeconds > 0L -> formatTime(sleepTimerRemainingSeconds * 1000)
                        stopAfterCurrentTrack -> "Track End"
                        else -> "Timer"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (sleepTimerRemainingSeconds > 0L || stopAfterCurrentTrack) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8)
                )
            }
        }

        // Section Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Explore",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Liked Songs (${likedSongs.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // Tab 0: Explore & Search
        if (selectedTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search songs or artists...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { executeSearch(searchQuery) },
                    enabled = !isSearching && searchQuery.isNotBlank()
                ) {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (searchResults.isEmpty()) {
                    if (recentSearches.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "Clear All",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch { dao.clearSearchHistory() }
                                    }
                                )
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(recentSearches) { historyItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF161622))
                                            .clickable { executeSearch(historyItem.query) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🕒", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = historyItem.query,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "↖",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .clickable { searchQuery = historyItem.query }
                                        )
                                        Text(
                                            text = "✕",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 14.sp,
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        dao.deleteSearchQuery(historyItem.query)
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = searchStatusMessage ?: "Type a song name and tap Search",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchResults) { song ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        playTrack(
                                            id = song.id,
                                            title = song.title,
                                            artist = song.artist,
                                            audioUrl = song.audioUrl,
                                            artworkUrl = song.artworkUrl,
                                            durationFormatted = song.durationFormatted
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.artworkUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = song.artist,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (song.durationFormatted.isNotBlank() && song.durationFormatted != "00:00") {
                                        Text(
                                            text = song.durationFormatted,
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 1: Liked Songs Collection
        if (selectedTab == 1) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (likedSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "♥", fontSize = 42.sp, color = Color(0xFF262635))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No liked songs yet",
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the heart button on any playing song",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(likedSongs) { savedSong ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        playTrack(
                                            id = savedSong.id,
                                            title = savedSong.title,
                                            artist = savedSong.artist,
                                            audioUrl = savedSong.audioUrl,
                                            artworkUrl = savedSong.artworkUrl,
                                            durationFormatted = savedSong.duration
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = savedSong.artworkUrl,
                                        contentDescription = savedSong.title,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = savedSong.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = savedSong.artist,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                dao.deleteLikedSongById(savedSong.id)
                                            }
                                        }
                                    ) {
                                        Text("♥", color = Color(0xFFFF4081), fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Floating Bottom Player Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeArtworkUrl.isNotBlank()) {
                        AsyncImage(
                            model = activeArtworkUrl,
                            contentDescription = activeTitle,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeArtist,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Like Heart Button
                    IconButton(
                        onClick = {
                            if (activeSongId.isNotBlank()) {
                                coroutineScope.launch {
                                    if (isCurrentSongLiked) {
                                        dao.deleteLikedSongById(activeSongId)
                                    } else {
                                        dao.insertLikedSong(
                                            LikedSongEntity(
                                                id = activeSongId,
                                                title = activeTitle,
                                                artist = activeArtist,
                                                audioUrl = activeAudioUrl,
                                                artworkUrl = activeArtworkUrl,
                                                duration = activeDurationFormatted
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        enabled = activeSongId.isNotBlank()
                    ) {
                        Text(
                            text = if (isCurrentSongLiked) "♥" else "♡",
                            fontSize = 22.sp,
                            color = if (isCurrentSongLiked) Color(0xFFFF4081) else Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Play / Pause Button
                    Button(
                        onClick = {
                            controller?.let { player ->
                                if (player.isPlaying) player.pause() else player.play()
                            }
                        },
                        enabled = controller != null && controller?.mediaItemCount != 0,
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Text(if (isPlaying) "⏸" else "▶", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Slider
                val maxDurationFloat = max(1L, totalDuration).toFloat()
                val currentProgressFloat = if (isDraggingSlider) sliderDragValue else currentPosition.toFloat()

                Slider(
                    value = currentProgressFloat.coerceIn(0f, maxDurationFloat),
                    onValueChange = { newPos ->
                        isDraggingSlider = true
                        sliderDragValue = newPos
                    },
                    onValueChangeFinished = {
                        val seekPos = sliderDragValue.toLong()
                        controller?.seekTo(seekPos)
                        currentPosition = seekPos
                        isDraggingSlider = false
                        prefs.edit().putLong(KEY_LAST_POSITION_MS, seekPos).apply()
                    },
                    valueRange = 0f..maxDurationFloat,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDraggingSlider) sliderDragValue.toLong() else currentPosition),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = formatTime(totalDuration),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}