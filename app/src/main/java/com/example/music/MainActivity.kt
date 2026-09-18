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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.example.music.data.LikedSongEntity
import com.example.music.data.PlaylistEntity
import com.example.music.data.PlaylistSongEntity
import com.example.music.data.SearchHistoryEntity
import com.example.music.data.SonoraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
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

// Builds an AndroidX MediaItem with embedded metadata and backup stream URI
fun buildMediaItem(track: FullTrackItem): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(track.title)
        .setArtist(track.artist)
        .setArtworkUri(Uri.parse(track.artworkUrl))
        .build()

    return MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(Uri.parse(track.audioUrl))
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(Uri.parse(track.audioUrl))
                .build()
        )
        .setMediaMetadata(metadata)
        .build()
}

// Converts a MediaItem from ExoPlayer back to FullTrackItem
fun mediaItemToTrack(item: MediaItem): FullTrackItem {
    val streamUri = item.requestMetadata.mediaUri?.toString()
        ?: item.localConfiguration?.uri?.toString()
        ?: ""
    return FullTrackItem(
        id = item.mediaId,
        title = item.mediaMetadata.title?.toString() ?: "Unknown Track",
        artist = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
        audioUrl = streamUri,
        artworkUrl = item.mediaMetadata.artworkUri?.toString() ?: "",
        durationFormatted = ""
    )
}

// JioSaavn Search API
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
            if (artUrl.startsWith("http://")) artUrl = artUrl.replaceFirst("http://", "https://")

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

// Smart Auto-Next & Endless Radio: fetches related tracks via reco.getreco
suspend fun fetchRelatedSongs(songId: String, artist: String): List<FullTrackItem> = withContext(Dispatchers.IO) {
    val resultsList = mutableListOf<FullTrackItem>()
    if (songId.isNotBlank()) {
        try {
            val endpoint = "https://www.jiosaavn.com/api.php?__call=reco.getreco&api_version=4&_format=json&_marker=0&ctx=web6dot0&pid=${URLEncoder.encode(songId, "UTF-8")}"
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            )
            connection.setRequestProperty("Accept", "application/json, text/plain, */*")
            connection.setRequestProperty("Referer", "https://www.jiosaavn.com/")
            connection.setRequestProperty("Cookie", "L=english; gdpr_acceptance=true;")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                val jsonArray = when {
                    responseText.startsWith("[") -> JSONArray(responseText)
                    responseText.startsWith("{") -> {
                        val obj = JSONObject(responseText)
                        obj.optJSONArray("results") ?: obj.optJSONArray("data")
                    }
                    else -> null
                }

                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
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
                        if (artUrl.startsWith("http://")) artUrl = artUrl.replaceFirst("http://", "https://")

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
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fallback: search by artist if recommendation returns empty
    if (resultsList.isEmpty() && artist.isNotBlank() && artist != "Unknown Artist") {
        val (artistSongs, _) = searchOfficialSongs(artist)
        resultsList.addAll(artistSongs.filter { it.id != songId })
    }

    resultsList
}

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
    val allPlaylists by dao.getAllPlaylists().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var viewingPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }

    val activePlaylistSongs by remember(viewingPlaylist?.id) {
        viewingPlaylist?.let { dao.getSongsForPlaylist(it.id) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Up Next Queue state
    var queueList by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var endlessRadioEnabled by remember { mutableStateOf(true) }
    var showQueueDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }

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

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sleepTimerRemainingSeconds by remember { mutableLongStateOf(0L) }
    var stopAfterCurrentTrack by remember { mutableStateOf(false) }
    var sleepTimerJob by remember { mutableStateOf<Job?>(null) }

    var songToAddToPlaylist by remember { mutableStateOf<FullTrackItem?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

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

    // Synchronizes the Compose queue state with ExoPlayer's live playlist
    fun updateQueueState(player: Player) {
        val count = player.mediaItemCount
        val items = ArrayList<FullTrackItem>(count)
        for (i in 0 until count) {
            items.add(mediaItemToTrack(player.getMediaItemAt(i)))
        }
        queueList = items
        currentTrackIndex = player.currentMediaItemIndex
    }

    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            controller = mediaController
            isPlaying = mediaController.isPlaying

            if (mediaController.mediaItemCount > 0) {
                val currentItem = mediaController.currentMediaItem
                activeSongId = currentItem?.mediaId ?: ""
                activeTitle = currentItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                activeArtist = currentItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                activeArtworkUrl = currentItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                currentPosition = max(0L, mediaController.currentPosition)
                totalDuration = if (mediaController.duration > 0) mediaController.duration else 0L
                updateQueueState(mediaController)
            } else {
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

                    val restoredTrack = FullTrackItem(
                        id = savedId,
                        title = savedTitle,
                        artist = savedArtist,
                        audioUrl = savedAudioUrl,
                        artworkUrl = savedArtworkUrl,
                        durationFormatted = savedDurationTxt
                    )

                    mediaController.setMediaItem(buildMediaItem(restoredTrack))
                    mediaController.prepare()
                    mediaController.seekTo(savedPosMs)
                    mediaController.pause()
                    updateQueueState(mediaController)
                }
            }

            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
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

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    updateQueueState(mediaController)
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
                    activeAudioUrl = mediaItem?.requestMetadata?.mediaUri?.toString()
                        ?: mediaItem?.localConfiguration?.uri?.toString() ?: ""
                    currentPosition = 0L

                    updateQueueState(mediaController)

                    prefs.edit()
                        .putString(KEY_LAST_ID, activeSongId)
                        .putString(KEY_LAST_TITLE, activeTitle)
                        .putString(KEY_LAST_ARTIST, activeArtist)
                        .putString(KEY_LAST_AUDIO_URL, activeAudioUrl)
                        .putString(KEY_LAST_ARTWORK_URL, activeArtworkUrl)
                        .putLong(KEY_LAST_POSITION_MS, 0L)
                        .apply()

                    // Endless Radio: Automatically appends related songs when within 2 tracks of queue end
                    if (endlessRadioEnabled && mediaController.currentMediaItemIndex >= mediaController.mediaItemCount - 2) {
                        coroutineScope.launch {
                            val similar = fetchRelatedSongs(activeSongId, activeArtist)
                            val existingIds = (0 until mediaController.mediaItemCount).map { idx ->
                                mediaController.getMediaItemAt(idx).mediaId
                            }.toSet()
                            val freshItems = similar.filter { it.id !in existingIds }.map { buildMediaItem(it) }
                            if (freshItems.isNotEmpty()) {
                                mediaController.addMediaItems(freshItems)
                                updateQueueState(mediaController)
                            }
                        }
                    }
                }
            })
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cancelSleepTimer()
            controller?.release()
        }
    }

    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            controller?.let { player ->
                val pos = max(0L, player.currentPosition)
                currentPosition = pos
                val dur = player.duration
                if (dur > 0) totalDuration = dur

                prefs.edit()
                    .putLong(KEY_LAST_POSITION_MS, pos)
                    .putLong(KEY_LAST_DURATION_MS, totalDuration)
                    .apply()
            }
            delay(1000L)
        }
    }

    // Loads a list into the player queue and begins playback from startIndex
    fun playQueue(tracks: List<FullTrackItem>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, tracks.size - 1)
        val targetTrack = tracks[safeIndex]

        activeSongId = targetTrack.id
        activeTitle = targetTrack.title
        activeArtist = targetTrack.artist
        activeArtworkUrl = targetTrack.artworkUrl
        activeAudioUrl = targetTrack.audioUrl
        activeDurationFormatted = targetTrack.durationFormatted

        prefs.edit()
            .putString(KEY_LAST_ID, targetTrack.id)
            .putString(KEY_LAST_TITLE, targetTrack.title)
            .putString(KEY_LAST_ARTIST, targetTrack.artist)
            .putString(KEY_LAST_AUDIO_URL, targetTrack.audioUrl)
            .putString(KEY_LAST_ARTWORK_URL, targetTrack.artworkUrl)
            .putString(KEY_LAST_DURATION_TXT, targetTrack.durationFormatted)
            .putLong(KEY_LAST_POSITION_MS, 0L)
            .apply()

        controller?.let { player ->
            val mediaItems = tracks.map { buildMediaItem(it) }
            player.setMediaItems(mediaItems, safeIndex, 0L)
            player.prepare()
            player.play()
            updateQueueState(player)
        }

        // When starting from a single track, proactively seed recommendations for continuous playback
        if (tracks.size == 1 && endlessRadioEnabled) {
            coroutineScope.launch {
                val related = fetchRelatedSongs(targetTrack.id, targetTrack.artist)
                controller?.let { player ->
                    val newItems = related.filter { it.id != targetTrack.id }.map { buildMediaItem(it) }
                    if (newItems.isNotEmpty()) {
                        player.addMediaItems(newItems)
                        updateQueueState(player)
                    }
                }
            }
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

    // --- Up Next Queue Dialog ---
    if (showQueueDialog) {
        AlertDialog(
            onDismissRequest = { showQueueDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Up Next Queue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${queueList.size} tracks", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Endless Radio", color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Switch(
                            checked = endlessRadioEnabled,
                            onCheckedChange = { endlessRadioEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            text = {
                if (queueList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text("Queue is empty. Tap any song to start playback.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                        itemsIndexed(queueList) { index, track ->
                            val isCurrent = index == currentTrackIndex
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        controller?.seekToDefaultPosition(index)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) Color(0xFF28243D) else Color(0xFF161622)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isCurrent) "▶" else "${index + 1}",
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(24.dp)
                                    )

                                    AsyncImage(
                                        model = track.artworkUrl,
                                        contentDescription = track.title,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Remove track from queue
                                    if (!isCurrent) {
                                        IconButton(
                                            onClick = {
                                                controller?.removeMediaItem(index)
                                                controller?.let { updateQueueState(it) }
                                            }
                                        ) {
                                            Text("✕", color = Color(0xFF64748B), fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQueueDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E1E2D),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Add to Playlist Picker Dialog ---
    if (songToAddToPlaylist != null) {
        AlertDialog(
            onDismissRequest = { songToAddToPlaylist = null },
            title = { Text("Add to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = songToAddToPlaylist?.title ?: "",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("+ Create New Playlist", fontWeight = FontWeight.Bold)
                    }

                    if (allPlaylists.isEmpty()) {
                        Text(
                            text = "No playlists created yet. Tap above to create one.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Select an existing playlist:",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(allPlaylists) { playlist ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val song = songToAddToPlaylist
                                            if (song != null) {
                                                coroutineScope.launch {
                                                    dao.addSongToPlaylist(
                                                        PlaylistSongEntity(
                                                            playlistId = playlist.id,
                                                            songId = song.id,
                                                            title = song.title,
                                                            artist = song.artist,
                                                            audioUrl = song.audioUrl,
                                                            artworkUrl = song.artworkUrl,
                                                            duration = song.durationFormatted
                                                        )
                                                    )
                                                }
                                            }
                                            songToAddToPlaylist = null
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2D)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📁", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = playlist.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { songToAddToPlaylist = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF161622),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Create New Playlist Dialog ---
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = { Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            val trimmedName = newPlaylistName.trim()
                            coroutineScope.launch {
                                val newId = dao.createPlaylist(PlaylistEntity(name = trimmedName))
                                songToAddToPlaylist?.let { song ->
                                    dao.addSongToPlaylist(
                                        PlaylistSongEntity(
                                            playlistId = newId,
                                            songId = song.id,
                                            title = song.title,
                                            artist = song.artist,
                                            audioUrl = song.audioUrl,
                                            artworkUrl = song.artworkUrl,
                                            duration = song.durationFormatted
                                        )
                                    )
                                    songToAddToPlaylist = null
                                }
                            }
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF161622),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Sleep Timer Dialog ---
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text(text = "Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold) },
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
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sleep Timer badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sleepTimerRemainingSeconds > 0L || stopAfterCurrentTrack) Color(0xFF1E1E2D) else Color.Transparent)
                        .clickable { showSleepTimerDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "🌙", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(5.dp))
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
                onClick = {
                    selectedTab = 0
                    viewingPlaylist = null
                },
                text = { Text("Explore", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    viewingPlaylist = null
                },
                text = { Text("Liked (${likedSongs.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Playlists (${allPlaylists.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        // --- Tab 0: Explore & Search ---
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
                        itemsIndexed(searchResults) { index, song ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        // Loads all search results into queue starting at this track
                                        playQueue(searchResults, index)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                                    // Add to Playlist Button
                                    IconButton(onClick = { songToAddToPlaylist = song }) {
                                        Text("+", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Tab 1: Liked Songs ---
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
                        }
                    }
                } else {
                    val convertedLiked = likedSongs.map {
                        FullTrackItem(it.id, it.title, it.artist, it.audioUrl, it.artworkUrl, it.duration)
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(convertedLiked) { index, savedSong ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        // Loads all liked songs into queue starting at this track
                                        playQueue(convertedLiked, index)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                                    IconButton(onClick = { songToAddToPlaylist = savedSong }) {
                                        Text("+", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

        // --- Tab 2: Playlists Management ---
        if (selectedTab == 2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (viewingPlaylist != null) {
                    val currentPlaylist = viewingPlaylist!!
                    val convertedPlaylistTracks = activePlaylistSongs.map {
                        FullTrackItem(it.songId, it.title, it.artist, it.audioUrl, it.artworkUrl, it.duration)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewingPlaylist = null }
                            ) {
                                Text("←", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentPlaylist.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${activePlaylistSongs.size} tracks",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Text(
                                text = "Delete",
                                fontSize = 13.sp,
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        dao.deletePlaylist(currentPlaylist.id)
                                        viewingPlaylist = null
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activePlaylistSongs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "This playlist is empty.\nTap '+' on any song to add it here.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(convertedPlaylistTracks) { index, track ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                // Loads playlist into queue starting at this track
                                                playQueue(convertedPlaylistTracks, index)
                                            },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = track.artworkUrl,
                                                contentDescription = track.title,
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = track.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = track.artist,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        dao.removeSongFromPlaylist(currentPlaylist.id, track.id)
                                                    }
                                                }
                                            ) {
                                                Text("✕", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Playlists",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            Button(
                                onClick = { showCreatePlaylistDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ New Playlist", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (allPlaylists.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📁", fontSize = 42.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No custom playlists yet",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(allPlaylists) { playlist ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clickable { viewingPlaylist = playlist },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1E1E2D)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🎵", fontSize = 20.sp)
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = playlist.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Tap to view tracks",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }

                                            Text("›", fontSize = 22.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Floating Player Bar with Up Next Queue & Complete Media Controls ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Artwork, Info, Heart, Playlist, Queue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeArtworkUrl.isNotBlank()) {
                        AsyncImage(
                            model = activeArtworkUrl,
                            contentDescription = activeTitle,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
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

                    // Add to Playlist Button
                    IconButton(
                        onClick = {
                            if (activeSongId.isNotBlank()) {
                                songToAddToPlaylist = FullTrackItem(
                                    id = activeSongId,
                                    title = activeTitle,
                                    artist = activeArtist,
                                    audioUrl = activeAudioUrl,
                                    artworkUrl = activeArtworkUrl,
                                    durationFormatted = activeDurationFormatted
                                )
                            }
                        },
                        enabled = activeSongId.isNotBlank()
                    ) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    // Favorite Heart Button
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
                            fontSize = 20.sp,
                            color = if (isCurrentSongLiked) Color(0xFFFF4081) else Color(0xFF94A3B8)
                        )
                    }

                    // Up Next Queue Button
                    IconButton(onClick = { showQueueDialog = true }) {
                        Text("≡", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

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
                    modifier = Modifier.fillMaxWidth().height(26.dp)
                )

                // Timestamps and Playback Control Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(if (isDraggingSlider) sliderDragValue.toLong() else currentPosition),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // Previous, Play/Pause, Next Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Previous Track
                        IconButton(
                            onClick = {
                                controller?.let { player ->
                                    if (player.currentPosition > 3000L) {
                                        player.seekTo(0L)
                                    } else if (player.hasPreviousMediaItem()) {
                                        player.seekToPreviousMediaItem()
                                    }
                                }
                            },
                            enabled = controller != null && controller?.mediaItemCount != 0
                        ) {
                            Text("⏮", fontSize = 20.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play / Pause Toggle
                        Button(
                            onClick = {
                                controller?.let { player ->
                                    if (player.isPlaying) player.pause() else player.play()
                                }
                            },
                            enabled = controller != null && controller?.mediaItemCount != 0,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Text(if (isPlaying) "⏸" else "▶", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Next Track
                        IconButton(
                            onClick = {
                                controller?.let { player ->
                                    if (player.hasNextMediaItem()) {
                                        player.seekToNextMediaItem()
                                    }
                                }
                            },
                            enabled = controller != null && controller?.hasNextMediaItem() == true
                        ) {
                            Text("⏭", fontSize = 20.sp, color = Color.White)
                        }
                    }

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