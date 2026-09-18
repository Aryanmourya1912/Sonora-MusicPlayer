package com.example.music

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.music.data.DownloadedSongEntity
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
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.max

data class FullTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    var audioUrl: String,
    val artworkUrl: String,
    val durationFormatted: String
)

data class DiscoveryCategory(
    val label: String,
    val searchQuery: String
)

val DiscoveryCategoryList = listOf(
    DiscoveryCategory("Chill", "Chill Lofi Beats"),
    DiscoveryCategory("Focus", "Deep Focus Study Music"),
    DiscoveryCategory("Commute", "Road Trip Hits"),
    DiscoveryCategory("Gaming", "Gaming Phonk EDM"),
    DiscoveryCategory("Energize", "Gym Workout Motivation"),
    DiscoveryCategory("Party", "Party Club Dance Hits"),
    DiscoveryCategory("Feel good", "Feel Good Upbeat Pop"),
    DiscoveryCategory("Romance", "Romantic Acoustic Love Songs")
)

private val SonoraThemeColors = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    background = Color(0xFF080C10),
    surface = Color(0xFF121921),
    surfaceVariant = Color(0xFF1A232E),
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

fun findRenderersRecursive(json: Any?, targetKey: String, sink: MutableList<JSONObject>) {
    when (json) {
        is JSONObject -> {
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                if (k == targetKey) {
                    val obj = json.optJSONObject(k)
                    if (obj != null) sink.add(obj)
                } else {
                    findRenderersRecursive(json.opt(k), targetKey, sink)
                }
            }
        }
        is JSONArray -> {
            for (i in 0 until json.length()) {
                findRenderersRecursive(json.opt(i), targetKey, sink)
            }
        }
    }
}

suspend fun searchYouTubeMusic(query: String): Pair<List<FullTrackItem>, String?> = withContext(Dispatchers.IO) {
    val results = mutableListOf<FullTrackItem>()
    try {
        val url = URL("https://music.youtube.com/youtubei/v1/search")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        conn.setRequestProperty("Referer", "https://music.youtube.com/")

        val payload = JSONObject().apply {
            put("query", query.trim())
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "WEB_REMIX")
                    put("clientVersion", "1.20231204.01.00")
                    put("hl", "en")
                    put("gl", "US")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            return@withContext Pair(emptyList(), "YouTube Music status: ${conn.responseCode}")
        }

        val respText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(respText)

        val renderers = mutableListOf<JSONObject>()
        findRenderersRecursive(root, "musicResponsiveListItemRenderer", renderers)

        for (item in renderers) {
            val videoId = item.optJSONObject("playlistItemData")?.optString("videoId")
                ?: item.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
                ?: ""

            if (videoId.isBlank()) continue

            val flexCols = item.optJSONArray("flexColumns") ?: continue
            val col0Runs = flexCols.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")
            val title = sanitizeText(col0Runs?.optJSONObject(0)?.optString("text", "Unknown Track") ?: "Unknown Track")

            val col1Runs = flexCols.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")
            val artist = sanitizeText(col1Runs?.optJSONObject(0)?.optString("text", "Unknown Artist") ?: "Unknown Artist")

            var duration = ""
            val fixedCols = item.optJSONArray("fixedColumns")
            if (fixedCols != null && fixedCols.length() > 0) {
                duration = fixedCols.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFixedColumnRenderer")
                    ?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "") ?: ""
            }
            if (duration.isBlank() && col1Runs != null && col1Runs.length() > 2) {
                duration = col1Runs.optJSONObject(col1Runs.length() - 1)?.optString("text", "") ?: ""
            }

            val thumbArray = item.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
                ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
            val artworkUrl = if (thumbArray != null && thumbArray.length() > 0) {
                thumbArray.getJSONObject(thumbArray.length() - 1).optString("url", "")
            } else ""

            results.add(
                FullTrackItem(
                    id = videoId,
                    title = title,
                    artist = artist,
                    audioUrl = "",
                    artworkUrl = artworkUrl,
                    durationFormatted = duration
                )
            )
        }
        Pair(results, null)
    } catch (e: Exception) {
        Pair(results, e.localizedMessage ?: "Network error occurred")
    }
}

suspend fun fetchYouTubeAutomixRadio(videoId: String): List<FullTrackItem> = withContext(Dispatchers.IO) {
    val results = mutableListOf<FullTrackItem>()
    if (videoId.isBlank()) return@withContext results
    try {
        val url = URL("https://music.youtube.com/youtubei/v1/next")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 7000
        conn.readTimeout = 7000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        conn.setRequestProperty("Referer", "https://music.youtube.com/")

        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("playlistId", "RDAMVM$videoId")
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "WEB_REMIX")
                    put("clientVersion", "1.20231204.01.00")
                    put("hl", "en")
                    put("gl", "US")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val respText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(respText)

            val renderers = mutableListOf<JSONObject>()
            findRenderersRecursive(root, "playlistPanelVideoRenderer", renderers)

            for (item in renderers) {
                val vId = item.optString("videoId", "")
                if (vId.isBlank()) continue

                val title = sanitizeText(item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Unknown Track") ?: "Unknown Track")
                val artist = sanitizeText(
                    item.optJSONObject("longBylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: item.optJSONObject("shortBylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: "Unknown Artist"
                )
                val duration = item.optJSONObject("lengthText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "") ?: ""

                val thumbArray = item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                val artworkUrl = if (thumbArray != null && thumbArray.length() > 0) {
                    thumbArray.getJSONObject(thumbArray.length() - 1).optString("url", "")
                } else ""

                results.add(
                    FullTrackItem(
                        id = vId,
                        title = title,
                        artist = artist,
                        audioUrl = "",
                        artworkUrl = artworkUrl,
                        durationFormatted = duration
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    results
}

// Multi-strategy audio stream resolver
suspend fun resolveYouTubeStreamUrl(videoId: String): Pair<String, String?> = withContext(Dispatchers.IO) {
    if (videoId.isBlank()) return@withContext Pair("", "Empty video ID")
    var lastError: String? = null

    // Strategy 1: Production iOS Client (returns unciphered direct audio streams)
    try {
        val url = URL("https://www.youtube.com/youtubei/v1/player")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "com.google.ios.youtube/19.29.1 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X; en_US)")
        conn.setRequestProperty("X-YouTube-Client-Name", "5")
        conn.setRequestProperty("X-YouTube-Client-Version", "19.29.1")
        conn.setRequestProperty("Origin", "https://www.youtube.com")

        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "IOS")
                    put("clientVersion", "19.29.1")
                    put("deviceMake", "Apple")
                    put("deviceModel", "iPhone14,3")
                    put("osName", "iOS")
                    put("osVersion", "15.6.0.19G71")
                    put("hl", "en")
                    put("gl", "US")
                })
            })
            put("playbackContext", JSONObject().apply {
                put("contentPlaybackContext", JSONObject().apply {
                    put("html5Preference", "HTML5_PREF_WANTS")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(resp)
            val status = root.optJSONObject("playabilityStatus")?.optString("status")
            if (status != null && status != "OK") {
                lastError = root.optJSONObject("playabilityStatus")?.optString("reason", status)
            }

            val streamingData = root.optJSONObject("streamingData")
            val formats = streamingData?.optJSONArray("adaptiveFormats")
            if (formats != null) {
                var bestUrl = ""
                var maxBitrate = 0L
                for (i in 0 until formats.length()) {
                    val fmt = formats.getJSONObject(i)
                    val mime = fmt.optString("mimeType", "")
                    val streamUrl = fmt.optString("url", "")
                    if (mime.startsWith("audio/") && streamUrl.isNotBlank()) {
                        val bitrate = fmt.optLong("bitrate", 0L)
                        if (bitrate > maxBitrate) {
                            maxBitrate = bitrate
                            bestUrl = streamUrl
                        }
                    }
                }
                if (bestUrl.isNotBlank()) return@withContext Pair(bestUrl, null)
            }
        } else {
            lastError = "YouTube returned status ${conn.responseCode}"
        }
    } catch (e: Exception) {
        lastError = e.localizedMessage ?: "Connection error"
    }

    // Strategy 2: Android Testsuite Client
    try {
        val url = URL("https://www.youtube.com/youtubei/v1/player")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "GoogleTest/1.0")

        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "ANDROID_TESTSUITE")
                    put("clientVersion", "1.9")
                    put("androidSdkVersion", 31)
                    put("hl", "en")
                    put("gl", "US")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(resp)
            val formats = root.optJSONObject("streamingData")?.optJSONArray("adaptiveFormats")
            if (formats != null) {
                var bestUrl = ""
                var maxBitrate = 0L
                for (i in 0 until formats.length()) {
                    val fmt = formats.getJSONObject(i)
                    val mime = fmt.optString("mimeType", "")
                    val streamUrl = fmt.optString("url", "")
                    if (mime.startsWith("audio/") && streamUrl.isNotBlank()) {
                        val bitrate = fmt.optLong("bitrate", 0L)
                        if (bitrate > maxBitrate) {
                            maxBitrate = bitrate
                            bestUrl = streamUrl
                        }
                    }
                }
                if (bestUrl.isNotBlank()) return@withContext Pair(bestUrl, null)
            }
        }
    } catch (e: Exception) {
        // Continue to Strategy 3
    }

    // Strategy 3: Active Invidious mirror resolvers
    val mirrors = listOf(
        "https://inv.nadeko.net/api/v1/videos/$videoId",
        "https://invidious.nerdvpn.de/api/v1/videos/$videoId",
        "https://yewtu.be/api/v1/videos/$videoId",
        "https://inv.tux.pizza/api/v1/videos/$videoId"
    )

    for (mirror in mirrors) {
        try {
            val url = URL(mirror)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(resp)
                val formats = root.optJSONArray("adaptiveFormats")
                if (formats != null) {
                    var bestUrl = ""
                    var maxBitrate = 0L
                    for (i in 0 until formats.length()) {
                        val fmt = formats.getJSONObject(i)
                        val type = fmt.optString("type", "")
                        val sUrl = fmt.optString("url", "")
                        val bitrate = fmt.optLong("bitrate", 0L)
                        if (type.startsWith("audio/") && sUrl.isNotBlank() && bitrate >= maxBitrate) {
                            maxBitrate = bitrate
                            bestUrl = sUrl
                        }
                    }
                    if (bestUrl.isNotBlank()) return@withContext Pair(bestUrl, null)
                }
            }
        } catch (e: Exception) {
            continue
        }
    }

    Pair("", lastError ?: "Audio stream unavailable")
}

fun cleanSongTitle(rawTitle: String): String {
    var clean = rawTitle.lowercase(Locale.ROOT)
    clean = clean.replace("\\(.*?\\)".toRegex(), " ")
    clean = clean.replace("\\[.*?\\]".toRegex(), " ")
    clean = clean.replace("\\{.*?\\}".toRegex(), " ")
    clean = clean.replace("-\\s*(slowed|reverb|remix|acoustic|live|sped up|speed up|lofi|instrumental|edit|deluxe|remastered|version|from|soundtrack|ost).*".toRegex(), " ")
    clean = clean.replace("\\b(slowed|reverb|remix|acoustic|live|sped up|speed up|lofi|instrumental|edit|deluxe|remastered|remaster|version|soundtrack|ost|audio|video|lyrics|official)\\b".toRegex(), " ")
    clean = clean.replace("\\b(feat|ft)\\.?\\s+.*".toRegex(), " ")
    clean = clean.replace("[^a-z0-9 ]".toRegex(), " ")
    return clean.trim().replace("\\s+".toRegex(), " ")
}

fun cleanArtist(rawArtist: String): String {
    val firstArtist = rawArtist.split(",", "&", "feat.", "ft.", "and", "/", ";").firstOrNull() ?: rawArtist
    return firstArtist.lowercase(Locale.ROOT)
        .replace("[^a-z0-9 ]".toRegex(), " ")
        .trim()
        .replace("\\s+".toRegex(), " ")
}

fun areTracksSimilar(trackA: FullTrackItem, trackB: FullTrackItem): Boolean {
    if (trackA.id.isNotBlank() && trackA.id == trackB.id) return true

    val titleA = cleanSongTitle(trackA.title)
    val titleB = cleanSongTitle(trackB.title)
    if (titleA.isBlank() || titleB.isBlank()) return false

    val artistA = cleanArtist(trackA.artist)
    val artistB = cleanArtist(trackB.artist)
    val artistsMatch = artistA.isBlank() || artistB.isBlank() || artistA == artistB ||
            artistA.contains(artistB) || artistB.contains(artistA)

    if (titleA == titleB && artistsMatch) return true

    if (titleA.length >= 5 && titleB.length >= 5) {
        if ((titleA.contains(titleB) || titleB.contains(titleA)) && artistsMatch) {
            return true
        }
    }
    return false
}

fun filterSimilarTracks(incoming: List<FullTrackItem>, existingQueue: List<FullTrackItem>): List<FullTrackItem> {
    val result = mutableListOf<FullTrackItem>()
    val pool = existingQueue.toMutableList()

    for (candidate in incoming) {
        val isDuplicate = pool.any { existing -> areTracksSimilar(candidate, existing) }
        if (!isDuplicate) {
            result.add(candidate)
            pool.add(candidate)
        }
    }
    return result
}

fun buildMediaItem(track: FullTrackItem): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(track.title)
        .setArtist(track.artist)
        .setArtworkUri(Uri.parse(track.artworkUrl))
        .build()

    val playbackUri = if (track.audioUrl.startsWith("/")) {
        Uri.fromFile(File(track.audioUrl))
    } else {
        Uri.parse(track.audioUrl)
    }

    return MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(playbackUri)
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(playbackUri)
                .build()
        )
        .setMediaMetadata(metadata)
        .build()
}

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

suspend fun downloadTrackToStorage(context: Context, track: FullTrackItem): String? = withContext(Dispatchers.IO) {
    try {
        val (streamUrl, _) = if (track.audioUrl.startsWith("http")) Pair(track.audioUrl, null) else resolveYouTubeStreamUrl(track.id)
        if (streamUrl.isBlank()) return@withContext null

        val downloadFolder = File(context.filesDir, "sonora_offline").apply { if (!exists()) mkdirs() }
        val cleanName = "${track.id}.m4a"
        val targetFile = File(downloadFolder, cleanName)

        if (targetFile.exists() && targetFile.length() > 50_000L) {
            return@withContext targetFile.absolutePath
        }

        val url = URL(streamUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 25000
        connection.instanceFollowRedirects = true
        connection.connect()

        if (connection.responseCode in 200..299) {
            connection.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 50_000L) {
                return@withContext targetFile.absolutePath
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext null
}

fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}

fun sanitizeText(input: String): String {
    return input.replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&#039;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}

@Composable
fun RefinedDownloadMark(
    isDownloaded: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isDownloaded -> Color(0xFF0C2E1F)
                    isDownloading -> Color(0xFF17202A)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = !isDownloaded && !isDownloading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isDownloading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF7C4DFF),
                    trackColor = Color(0x337C4DFF)
                )
            }
            isDownloaded -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Downloaded",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Download",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RefinedLikeMark(
    isLiked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFF3B70) else Color(0xFF94A3B8),
        animationSpec = tween(250),
        label = "heartColor"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isLiked) "Liked" else "Like",
            tint = heartColor,
            modifier = Modifier.size(20.dp)
        )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SonoraPlayerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val prefs: SharedPreferences = remember {
        context.getSharedPreferences(PREFS_SONORA, Context.MODE_PRIVATE)
    }

    val database = remember { SonoraDatabase.getDatabase(context) }
    val dao = remember { database.sonoraDao() }

    val recentSearches by dao.getRecentSearches().collectAsState(initial = emptyList())
    val likedSongs by dao.getAllLikedSongs().collectAsState(initial = emptyList())
    val allPlaylists by dao.getAllPlaylists().collectAsState(initial = emptyList())
    val downloadedSongs by dao.getAllDownloadedSongs().collectAsState(initial = emptyList())

    var selectedNavTab by remember { mutableIntStateOf(0) }
    var selectedLibrarySubTab by remember { mutableIntStateOf(0) }
    var viewingPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    val activePlaylistSongs by remember(viewingPlaylist?.id) {
        viewingPlaylist?.let { dao.getSongsForPlaylist(it.id) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    var isPlayerExpanded by remember { mutableStateOf(false) }
    var selectedTrackForOptions by remember { mutableStateOf<FullTrackItem?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var queueList by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var endlessRadioEnabled by remember { mutableStateOf(true) }
    var showQueueDialog by remember { mutableStateOf(false) }
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var repeatModeState by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }

    var downloadingSongIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedMoodCategory by remember { mutableStateOf(DiscoveryCategoryList[0]) }
    var moodTracks by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isMoodLoading by remember { mutableStateOf(false) }

    var activeSongId by remember { mutableStateOf("") }
    var activeTitle by remember { mutableStateOf("No Track Playing") }
    var activeArtist by remember { mutableStateOf("Select a song to start listening") }
    var activeArtworkUrl by remember { mutableStateOf("") }
    var activeAudioUrl by remember { mutableStateOf("") }
    var activeDurationFormatted by remember { mutableStateOf("0:00") }

    val isCurrentSongLiked by dao.isSongLiked(activeSongId).collectAsState(initial = false)
    val isCurrentSongDownloaded by dao.isSongDownloaded(activeSongId).collectAsState(initial = false)

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

    val maxSysVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolumeSlider by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }

    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    LaunchedEffect(selectedMoodCategory) {
        isMoodLoading = true
        val (tracks, _) = searchYouTubeMusic(selectedMoodCategory.searchQuery)
        moodTracks = tracks
        isMoodLoading = false
    }

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

    fun updateQueueState(player: Player) {
        val count = player.mediaItemCount
        val items = ArrayList<FullTrackItem>(count)
        for (i in 0 until count) {
            items.add(mediaItemToTrack(player.getMediaItemAt(i)))
        }
        queueList = items
        currentTrackIndex = player.currentMediaItemIndex
    }

    fun triggerDownload(track: FullTrackItem) {
        if (track.id in downloadingSongIds) return
        downloadingSongIds = downloadingSongIds + track.id
        coroutineScope.launch {
            val localPath = downloadTrackToStorage(context, track)
            if (localPath != null) {
                dao.insertDownloadedSong(
                    DownloadedSongEntity(
                        id = track.id,
                        title = track.title,
                        artist = track.artist,
                        localFilePath = localPath,
                        artworkUrl = track.artworkUrl,
                        duration = track.durationFormatted
                    )
                )
                Toast.makeText(context, "Downloaded \"${track.title}\"", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            }
            downloadingSongIds = downloadingSongIds - track.id
        }
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
                val savedId = prefs.getString(KEY_LAST_ID, "") ?: ""
                if (savedId.isNotBlank()) {
                    val savedTitle = prefs.getString(KEY_LAST_TITLE, "Last Played Track") ?: ""
                    val savedArtist = prefs.getString(KEY_LAST_ARTIST, "Tap play to resume") ?: ""
                    val savedArtworkUrl = prefs.getString(KEY_LAST_ARTWORK_URL, "") ?: ""
                    val savedDurationTxt = prefs.getString(KEY_LAST_DURATION_TXT, "0:00") ?: ""
                    val savedPosMs = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
                    val savedDurMs = prefs.getLong(KEY_LAST_DURATION_MS, 0L)

                    activeSongId = savedId
                    activeTitle = savedTitle
                    activeArtist = savedArtist
                    activeArtworkUrl = savedArtworkUrl
                    activeDurationFormatted = savedDurationTxt
                    currentPosition = savedPosMs
                    totalDuration = savedDurMs

                    coroutineScope.launch {
                        val (streamUrl, _) = resolveYouTubeStreamUrl(savedId)
                        if (streamUrl.isNotBlank()) {
                            activeAudioUrl = streamUrl
                            val restoredTrack = FullTrackItem(
                                id = savedId,
                                title = savedTitle,
                                artist = savedArtist,
                                audioUrl = streamUrl,
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
                }
            }

            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                    if (!playing && mediaController.currentPosition > 0) {
                        prefs.edit().putLong(KEY_LAST_POSITION_MS, mediaController.currentPosition).apply()
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

                    if (endlessRadioEnabled && mediaController.currentMediaItemIndex >= mediaController.mediaItemCount - 2) {
                        coroutineScope.launch {
                            val similar = fetchYouTubeAutomixRadio(activeSongId)
                            val currentQueueTracks = (0 until mediaController.mediaItemCount).map { idx ->
                                mediaItemToTrack(mediaController.getMediaItemAt(idx))
                            }
                            val filteredSongs = filterSimilarTracks(similar, currentQueueTracks)

                            for (song in filteredSongs.take(5)) {
                                val (sUrl, _) = resolveYouTubeStreamUrl(song.id)
                                if (sUrl.isNotBlank()) {
                                    song.audioUrl = sUrl
                                    mediaController.addMediaItem(buildMediaItem(song))
                                }
                            }
                            updateQueueState(mediaController)
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

    fun playQueue(tracks: List<FullTrackItem>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val safeIndex = startIndex.coerceIn(0, tracks.size - 1)
        val targetTrack = tracks[safeIndex]

        coroutineScope.launch {
            val downloadedLocal = dao.getDownloadedSongById(targetTrack.id)
            val (effectiveUrl, errDetail) = if (downloadedLocal != null && File(downloadedLocal.localFilePath).exists()) {
                Pair(downloadedLocal.localFilePath, null)
            } else {
                resolveYouTubeStreamUrl(targetTrack.id)
            }

            if (effectiveUrl.isBlank()) {
                Toast.makeText(context, "Stream error: ${errDetail ?: "Playback failed"}", Toast.LENGTH_LONG).show()
                return@launch
            }

            targetTrack.audioUrl = effectiveUrl
            activeSongId = targetTrack.id
            activeTitle = targetTrack.title
            activeArtist = targetTrack.artist
            activeArtworkUrl = targetTrack.artworkUrl
            activeAudioUrl = effectiveUrl
            activeDurationFormatted = targetTrack.durationFormatted

            prefs.edit()
                .putString(KEY_LAST_ID, targetTrack.id)
                .putString(KEY_LAST_TITLE, targetTrack.title)
                .putString(KEY_LAST_ARTIST, targetTrack.artist)
                .putString(KEY_LAST_AUDIO_URL, effectiveUrl)
                .putString(KEY_LAST_ARTWORK_URL, targetTrack.artworkUrl)
                .putString(KEY_LAST_DURATION_TXT, targetTrack.durationFormatted)
                .putLong(KEY_LAST_POSITION_MS, 0L)
                .apply()

            controller?.let { player ->
                player.setMediaItem(buildMediaItem(targetTrack))
                player.prepare()
                player.play()
                updateQueueState(player)
            }

            if (endlessRadioEnabled) {
                val radioSongs = fetchYouTubeAutomixRadio(targetTrack.id)
                val filtered = filterSimilarTracks(radioSongs, listOf(targetTrack))

                for (song in filtered.take(6)) {
                    val (sUrl, _) = resolveYouTubeStreamUrl(song.id)
                    if (sUrl.isNotBlank()) {
                        song.audioUrl = sUrl
                        controller?.addMediaItem(buildMediaItem(song))
                    }
                }
                controller?.let { updateQueueState(it) }
            }
        }
    }

    fun executeSearch(queryToSearch: String) {
        if (queryToSearch.isNotBlank()) {
            searchQuery = queryToSearch
            isSearching = true
            coroutineScope.launch {
                dao.insertSearchQuery(SearchHistoryEntity(query = queryToSearch.trim()))
                val (results, _) = searchYouTubeMusic(queryToSearch)
                searchResults = results
                isSearching = false
            }
        }
    }

    // Modal Bottom Sheet: Track Options
    if (selectedTrackForOptions != null) {
        val song = selectedTrackForOptions!!
        ModalBottomSheet(
            onDismissRequest = { selectedTrackForOptions = null },
            sheetState = bottomSheetState,
            containerColor = Color(0xFF0F151C),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1B232D))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "Volume",
                        tint = Color(0xFFD3E2F8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Slider(
                        value = currentVolumeSlider,
                        onValueChange = { newVol ->
                            currentVolumeSlider = newVol
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                        },
                        valueRange = 0f..maxSysVolume,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD3E2F8),
                            activeTrackColor = Color(0xFFD3E2F8),
                            inactiveTrackColor = Color(0xFF2A3644)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                playQueue(listOf(song), 0)
                                selectedTrackForOptions = null
                                Toast.makeText(context, "Started radio for ${song.title}", Toast.LENGTH_SHORT).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Rounded.Radio, contentDescription = "Radio", tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Start radio", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                songToAddToPlaylist = song
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Rounded.PlaylistAdd, contentDescription = "Add to playlist", tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Add to playl", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Song Link", "https://music.youtube.com/watch?v=${song.id}")
                                clipboard.setPrimaryClip(clip)
                                selectedTrackForOptions = null
                                Toast.makeText(context, "Copied YouTube Music link", Toast.LENGTH_SHORT).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Rounded.Link, contentDescription = "Copy link", tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Copy link", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                selectedNavTab = 1
                                executeSearch(song.artist)
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("View artist", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(song.artist, fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                selectedNavTab = 1
                                executeSearch(song.title)
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Album, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("View album", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(song.title, fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                coroutineScope.launch {
                                    dao.insertLikedSong(
                                        LikedSongEntity(
                                            id = song.id,
                                            title = song.title,
                                            artist = song.artist,
                                            audioUrl = song.audioUrl,
                                            artworkUrl = song.artworkUrl,
                                            duration = song.durationFormatted
                                        )
                                    )
                                    Toast.makeText(context, "Added to Liked Songs", Toast.LENGTH_SHORT).show()
                                }
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.BookmarkAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Add to library", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                triggerDownload(song)
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Download", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                Toast.makeText(context, "${song.title} by ${song.artist}\nYouTube ID: ${song.id}", Toast.LENGTH_LONG).show()
                                selectedTrackForOptions = null
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Details", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("YouTube Music Track Info", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text(text = "Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("15 Minutes" to 15, "30 Minutes" to 30, "60 Minutes" to 60).forEach { (label, mins) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    startSleepTimer(mins)
                                    showSleepTimerDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2631)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(14.dp))
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2631)),
                            shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("End of Current Track", color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(14.dp))
                    }
                    if (sleepTimerRemainingSeconds > 0L || stopAfterCurrentTrack) {
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
                            Text("Turn Off Timer", color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
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
            containerColor = Color(0xFF161F29),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (songToAddToPlaylist != null) {
        AlertDialog(
            onDismissRequest = { songToAddToPlaylist = null },
            title = { Text("Add to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Button(
                        onClick = { showCreatePlaylistDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("+ Create New Playlist", fontWeight = FontWeight.Bold)
                    }
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
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2631)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Folder, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(playlist.name, color = Color.White)
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
            containerColor = Color(0xFF161F29),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = { Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            },
            containerColor = Color(0xFF161F29),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showQueueDialog) {
        AlertDialog(
            onDismissRequest = { showQueueDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Up Next Queue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Radio", color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
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
                LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    itemsIndexed(queueList) { index, track ->
                        val isCurrent = index == currentTrackIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { controller?.seekToDefaultPosition(index) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFF263242) else Color(0xFF141C24)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = "Playing",
                                        tint = Color(0xFFD3E2F8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                AsyncImage(
                                    model = track.artworkUrl,
                                    contentDescription = track.title,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(track.artist, color = Color(0xFF94A3B8), fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQueueDialog = false }) { Text("Close", color = Color(0xFF94A3B8)) }
            },
            containerColor = Color(0xFF161F29),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF070B10))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedNavTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Home",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showSleepTimerDialog = true }) {
                                        Icon(imageVector = Icons.Rounded.Bedtime, contentDescription = "Timer", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { showQueueDialog = true }) {
                                        Icon(imageVector = Icons.Rounded.TrendingUp, contentDescription = "Queue", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Rounded.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Mood and Genres",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                item {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        for (i in DiscoveryCategoryList.indices step 2) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                val firstCat = DiscoveryCategoryList[i]
                                                Card(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(52.dp)
                                                        .clickable { selectedMoodCategory = firstCat },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (selectedMoodCategory == firstCat) Color(0xFF2A3644) else Color(0xFF141C24)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                                                        Text(firstCat.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                    }
                                                }

                                                if (i + 1 < DiscoveryCategoryList.size) {
                                                    val secondCat = DiscoveryCategoryList[i + 1]
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(52.dp)
                                                            .clickable { selectedMoodCategory = secondCat },
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (selectedMoodCategory == secondCat) Color(0xFF2A3644) else Color(0xFF141C24)
                                                        ),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                                                            Text(secondCat.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Heard in Shorts",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222B)),
                                            modifier = Modifier.clickable { playQueue(moodTracks, 0) }
                                        ) {
                                            Text(
                                                text = "Play all",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (isMoodLoading) {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                } else {
                                    itemsIndexed(moodTracks) { index, song ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clickable { playQueue(moodTracks, index) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = song.artworkUrl,
                                                contentDescription = song.title,
                                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(song.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                                                Text(song.artist, color = Color(0xFF94A3B8), fontSize = 13.sp, maxLines = 1)
                                            }
                                            IconButton(onClick = { selectedTrackForOptions = song }) {
                                                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(84.dp)) }
                            }
                        }
                    }

                    1 -> {
                        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        selectedNavTab = 0
                                    }) {
                                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }

                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Search YouTube Music...", color = Color(0xFF6B7280)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(24.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF141C24),
                                            unfocusedContainerColor = Color(0xFF141C24),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        trailingIcon = {
                                            if (searchQuery.isNotBlank()) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Clear",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(18.dp).clickable { searchQuery = "" }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(onClick = { executeSearch(searchQuery) }) {
                                        Icon(imageVector = Icons.Rounded.Language, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                if (isSearching) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(searchResults) { index, song ->
                                            val isDownloaded = downloadedSongs.any { it.id == song.id }
                                            val isDownloading = song.id in downloadingSongIds

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                                    .clickable { playQueue(searchResults, index) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = song.artworkUrl,
                                                    contentDescription = song.title,
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(song.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                                                    Text(song.artist, color = Color(0xFF94A3B8), fontSize = 13.sp, maxLines = 1)
                                                }
                                                RefinedDownloadMark(
                                                    isDownloaded = isDownloaded,
                                                    isDownloading = isDownloading,
                                                    onClick = { triggerDownload(song) }
                                                )
                                                IconButton(onClick = { selectedTrackForOptions = song }) {
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(recentSearches) { historyItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp)
                                                    .clickable { executeSearch(historyItem.query) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Rounded.History, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = historyItem.query,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Remove",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .padding(horizontal = 2.dp)
                                                        .clickable {
                                                            coroutineScope.launch { dao.deleteSearchQuery(historyItem.query) }
                                                        }
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(
                                                    imageVector = Icons.Rounded.NorthWest,
                                                    contentDescription = "Autofill",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(18.dp).clickable { searchQuery = historyItem.query }
                                                )
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                }
                            }

                            FloatingActionButton(
                                onClick = { Toast.makeText(context, "Voice Search coming soon", Toast.LENGTH_SHORT).show() },
                                containerColor = Color(0xFF384353),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 90.dp)
                                    .size(54.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.Mic, contentDescription = "Voice Search", modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Library",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Liked (${likedSongs.size})" to 0, "Playlists (${allPlaylists.size})" to 1, "Offline (${downloadedSongs.size})" to 2).forEach { (label, idx) ->
                                    val isSubSel = selectedLibrarySubTab == idx
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSubSel) Color(0xFF283444) else Color(0xFF141C24)
                                        ),
                                        modifier = Modifier.clickable { selectedLibrarySubTab = idx }
                                    ) {
                                        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                    }
                                }
                            }

                            when (selectedLibrarySubTab) {
                                0 -> {
                                    val convertedLiked = likedSongs.map { FullTrackItem(it.id, it.title, it.artist, it.audioUrl, it.artworkUrl, it.duration) }
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(convertedLiked) { index, savedSong ->
                                            val isDownloaded = downloadedSongs.any { it.id == savedSong.id }
                                            val isDownloading = savedSong.id in downloadingSongIds

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { playQueue(convertedLiked, index) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(model = savedSong.artworkUrl, contentDescription = savedSong.title, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(savedSong.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                                                    Text(savedSong.artist, color = Color(0xFF94A3B8), fontSize = 13.sp, maxLines = 1)
                                                }
                                                RefinedDownloadMark(
                                                    isDownloaded = isDownloaded,
                                                    isDownloading = isDownloading,
                                                    onClick = { triggerDownload(savedSong) }
                                                )
                                                IconButton(onClick = { selectedTrackForOptions = savedSong }) {
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                }
                                1 -> {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        item {
                                            Button(
                                                onClick = { showCreatePlaylistDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                            ) { Text("+ New Playlist") }
                                        }
                                        items(allPlaylists) { playlist ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewingPlaylist = playlist },
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C24))
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Rounded.Folder, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(24.dp))
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(playlist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                }
                                2 -> {
                                    val convertedDownloads = downloadedSongs.map { FullTrackItem(it.id, it.title, it.artist, it.localFilePath, it.artworkUrl, it.duration) }
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(convertedDownloads) { index, song ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { playQueue(convertedDownloads, index) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(model = song.artworkUrl, contentDescription = song.title, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(song.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                                                    Text(song.artist, color = Color(0xFF94A3B8), fontSize = 13.sp, maxLines = 1)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            File(song.audioUrl).delete()
                                                            dao.deleteDownloadedSong(song.id)
                                                        }
                                                    }
                                                ) {
                                                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                                                }
                                                IconButton(onClick = { selectedTrackForOptions = song }) {
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Miniplayer
            if (activeSongId.isNotBlank()) {
                val progressFraction = if (totalDuration > 0) (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { isPlayerExpanded = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable {
                                    controller?.let { player ->
                                        if (player.isPlaying) player.pause() else player.play()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 2.5.dp,
                                color = Color(0xFFD3E2F8),
                                trackColor = Color(0x33FFFFFF)
                            )
                            AsyncImage(
                                model = activeArtworkUrl,
                                contentDescription = activeTitle,
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x55000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

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

                        IconButton(
                            onClick = {
                                selectedTrackForOptions = FullTrackItem(
                                    id = activeSongId,
                                    title = activeTitle,
                                    artist = activeArtist,
                                    audioUrl = activeAudioUrl,
                                    artworkUrl = activeArtworkUrl,
                                    durationFormatted = activeDurationFormatted
                                )
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        RefinedLikeMark(
                            isLiked = isCurrentSongLiked,
                            onClick = {
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
                        )
                    }
                }
            }

            // Bottom Navigation Bar
            NavigationBar(
                containerColor = Color(0xFF0A0F14),
                contentColor = Color.White,
                modifier = Modifier.height(64.dp)
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = { Icon(imageVector = Icons.Rounded.Home, contentDescription = "Home", modifier = Modifier.size(22.dp)) },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFF283444)
                    )
                )
                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(22.dp)) },
                    label = { Text("Search", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFF283444)
                    )
                )
                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(imageVector = Icons.Rounded.LibraryMusic, contentDescription = "Library", modifier = Modifier.size(22.dp)) },
                    label = { Text("Library", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFF283444)
                    )
                )
            }
        }

        // Full Screen Now Playing View
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F2231), Color(0xFF070B10))
                        )
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isPlayerExpanded = false }) {
                            Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "Collapse", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Now Playing",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = activeTitle,
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141C24)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = activeArtworkUrl,
                            contentDescription = activeTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = activeArtist,
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFF283444))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Track", "https://music.youtube.com/watch?v=$activeSongId"))
                                            Toast.makeText(context, "Copied track link", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFF283444))
                                        .clickable {
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
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentSongLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isCurrentSongLiked) Color(0xFFFF3B70) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val maxDurationFloat = max(1L, totalDuration).toFloat()
                        val currentProgressFloat = if (isDraggingSlider) sliderDragValue else currentPosition.toFloat()

                        Slider(
                            value = currentProgressFloat.coerceIn(0f, maxDurationFloat),
                            onValueChange = {
                                isDraggingSlider = true
                                sliderDragValue = it
                            },
                            onValueChangeFinished = {
                                val seekPos = sliderDragValue.toLong()
                                controller?.seekTo(seekPos)
                                currentPosition = seekPos
                                isDraggingSlider = false
                                prefs.edit().putLong(KEY_LAST_POSITION_MS, seekPos).apply()
                            },
                            valueRange = 0f..maxDurationFloat,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color(0xFF2A3644)
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(if (isDraggingSlider) sliderDragValue.toLong() else currentPosition),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = formatTime(totalDuration),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8FA2B5))
                                .clickable {
                                    controller?.let { player ->
                                        if (player.currentPosition > 3000L) {
                                            player.seekTo(0L)
                                        } else if (player.hasPreviousMediaItem()) {
                                            player.seekToPreviousMediaItem()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Prev", tint = Color(0xFF0F1B26), modifier = Modifier.size(28.dp))
                        }

                        Box(
                            modifier = Modifier
                                .height(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFFD3E2F8))
                                .clickable {
                                    controller?.let { player ->
                                        if (player.isPlaying) player.pause() else player.play()
                                    }
                                }
                                .padding(horizontal = 34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color(0xFF0C141C),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPlaying) "Pause" else "Play",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0C141C)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8FA2B5))
                                .clickable {
                                    controller?.let { player ->
                                        if (player.hasNextMediaItem()) {
                                            player.seekToNextMediaItem()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color(0xFF0F1B26), modifier = Modifier.size(28.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141C24))
                                .clickable { showQueueDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141C24))
                                .clickable { showSleepTimerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.Bedtime, contentDescription = "Timer", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isShuffleEnabled) Color(0xFF2E3D4F) else Color(0xFF141C24))
                                .clickable {
                                    isShuffleEnabled = !isShuffleEnabled
                                    controller?.shuffleModeEnabled = isShuffleEnabled
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141C24))
                                .clickable { Toast.makeText(context, "Equalizer coming soon", Toast.LENGTH_SHORT).show() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.Tune, contentDescription = "Equalizer", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (repeatModeState != Player.REPEAT_MODE_OFF) Color(0xFF2E3D4F) else Color(0xFF141C24))
                                .clickable {
                                    repeatModeState = when (repeatModeState) {
                                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                        else -> Player.REPEAT_MODE_OFF
                                    }
                                    controller?.repeatMode = repeatModeState
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (repeatModeState == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                                contentDescription = "Repeat",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD3E2F8))
                                .clickable {
                                    selectedTrackForOptions = FullTrackItem(
                                        id = activeSongId,
                                        title = activeTitle,
                                        artist = activeArtist,
                                        audioUrl = activeAudioUrl,
                                        artworkUrl = activeArtworkUrl,
                                        durationFormatted = activeDurationFormatted
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "More", tint = Color(0xFF0F1B26), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}