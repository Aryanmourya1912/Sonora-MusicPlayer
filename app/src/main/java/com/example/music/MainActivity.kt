package com.example.music

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
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
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Notes
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
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.music.data.DownloadedSongEntity
import com.example.music.data.LikedSongEntity
import com.example.music.data.PlaylistEntity
import com.example.music.data.PlaylistSongEntity
import com.example.music.data.SearchHistoryEntity
import com.example.music.data.SonoraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.max
import kotlin.math.roundToInt

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

data class SyncedLyricLine(
    val timeMs: Long,
    val text: String
)

val DiscoveryCategoryList = listOf(
    DiscoveryCategory("Trending Hindi", "Trending Hindi Bollywood Songs"),
    DiscoveryCategory("Trending English", "Global Top English Hits"),
    DiscoveryCategory("Bollywood Hits", "Top Bollywood Songs"),
    DiscoveryCategory("Punjabi Pop", "Trending Punjabi Hits"),
    DiscoveryCategory("Desi Hip Hop", "Desi Hip Hop Rap India"),
    DiscoveryCategory("Romantic Hindi", "Romantic Hindi Love Songs"),
    DiscoveryCategory("Indian Indie", "Indian Indie Pop Melodies"),
    DiscoveryCategory("Evergreen 90s", "90s Evergreen Bollywood Hits"),
    DiscoveryCategory("Bhakti", "Top Bhakti Hindi Songs")
)

private val SonoraDarkColors = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    background = Color(0xFF080C10),
    surface = Color(0xFF121921),
    surfaceVariant = Color(0xFF1A232E),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val SonoraLightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onBackground = Color(0xFF0F172A),       // Deep slate-black for headings & titles
    onSurface = Color(0xFF0F172A),          // Deep slate-black for labels & icons
    onSurfaceVariant = Color(0xFF475569)    // Mid-slate grey for secondary text
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            MaterialTheme(colorScheme = if (isDarkTheme) SonoraDarkColors else SonoraLightColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SonoraPlayerScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations) {
            val intent = Intent("com.example.music.ACTION_KILL_SERVICE").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)
        }
        super.onDestroy()
    }
}

suspend fun extractArtworkPaletteColors(context: Context, imageUrl: String): Pair<Color, Color> = withContext(Dispatchers.IO) {
    if (imageUrl.isBlank()) {
        return@withContext Pair(Color(0xFF1E2836), Color(0xFF0D1520))
    }
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            val palette = Palette.from(bitmap).generate()
            val dominant = palette.getDominantColor(0xFF1E2836.toInt())
            val secondary = palette.getDarkMutedColor(palette.getDarkVibrantColor(0xFF0D1520.toInt()))
            return@withContext Pair(Color(dominant), Color(secondary))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    Pair(Color(0xFF1E2836), Color(0xFF0D1520))
}

fun parseLrcLyrics(lrcString: String): List<SyncedLyricLine> {
    val lines = mutableListOf<SyncedLyricLine>()
    val regex = """\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""".toRegex()

    lrcString.lines().forEach { rawLine ->
        val match = regex.find(rawLine.trim())
        if (match != null) {
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val msRaw = match.groupValues[3]
            val ms = if (msRaw.length == 2) (msRaw.toLongOrNull() ?: 0L) * 10 else (msRaw.toLongOrNull() ?: 0L)
            val totalMs = (min * 60 + sec) * 1000 + ms
            val text = match.groupValues[4].trim()

            if (text.isNotBlank()) {
                lines.add(SyncedLyricLine(timeMs = totalMs, text = text))
            }
        }
    }
    return lines.sortedBy { it.timeMs }
}

fun convertPlainLyricsToTimed(plainText: String, durationSec: Int): List<SyncedLyricLine> {
    val cleanLines = plainText.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("[") }
    if (cleanLines.isEmpty()) return emptyList()

    val totalMs = if (durationSec > 10) durationSec * 1000L else cleanLines.size * 3500L
    val intervalMs = totalMs / cleanLines.size

    return cleanLines.mapIndexed { index, text ->
        SyncedLyricLine(timeMs = index * intervalMs, text = text)
    }
}

suspend fun fetchLyricsFromPriorityProviders(
    songTitle: String,
    artistName: String,
    durationSeconds: Int = 0
): Pair<String, List<SyncedLyricLine>> = withContext(Dispatchers.IO) {
    val providers = listOf(
        "Better Lyrics",
        "LrcLib",
        "KuGou",
        "Paxsenix",
        "LyricsPlus",
        "Zemer"
    )

    val cleanTitle = cleanSongTitle(songTitle)
    val cleanArtist = if (artistName.equals("Song", ignoreCase = true) || artistName.equals("Unknown Artist", ignoreCase = true)) "" else cleanArtist(artistName)

    for (provider in providers) {
        try {
            when (provider) {
                "LrcLib" -> {
                    val result = fetchFromLrcLib(cleanTitle, cleanArtist, durationSeconds)
                    if (result.isNotEmpty()) return@withContext Pair("LrcLib", result)
                }
                "KuGou" -> {
                    val result = fetchFromKuGou(cleanTitle, cleanArtist, durationSeconds)
                    if (result.isNotEmpty()) return@withContext Pair("KuGou", result)
                }
                else -> {
                    val result = fetchFromLrcLib(cleanTitle, "", durationSeconds)
                    if (result.isNotEmpty()) return@withContext Pair(provider, result)
                }
            }
        } catch (_: Exception) {}
    }

    Pair("Sonora", emptyList())
}

private fun fetchFromLrcLib(title: String, artist: String, duration: Int): List<SyncedLyricLine> {
    val queries = listOfNotNull(
        if (artist.isNotBlank()) "$title $artist" else null,
        title
    )

    for (q in queries) {
        try {
            val encodedQuery = URLEncoder.encode(q, "UTF-8")
            val endpoint = "https://lrclib.net/api/search?q=$encodedQuery"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "SonoraMusicPlayer/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(resp)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val synced = item.optString("syncedLyrics", "")
                    if (synced.isNotBlank()) {
                        val parsed = parseLrcLyrics(synced)
                        if (parsed.isNotEmpty()) return parsed
                    }
                }
                if (array.length() > 0) {
                    val plain = array.getJSONObject(0).optString("plainLyrics", "")
                    if (plain.isNotBlank()) {
                        return convertPlainLyricsToTimed(plain, duration)
                    }
                }
            }
        } catch (_: Exception) {}
    }
    return emptyList()
}

private fun fetchFromKuGou(title: String, artist: String, duration: Int): List<SyncedLyricLine> {
    return try {
        val query = URLEncoder.encode("$title $artist".trim(), "UTF-8")
        val searchUrl = "http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=$query&duration=&hash="
        val conn = (URL(searchUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3000
            readTimeout = 3000
        }
        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(resp)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val id = candidates.getJSONObject(0).optString("id")
                val accesskey = candidates.getJSONObject(0).optString("accesskey")
                val lrcUrl = "http://lyrics.kugou.com/download?ver=1&client=pc&id=$id&accesskey=$accesskey&fmt=lrc&charset=utf8"
                val dlConn = (URL(lrcUrl).openConnection() as HttpURLConnection)
                if (dlConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val dlResp = dlConn.inputStream.bufferedReader().use { it.readText() }
                    val b64 = JSONObject(dlResp).optString("content")
                    if (b64.isNotBlank()) {
                        val lrcText = String(Base64.decode(b64, Base64.DEFAULT), Charsets.UTF_8)
                        val parsed = parseLrcLyrics(lrcText)
                        if (parsed.isNotEmpty()) return parsed
                        return convertPlainLyricsToTimed(lrcText, duration)
                    }
                }
            }
        }
        emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun recordTrackPlay(context: Context, track: FullTrackItem) {
    if (track.id.isBlank()) return
    val prefs = context.getSharedPreferences("sonora_play_counts", Context.MODE_PRIVATE)
    val currentCount = prefs.getInt("count_${track.id}", 0)
    prefs.edit()
        .putInt("count_${track.id}", currentCount + 1)
        .putString("title_${track.id}", track.title)
        .putString("artist_${track.id}", track.artist)
        .putString("art_${track.id}", track.artworkUrl)
        .putString("dur_${track.id}", track.durationFormatted)
        .putLong("last_played_${track.id}", System.currentTimeMillis())
        .apply()
}

fun getMostPlayedTracks(context: Context): List<FullTrackItem> {
    val prefs = context.getSharedPreferences("sonora_play_counts", Context.MODE_PRIVATE)
    val allKeys = prefs.all
    val songIds = allKeys.keys.filter { it.startsWith("count_") }.map { it.removePrefix("count_") }
    return songIds.map { id ->
        val count = prefs.getInt("count_$id", 0)
        val title = prefs.getString("title_$id", "Unknown Track") ?: "Unknown Track"
        val artist = prefs.getString("artist_$id", "Unknown Artist") ?: "Unknown Artist"
        val art = prefs.getString("art_$id", "") ?: ""
        val dur = prefs.getString("dur_$id", "0:00") ?: "0:00"
        val lastPlayed = prefs.getLong("last_played_$id", 0L)
        Triple(FullTrackItem(id, title, artist, "", art, dur), count, lastPlayed)
    }.sortedWith(compareByDescending<Triple<FullTrackItem, Int, Long>> { it.second }.thenByDescending { it.third })
        .map { it.first }
}

fun getRecentlyPlayedTracks(context: Context): List<FullTrackItem> {
    val prefs = context.getSharedPreferences("sonora_play_counts", Context.MODE_PRIVATE)
    val allKeys = prefs.all
    val songIds = allKeys.keys.filter { it.startsWith("last_played_") }.map { it.removePrefix("last_played_") }
    return songIds.map { id ->
        val title = prefs.getString("title_$id", "Unknown Track") ?: "Unknown Track"
        val artist = prefs.getString("artist_$id", "Unknown Artist") ?: "Unknown Artist"
        val art = prefs.getString("art_$id", "") ?: ""
        val dur = prefs.getString("dur_$id", "0:00") ?: "0:00"
        val lastPlayed = prefs.getLong("last_played_$id", 0L)
        Pair(FullTrackItem(id, title, artist, "", art, dur), lastPlayed)
    }.sortedByDescending { it.second }
        .map { it.first }
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

fun extractVideoIdFromRenderer(item: JSONObject): String {
    item.optString("videoId").takeIf { it.isNotBlank() }?.let { return it }
    item.optJSONObject("playlistItemData")?.optString("videoId")?.takeIf { it.isNotBlank() }?.let { return it }
    item.optJSONObject("overlay")
        ?.optJSONObject("musicItemThumbnailOverlayRenderer")
        ?.optJSONObject("content")
        ?.optJSONObject("musicPlayButtonRenderer")
        ?.optJSONObject("playNavigationEndpoint")
        ?.optJSONObject("watchEndpoint")
        ?.optString("videoId")?.takeIf { it.isNotBlank() }?.let { return it }

    val flexCols = item.optJSONArray("flexColumns")
    if (flexCols != null) {
        for (c in 0 until flexCols.length()) {
            val runs = flexCols.optJSONObject(c)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
            if (runs != null) {
                for (r in 0 until runs.length()) {
                    val vId = runs.optJSONObject(r)
                        ?.optJSONObject("navigationEndpoint")
                        ?.optJSONObject("watchEndpoint")
                        ?.optString("videoId")
                    if (!vId.isNullOrBlank()) return vId
                }
            }
        }
    }
    item.optJSONObject("navigationEndpoint")
        ?.optJSONObject("watchEndpoint")
        ?.optString("videoId")?.takeIf { it.isNotBlank() }?.let { return it }
    item.optJSONObject("doubleTapNavigationEndpoint")
        ?.optJSONObject("watchEndpoint")
        ?.optString("videoId")?.takeIf { it.isNotBlank() }?.let { return it }
    return ""
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
                    put("gl", "IN")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            return@withContext Pair(emptyList(), "YouTube status: ${conn.responseCode}")
        }

        val respText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(respText)

        val renderers = mutableListOf<JSONObject>()
        findRenderersRecursive(root, "musicResponsiveListItemRenderer", renderers)

        for (item in renderers) {
            val videoId = extractVideoIdFromRenderer(item)
            if (videoId.isBlank()) continue

            val flexCols = item.optJSONArray("flexColumns") ?: continue
            val col0Runs = flexCols.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")
            val title = sanitizeText(col0Runs?.optJSONObject(0)?.optString("text", "Unknown Track") ?: "Unknown Track")

            val col1Runs = flexCols.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")
            var artist = "Unknown Artist"
            if (col1Runs != null && col1Runs.length() > 0) {
                for (r in 0 until col1Runs.length()) {
                    val t = col1Runs.optJSONObject(r)?.optString("text", "")?.trim() ?: ""
                    if (t.isNotBlank() && t != "•" && t != "Song" && t != "Video" && t != "Single" && t != "EP" && t != "Album") {
                        artist = t
                        break
                    }
                }
            }
            artist = sanitizeText(artist)

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
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        conn.setRequestProperty("Referer", "https://music.youtube.com/")

        val payload = JSONObject().apply {
            put("videoId", videoId)
            put("playlistId", "RDAMVM$videoId")
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", "WEB_REMIX")
                    put("clientVersion", "1.20231204.01.00")
                    put("hl", "en")
                    put("gl", "IN")
                })
            })
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val respText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(respText)

            val renderers = mutableListOf<JSONObject>()
            findRenderersRecursive(root, "playlistPanelVideoRenderer", renderers)

            for (item in renderers) {
                val vId = extractVideoIdFromRenderer(item)
                if (vId.isBlank()) continue

                val title = sanitizeText(item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Unknown Track") ?: "Unknown Track")
                var radioArtist = "Unknown Artist"
                val bylineRuns = item.optJSONObject("longBylineText")?.optJSONArray("runs")
                    ?: item.optJSONObject("shortBylineText")?.optJSONArray("runs")
                if (bylineRuns != null) {
                    for (r in 0 until bylineRuns.length()) {
                        val t = bylineRuns.optJSONObject(r)?.optString("text", "")?.trim() ?: ""
                        if (t.isNotBlank() && t != "•" && t != "Song" && t != "Video") {
                            radioArtist = t
                            break
                        }
                    }
                }
                val duration = item.optJSONObject("lengthText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "") ?: ""

                val thumbArray = item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                val artworkUrl = if (thumbArray != null && thumbArray.length() > 0) {
                    thumbArray.getJSONObject(thumbArray.length() - 1).optString("url", "")
                } else ""

                results.add(
                    FullTrackItem(
                        id = vId,
                        title = title,
                        artist = radioArtist,
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

suspend fun resolveTrackAudioStream(track: FullTrackItem): String = withContext(Dispatchers.IO) {
    val videoId = track.id

    if (videoId.isNotBlank()) {
        try {
            val url = URL("https://www.youtube.com/youtubei/v1/player")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
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
                        put("androidSdkVersion", 30)
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
                    if (bestUrl.isNotBlank()) return@withContext bestUrl
                }
            }
        } catch (_: Exception) {}

        try {
            val url = URL("https://www.youtube.com/youtubei/v1/player")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.0) AppleWebKit/538.1 (KHTML, like Gecko) Version/5.0 TV Safari/538.1")

            val payload = JSONObject().apply {
                put("videoId", videoId)
                put("contentCheckOk", true)
                put("racyCheckOk", true)
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
                        put("clientVersion", "2.0")
                        put("hl", "en")
                    })
                    put("thirdParty", JSONObject().apply {
                        put("embedUrl", "https://www.youtube.com")
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
                    if (bestUrl.isNotBlank()) return@withContext bestUrl
                }
            }
        } catch (_: Exception) {}

        val pipedInstances = listOf(
            "https://pipedapi.adminforge.de",
            "https://pipedapi.tokhmi.xyz",
            "https://api.piped.yt"
        )
        for (base in pipedInstances) {
            try {
                val url = URL("$base/streams/$videoId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(resp)
                    val audioStreams = root.optJSONArray("audioStreams")
                    if (audioStreams != null && audioStreams.length() > 0) {
                        var bestUrl = ""
                        var maxBitrate = 0L
                        for (i in 0 until audioStreams.length()) {
                            val s = audioStreams.getJSONObject(i)
                            val sUrl = s.optString("url", "")
                            val bitrate = s.optLong("bitrate", 0L)
                            if (sUrl.isNotBlank() && bitrate >= maxBitrate) {
                                maxBitrate = bitrate
                                bestUrl = sUrl
                            }
                        }
                        if (bestUrl.isNotBlank()) return@withContext bestUrl
                    }
                }
            } catch (_: Exception) { continue }
        }
    }

    try {
        val queryText = URLEncoder.encode("${track.title} ${track.artist}".trim(), "UTF-8")
        val saavnUrl = URL("https://www.jiosaavn.com/api.php?__call=search.getResults&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=3&p=1&q=$queryText")
        val conn = saavnUrl.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 4000
        conn.readTimeout = 4000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val resp = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(resp)
            val results = root.optJSONArray("results")
            if (results != null && results.length() > 0) {
                val first = results.getJSONObject(0)
                val moreInfo = first.optJSONObject("more_info")
                val encUrl = moreInfo?.optString("encrypted_media_url") ?: first.optString("encrypted_media_url", "")
                if (encUrl.isNotBlank()) {
                    val decrypted = decryptMediaUrl(encUrl)
                    if (decrypted.isNotBlank()) return@withContext decrypted
                }
            }
        }
    } catch (_: Exception) {}

    ""
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
        val isDuplicate = pool.any { existing -> areTracksSimilar(candidate, existing) || candidate.id == existing.id }
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
        val streamUrl = if (track.audioUrl.startsWith("http")) track.audioUrl else resolveTrackAudioStream(track)
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

@Composable
fun SyncedLyricsView(
    providerName: String,
    lyrics: List<SyncedLyricLine>,
    currentPositionMs: Long,
    isLoading: Boolean,
    onSeekRequested: (Long) -> Unit,
    onCloseRequested: () -> Unit,
    trackTitle: String,
    trackArtist: String,
    artworkUrl: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isManualScrolling by remember { mutableStateOf(false) }

    val activeIndex = remember(currentPositionMs, lyrics) {
        if (lyrics.isEmpty()) -1
        else {
            val idx = lyrics.indexOfLast { currentPositionMs >= it.timeMs }
            if (idx >= 0) idx else 0
        }
    }

    // Detect manual scrolling and release lock after 3.5s of inactivity
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isManualScrolling = true
        } else if (isManualScrolling) {
            delay(3500L)
            isManualScrolling = false
        }
    }

    // Smooth auto-scroll snap when playback progresses
    LaunchedEffect(activeIndex, isManualScrolling) {
        if (!isManualScrolling && activeIndex >= 0 && lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -140 // Centers the line on screen
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back navigation, Provider Name, and Lyric Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseRequested) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (providerName.isNotBlank()) "Lyrics from $providerName" else "Synchronized Lyrics",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )
                    if (isManualScrolling) {
                        Text(
                            text = "Auto-scroll paused",
                            fontSize = 11.sp,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (lyrics.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No synchronized lyrics found for this track",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(top = 180.dp, bottom = 220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    itemsIndexed(lyrics, key = { index, line -> "${line.timeMs}_$index" }) { index, item ->
                        val isActive = index == activeIndex

                        val animatedAlpha by animateFloatAsState(
                            targetValue = if (isActive) 1f else 0.35f,
                            animationSpec = tween(300),
                            label = "lyricAlpha"
                        )

                        val animatedScale by animateFloatAsState(
                            targetValue = if (isActive) 1.05f else 0.96f,
                            animationSpec = tween(300),
                            label = "lyricScale"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSeekRequested(item.timeMs)
                                    isManualScrolling = false
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.text,
                                fontSize = if (isActive) 26.sp else 20.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White.copy(alpha = animatedAlpha),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isManualScrolling) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatTime(item.timeMs),
                                    fontSize = 11.sp,
                                    color = if (isActive) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.45f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Track Info Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = artworkUrl,
                        contentDescription = trackTitle,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = trackArtist,
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Floating "Sync to Audio" Button when user scrolls away
        AnimatedVisibility(
            visible = isManualScrolling && activeIndex >= 0,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7C4DFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.clickable {
                    isManualScrolling = false
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = activeIndex,
                            scrollOffset = -140
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Sync",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sync to Audio",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeablePlaylistTrackRow(
    track: FullTrackItem,
    index: Int,
    totalCount: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = tween(180),
        label = "playlistRowSwipe"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Red background with Trash Icon revealed during left-swipe
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFDC2626))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Foreground Content Card with Drag and Move Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragOffsetX = (dragOffsetX + delta).coerceIn(-180f, 0f)
                    },
                    onDragStopped = { velocity ->
                        if (dragOffsetX < -90f || velocity < -400f) {
                            onDelete()
                        }
                        dragOffsetX = 0f
                    }
                )
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF141C24) else Color(0xFFF1F5F9)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reorder controls (Move Up / Move Down)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (index > 0) (if (isDarkTheme) Color.White else Color(0xFF0F172A)) else Color(0x3094A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) (if (isDarkTheme) Color.White else Color(0xFF0F172A)) else Color(0x3094A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove",
                        tint = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SonoraPlayerScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val keyboardController = LocalSoftwareKeyboardController.current

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
    var showLiveLyrics by remember { mutableStateOf(false) }
    var activeLyricsProvider by remember { mutableStateOf("") }
    var activeSyncedLyrics by remember { mutableStateOf<List<SyncedLyricLine>>(emptyList()) }
    var isLyricsLoading by remember { mutableStateOf(false) }

    var showEqualizerSheet by remember { mutableStateOf(false) }

    var selectedTrackForOptions by remember { mutableStateOf<FullTrackItem?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var queueList by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var endlessRadioEnabled by remember { mutableStateOf(true) }
    var showQueueDialog by remember { mutableStateOf(false) }

    var isShuffleActive by remember { mutableStateOf(false) }
    var isRepeatActive by remember { mutableStateOf(false) }
    var repeatModeState by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var sleepTimerActiveMinutes by remember { mutableIntStateOf(0) }
    var sleepTimerSecondsRemaining by remember { mutableLongStateOf(0L) }

    var downloadingSongIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedMoodCategory by remember { mutableStateOf(DiscoveryCategoryList[0]) }
    var moodTracks by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isMoodLoading by remember { mutableStateOf(false) }

    var mostPlayedTracks by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var recentlyPlayedTracks by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }

    fun refreshListeningStats() {
        mostPlayedTracks = getMostPlayedTracks(context)
        recentlyPlayedTracks = getRecentlyPlayedTracks(context)
    }

    LaunchedEffect(Unit) {
        refreshListeningStats()
    }

    var activeSongId by remember { mutableStateOf("") }
    var activeTitle by remember { mutableStateOf("No Track Playing") }
    var activeArtist by remember { mutableStateOf("Select a song to start listening") }
    var activeArtworkUrl by remember { mutableStateOf("") }
    var activeAudioUrl by remember { mutableStateOf("") }
    var activeDurationFormatted by remember { mutableStateOf("0:00") }

    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(sleepTimerActiveMinutes, sleepTimerSecondsRemaining) {
        if (sleepTimerActiveMinutes > 0 && sleepTimerSecondsRemaining > 0) {
            delay(1000L)
            sleepTimerSecondsRemaining -= 1
            if (sleepTimerSecondsRemaining == 0L) {
                controller?.pause()
                sleepTimerActiveMinutes = 0
            }
        }
    }

    LaunchedEffect(activeSongId, activeTitle, activeArtist) {
        if (activeSongId.isNotBlank()) {
            isLyricsLoading = true
            val durationSec = (totalDuration / 1000).toInt()
            val (provider, parsedLines) = fetchLyricsFromPriorityProviders(
                songTitle = activeTitle,
                artistName = activeArtist,
                durationSeconds = durationSec
            )
            activeLyricsProvider = provider
            activeSyncedLyrics = parsedLines
            isLyricsLoading = false
        }
    }

    var rawDominantColor by remember { mutableStateOf(Color(0xFF1E2836)) }
    var rawSecondaryColor by remember { mutableStateOf(Color(0xFF0D1520)) }

    LaunchedEffect(activeArtworkUrl) {
        if (activeArtworkUrl.isNotBlank()) {
            val (dom, sec) = extractArtworkPaletteColors(context, activeArtworkUrl)
            rawDominantColor = dom
            rawSecondaryColor = sec
        } else {
            rawDominantColor = Color(0xFF1E2836)
            rawSecondaryColor = Color(0xFF0D1520)
        }
    }

    val animatedDominantColor by animateColorAsState(
        targetValue = rawDominantColor,
        animationSpec = tween(650),
        label = "domColorAnim"
    )
    val animatedSecondaryColor by animateColorAsState(
        targetValue = rawSecondaryColor,
        animationSpec = tween(650),
        label = "secColorAnim"
    )

    val isCurrentSongLiked by dao.isSongLiked(activeSongId).collectAsState(initial = false)
    val isCurrentSongDownloaded by dao.isSongDownloaded(activeSongId).collectAsState(initial = false)

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var stopAfterCurrentTrack by remember { mutableStateOf(false) }

    var songToAddToPlaylist by remember { mutableStateOf<FullTrackItem?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val maxSysVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolumeSlider by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }

    DisposableEffect(audioManager) {
        val runnable = Runnable {
            currentVolumeSlider = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        }
        val thread = Thread {
            var lastVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            while (true) {
                Thread.sleep(300)
                val currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currVol != lastVol) {
                    lastVol = currVol
                    android.os.Handler(context.mainLooper).post(runnable)
                }
            }
        }
        thread.start()
        onDispose {
            thread.interrupt()
        }
    }

    BackHandler(enabled = true) {
        when {
            showLiveLyrics -> {
                showLiveLyrics = false
            }
            isPlayerExpanded -> {
                isPlayerExpanded = false
            }
            viewingPlaylist != null -> {
                viewingPlaylist = null
            }
            selectedNavTab != 0 -> {
                selectedNavTab = 0
            }
            else -> {
                (context as? ComponentActivity)?.moveTaskToBack(true)
            }
        }
    }

    LaunchedEffect(selectedMoodCategory) {
        isMoodLoading = true
        val (tracks, _) = searchYouTubeMusic(selectedMoodCategory.searchQuery)
        moodTracks = tracks
        isMoodLoading = false
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerActiveMinutes = minutes
        sleepTimerSecondsRemaining = minutes * 60L
    }

    fun updateQueueState(player: Player) {
        try {
            val count = player.mediaItemCount
            val items = ArrayList<FullTrackItem>(count)
            for (i in 0 until count) {
                if (i < player.mediaItemCount) {
                    items.add(mediaItemToTrack(player.getMediaItemAt(i)))
                }
            }
            queueList = items
            currentTrackIndex = player.currentMediaItemIndex.coerceAtLeast(0)
        } catch (_: Exception) {}
    }

    var isAutoQueueFilling by remember { mutableStateOf(false) }

    fun ensureInfiniteQueueFilled(player: Player) {
        if (!endlessRadioEnabled || isAutoQueueFilling) return
        val total = player.mediaItemCount
        val curr = player.currentMediaItemIndex
        val remaining = total - (curr + 1)

        if ((remaining <= 5 || curr >= total - 1) && total > 0) {
            isAutoQueueFilling = true
            coroutineScope.launch {
                try {
                    val lastIdx = total - 1
                    val seedItem = if (lastIdx >= 0) player.getMediaItemAt(lastIdx) else player.currentMediaItem
                    val seedId = seedItem?.mediaId ?: activeSongId
                    val seedArtist = seedItem?.mediaMetadata?.artist?.toString() ?: activeArtist

                    val candidates = mutableListOf<FullTrackItem>()
                    candidates.addAll(fetchYouTubeAutomixRadio(seedId))
                    if (candidates.size < 25) {
                        val (moreArtist, _) = searchYouTubeMusic("$seedArtist latest hits")
                        candidates.addAll(moreArtist)
                    }
                    if (candidates.size < 25) {
                        val (trending, _) = searchYouTubeMusic("Trending Hindi Bollywood Songs")
                        candidates.addAll(trending)
                    }

                    val currentQueueTracks = (0 until player.mediaItemCount).map { idx ->
                        mediaItemToTrack(player.getMediaItemAt(idx))
                    }
                    val filtered = filterSimilarTracks(candidates, currentQueueTracks)

                    var added = 0
                    for (song in filtered) {
                        if (added >= 15) break
                        val streamUrl = resolveTrackAudioStream(song)
                        if (streamUrl.isNotBlank()) {
                            song.audioUrl = streamUrl
                            player.addMediaItem(buildMediaItem(song))
                            added++
                        }
                    }
                    updateQueueState(player)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isAutoQueueFilling = false
                }
            }
        }
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

    fun togglePlayPause() {
        controller?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                val savedPos = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
                if (player.currentPosition < 1000L && savedPos > 1000L) {
                    player.seekTo(savedPos)
                    currentPosition = savedPos
                }
                player.play()
            }
        }
    }

    DisposableEffect(context, lifecycleOwner) {
        val activity = context as? Activity
        var disposed = false

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (activity?.isFinishing == true && activity.isChangingConfigurations.not()) {
                        controller?.release()
                        controller = null
                        val killIntent = Intent("com.example.music.ACTION_KILL_SERVICE").apply {
                            setPackage(context.packageName)
                        }
                        try {
                            context.sendBroadcast(killIntent)
                        } catch (_: Exception) {}
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    if (activity?.isChangingConfigurations != true) {
                        controller?.release()
                        controller = null
                        val killIntent = Intent("com.example.music.ACTION_KILL_SERVICE").apply {
                            setPackage(context.packageName)
                        }
                        try {
                            context.sendBroadcast(killIntent)
                        } catch (_: Exception) {}
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                val mediaController = controllerFuture.get()
                if (disposed || (activity?.isFinishing == true && activity.isChangingConfigurations.not())) {
                    try {
                        mediaController.release()
                    } catch (_: Exception) {}
                    return@addListener
                }

                controller = mediaController
                isPlaying = mediaController.isPlaying

                val savedId = prefs.getString(KEY_LAST_ID, "") ?: ""
                val savedPosMs = prefs.getLong(KEY_LAST_POSITION_MS, 0L)

                if (mediaController.mediaItemCount > 0) {
                    val currentItem = mediaController.currentMediaItem
                    activeSongId = currentItem?.mediaId ?: ""
                    activeTitle = currentItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                    activeArtist = currentItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                    activeArtworkUrl = currentItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                    currentPosition = max(0L, mediaController.currentPosition)
                    totalDuration = if (mediaController.duration > 0) mediaController.duration else 0L
                    updateQueueState(mediaController)
                } else if (savedId.isNotBlank()) {
                    val savedTitle = prefs.getString(KEY_LAST_TITLE, "Last Played Track") ?: ""
                    val savedArtist = prefs.getString(KEY_LAST_ARTIST, "Tap play to resume") ?: ""
                    val savedArtworkUrl = prefs.getString(KEY_LAST_ARTWORK_URL, "") ?: ""
                    val savedDurationTxt = prefs.getString(KEY_LAST_DURATION_TXT, "0:00") ?: ""
                    val savedDurMs = prefs.getLong(KEY_LAST_DURATION_MS, 0L)

                    activeSongId = savedId
                    activeTitle = savedTitle
                    activeArtist = savedArtist
                    activeArtworkUrl = savedArtworkUrl
                    activeDurationFormatted = savedDurationTxt
                    currentPosition = savedPosMs
                    totalDuration = savedDurMs

                    coroutineScope.launch {
                        if (disposed) return@launch
                        val restoredTrack = FullTrackItem(savedId, savedTitle, savedArtist, "", savedArtworkUrl, savedDurationTxt)
                        val streamUrl = resolveTrackAudioStream(restoredTrack)
                        if (streamUrl.isNotBlank() && !disposed) {
                            activeAudioUrl = streamUrl
                            restoredTrack.audioUrl = streamUrl
                            mediaController.setMediaItem(buildMediaItem(restoredTrack), savedPosMs)
                            mediaController.prepare()
                            mediaController.pause()
                            updateQueueState(mediaController)
                        }
                    }
                }

                mediaController.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        if (disposed) return
                        isPlaying = playing
                        if (!playing && mediaController.currentPosition > 0) {
                            prefs.edit().putLong(KEY_LAST_POSITION_MS, mediaController.currentPosition).apply()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (disposed) return
                        if (playbackState == Player.STATE_READY) {
                            val dur = mediaController.duration
                            if (dur > 0) {
                                totalDuration = dur
                                prefs.edit().putLong(KEY_LAST_DURATION_MS, dur).apply()
                            }
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            if (stopAfterCurrentTrack) {
                                mediaController.pause()
                            } else if (endlessRadioEnabled) {
                                ensureInfiniteQueueFilled(mediaController)
                                mediaController.play()
                            }
                        }
                    }

                    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                        if (disposed) return
                        updateQueueState(mediaController)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (disposed) return
                        if (stopAfterCurrentTrack && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                            mediaController.pause()
                        }

                        val newId = mediaItem?.mediaId ?: ""
                        activeSongId = newId
                        activeTitle = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                        activeArtist = mediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                        activeArtworkUrl = mediaItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                        activeAudioUrl = mediaItem?.requestMetadata?.mediaUri?.toString()
                            ?: mediaItem?.localConfiguration?.uri?.toString() ?: ""

                        if (newId != savedId) {
                            currentPosition = 0L
                            prefs.edit().putLong(KEY_LAST_POSITION_MS, 0L).apply()
                        } else {
                            currentPosition = savedPosMs
                        }

                        updateQueueState(mediaController)

                        if (activeSongId.isNotBlank()) {
                            recordTrackPlay(
                                context,
                                FullTrackItem(
                                    activeSongId,
                                    activeTitle,
                                    activeArtist,
                                    activeAudioUrl,
                                    activeArtworkUrl,
                                    activeDurationFormatted
                                )
                            )
                            refreshListeningStats()
                        }

                        prefs.edit()
                            .putString(KEY_LAST_ID, activeSongId)
                            .putString(KEY_LAST_TITLE, activeTitle)
                            .putString(KEY_LAST_ARTIST, activeArtist)
                            .putString(KEY_LAST_AUDIO_URL, activeAudioUrl)
                            .putString(KEY_LAST_ARTWORK_URL, activeArtworkUrl)
                            .apply()

                        ensureInfiniteQueueFilled(mediaController)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            try {
                controller?.release()
            } catch (_: Exception) {}
            controller = null
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
            val effectiveUrl = if (downloadedLocal != null && File(downloadedLocal.localFilePath).exists()) {
                downloadedLocal.localFilePath
            } else {
                resolveTrackAudioStream(targetTrack)
            }

            if (effectiveUrl.isBlank()) {
                Toast.makeText(context, "Unable to resolve stream for ${targetTrack.title}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            targetTrack.audioUrl = effectiveUrl
            activeSongId = targetTrack.id
            activeTitle = targetTrack.title
            activeArtist = targetTrack.artist
            activeArtworkUrl = targetTrack.artworkUrl
            activeAudioUrl = effectiveUrl
            activeDurationFormatted = targetTrack.durationFormatted

            recordTrackPlay(context, targetTrack)
            refreshListeningStats()

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
                coroutineScope.launch {
                    val candidates = mutableListOf<FullTrackItem>()
                    candidates.addAll(fetchYouTubeAutomixRadio(targetTrack.id))

                    if (candidates.size < 35) {
                        val (artistSongs, _) = searchYouTubeMusic("${targetTrack.artist} song")
                        candidates.addAll(artistSongs)
                    }
                    if (candidates.size < 35) {
                        val (hindiSongs, _) = searchYouTubeMusic("Trending Hindi Bollywood Songs")
                        candidates.addAll(hindiSongs)
                    }

                    val currentQueueTracks = (0 until (controller?.mediaItemCount ?: 0)).map { idx ->
                        mediaItemToTrack(controller!!.getMediaItemAt(idx))
                    }
                    val filtered = filterSimilarTracks(candidates, currentQueueTracks)

                    var added = 0
                    for (song in filtered) {
                        if ((controller?.mediaItemCount ?: 0) >= 31) break
                        val sUrl = resolveTrackAudioStream(song)
                        if (sUrl.isNotBlank()) {
                            song.audioUrl = sUrl
                            controller?.addMediaItem(buildMediaItem(song))
                            added++
                        }
                    }
                    controller?.let { updateQueueState(it) }
                }
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
                                stopAfterCurrentTrack = true
                                showSleepTimerDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2631)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("End of Current Track", color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(14.dp))
                    }
                    if (sleepTimerActiveMinutes > 0 || stopAfterCurrentTrack) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    sleepTimerActiveMinutes = 0
                                    sleepTimerSecondsRemaining = 0L
                                    stopAfterCurrentTrack = false
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
                var localQueue by remember(queueList) { mutableStateOf(queueList) }
                LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    itemsIndexed(localQueue, key = { index, track -> "${track.id}_$index" }) { index, track ->
                        val isCurrent = index == currentTrackIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFF263242) else Color(0xFF141C24)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        controller?.let { player ->
                                            if (index in 0 until player.mediaItemCount) {
                                                player.seekToDefaultPosition(index)
                                                player.play()
                                            }
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    IconButton(
                                        onClick = {
                                            controller?.let { player ->
                                                if (index > 0 && index < player.mediaItemCount) {
                                                    val mutable = localQueue.toMutableList()
                                                    val item = mutable.removeAt(index)
                                                    mutable.add(index - 1, item)
                                                    localQueue = mutable
                                                    player.moveMediaItem(index, index - 1)
                                                    updateQueueState(player)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(imageVector = Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            controller?.let { player ->
                                                if (index < localQueue.size - 1 && index + 1 < player.mediaItemCount) {
                                                    val mutable = localQueue.toMutableList()
                                                    val item = mutable.removeAt(index)
                                                    mutable.add(index + 1, item)
                                                    localQueue = mutable
                                                    player.moveMediaItem(index, index + 1)
                                                    updateQueueState(player)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(imageVector = Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))
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
                                IconButton(
                                    onClick = {
                                        controller?.let { player ->
                                            if (index in 0 until player.mediaItemCount) {
                                                val mutable = localQueue.toMutableList()
                                                mutable.removeAt(index)
                                                localQueue = mutable
                                                player.removeMediaItem(index)
                                                updateQueueState(player)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedNavTab) {
                    0 -> {
                        val allSpeedDialSongs = remember(mostPlayedTracks, moodTracks) {
                            val combined = mutableListOf<FullTrackItem>()
                            combined.addAll(mostPlayedTracks)
                            for (t in moodTracks) {
                                if (combined.none { it.id == t.id }) {
                                    combined.add(t)
                                }
                                if (combined.size >= 36) break
                            }
                            combined
                        }

                        val keepListeningSongs = remember(recentlyPlayedTracks, moodTracks) {
                            if (recentlyPlayedTracks.isNotEmpty()) {
                                recentlyPlayedTracks
                            } else {
                                moodTracks.drop(4)
                            }
                        }

                        val speedDialPagerState = rememberPagerState(pageCount = { 4 })

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
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showSleepTimerDialog = true }) {
                                        Icon(imageVector = Icons.Rounded.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(onClick = { showQueueDialog = true }) {
                                        Icon(imageVector = Icons.Rounded.TrendingUp, contentDescription = "Queue", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(imageVector = Icons.Rounded.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(onClick = onToggleTheme) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lightbulb,
                                            contentDescription = "Toggle Theme",
                                            tint = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFD97706),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(DiscoveryCategoryList) { cat ->
                                    val isSelected = selectedMoodCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary 
                                                else if (isDarkTheme) Color(0xFF1A232E) 
                                                else Color(0xFFE2E8F0)
                                            )
                                            .clickable { selectedMoodCategory = cat }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = cat.label,
                                            color = if (isSelected) Color.White 
                                                    else if (isDarkTheme) Color(0xFFCBD5E1) 
                                                    else Color(0xFF1E293B),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = "Speed dial",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }

                                item {
                                    if (isMoodLoading && allSpeedDialSongs.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(260.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else {
                                        HorizontalPager(
                                            state = speedDialPagerState,
                                            modifier = Modifier.fillMaxWidth()
                                        ) { pageIdx ->
                                            val pageSongs = allSpeedDialSongs.drop(pageIdx * 9).take(9)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                for (row in 0 until 3) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        for (col in 0 until 3) {
                                                            val idx = row * 3 + col
                                                            if (idx < pageSongs.size) {
                                                                val song = pageSongs[idx]
                                                                Column(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable { playQueue(pageSongs, idx) }
                                                                ) {
                                                                    AsyncImage(
                                                                        model = song.artworkUrl,
                                                                        contentDescription = song.title,
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .aspectRatio(1f)
                                                                            .clip(RoundedCornerShape(8.dp)),
                                                                        contentScale = ContentScale.Crop
                                                                    )
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text(
                                                                        text = song.title,
                                                                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                                                        fontSize = 13.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                            } else {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (p in 0 until 4) {
                                            val isCurrent = speedDialPagerState.currentPage == p
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isCurrent) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCurrent) MaterialTheme.colorScheme.primary 
                                                        else if (isDarkTheme) Color(0xFF334155) 
                                                        else Color(0xFFCBD5E1)
                                                    )
                                            )
                                            if (p < 3) Spacer(modifier = Modifier.width(6.dp))
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "Keep listening",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                                    )
                                }

                                item {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 84.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        itemsIndexed(keepListeningSongs) { idx, song ->
                                            Column(
                                                modifier = Modifier
                                                    .width(136.dp)
                                                    .clickable { playQueue(keepListeningSongs, idx) }
                                            ) {
                                                AsyncImage(
                                                    model = song.artworkUrl,
                                                    contentDescription = song.title,
                                                    modifier = Modifier
                                                        .size(136.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = song.title,
                                                    color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = song.artist,
                                                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
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
                                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
                                    }

                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Search YouTube music...", color = Color(0xFF6B7280)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(24.dp),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Search
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                if (searchQuery.isNotBlank()) {
                                                    keyboardController?.hide()
                                                    executeSearch(searchQuery)
                                                }
                                            }
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        trailingIcon = {
                                            if (searchQuery.isNotBlank()) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Clear",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp).clickable { searchQuery = "" }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(onClick = {
                                        if (searchQuery.isNotBlank()) {
                                            keyboardController?.hide()
                                            executeSearch(searchQuery)
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
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
                                                    Text(song.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                                                    Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
                                                }
                                                RefinedDownloadMark(
                                                    isDownloaded = isDownloaded,
                                                    isDownloading = isDownloading,
                                                    onClick = { triggerDownload(song) }
                                                )
                                                IconButton(onClick = { selectedTrackForOptions = song }) {
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
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
                                                Icon(imageVector = Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = historyItem.query,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    fontSize = 16.sp,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Remove",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                color = MaterialTheme.colorScheme.onBackground,
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
                                            containerColor = if (isSubSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.clickable { selectedLibrarySubTab = idx }
                                    ) {
                                        Text(label, color = if (isSubSel) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
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
                                                    Text(savedSong.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                                                    Text(savedSong.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
                                                }
                                                RefinedDownloadMark(
                                                    isDownloaded = isDownloaded,
                                                    isDownloading = isDownloading,
                                                    onClick = { triggerDownload(savedSong) }
                                                )
                                                IconButton(onClick = { selectedTrackForOptions = savedSong }) {
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(84.dp)) }
                                    }
                                }
                                1 -> {
                                    if (viewingPlaylist != null) {
                                        val playlist = viewingPlaylist!!
                                        val playlistTracks = activePlaylistSongs.map {
                                            FullTrackItem(it.songId, it.title, it.artist, it.audioUrl, it.artworkUrl, it.duration)
                                        }
                                        var localPlaylistTracks by remember(playlistTracks) { mutableStateOf(playlistTracks) }

                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Top navigation row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                IconButton(onClick = { viewingPlaylist = null }) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ArrowBack,
                                                        contentDescription = "Back",
                                                        tint = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                                    )
                                                }
                                                Text(
                                                    text = playlist.name,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            dao.deletePlaylistSongs(playlist.id)
                                                            dao.deletePlaylist(playlist.id)
                                                            viewingPlaylist = null
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete Playlist",
                                                        tint = Color(0xFFFF5252),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Hero Section: 4-Artwork Mosaic Cover & Action Buttons
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                PlaylistMosaicCover(
                                                    artworkUrls = localPlaylistTracks.map { it.artworkUrl },
                                                    modifier = Modifier.size(110.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    fallbackIconSize = 42.dp
                                                )

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = playlist.name,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "${localPlaylistTracks.size} songs",
                                                        fontSize = 13.sp,
                                                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                                                    )

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                if (localPlaylistTracks.isNotEmpty()) {
                                                                    playQueue(localPlaylistTracks, 0)
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                            shape = RoundedCornerShape(20.dp),
                                                            modifier = Modifier.height(36.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                if (localPlaylistTracks.isNotEmpty()) {
                                                                    playQueue(localPlaylistTracks.shuffled(), 0)
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFE2E8F0),
                                                                contentColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                                            ),
                                                            shape = RoundedCornerShape(20.dp),
                                                            modifier = Modifier.height(36.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Rounded.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }

                                            // Reorderable & Swipeable Track List
                                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                itemsIndexed(localPlaylistTracks, key = { _, track -> track.id }) { idx, track ->
                                                    SwipeablePlaylistTrackRow(
                                                        track = track,
                                                        index = idx,
                                                        totalCount = localPlaylistTracks.size,
                                                        isDarkTheme = isDarkTheme,
                                                        onClick = { playQueue(localPlaylistTracks, idx) },
                                                        onMoveUp = {
                                                            if (idx > 0) {
                                                                val mutable = localPlaylistTracks.toMutableList()
                                                                val item = mutable.removeAt(idx)
                                                                mutable.add(idx - 1, item)
                                                                localPlaylistTracks = mutable
                                                            }
                                                        },
                                                        onMoveDown = {
                                                            if (idx < localPlaylistTracks.size - 1) {
                                                                val mutable = localPlaylistTracks.toMutableList()
                                                                val item = mutable.removeAt(idx)
                                                                mutable.add(idx + 1, item)
                                                                localPlaylistTracks = mutable
                                                            }
                                                        },
                                                        onDelete = {
                                                            coroutineScope.launch {
                                                                dao.removeSongFromPlaylist(playlist.id, track.id)
                                                                localPlaylistTracks = localPlaylistTracks.filter { it.id != track.id }
                                                            }
                                                        }
                                                    )
                                                }
                                                item { Spacer(modifier = Modifier.height(90.dp)) }
                                            }
                                        }
                                    } else {
                                        // Playlists overview list with 4-Artwork Mosaic Covers
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            item {
                                                Button(
                                                    onClick = { showCreatePlaylistDialog = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp)
                                                ) {
                                                    Text("+ Create New Playlist", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            items(allPlaylists) { playlist ->
                                                val songsForThisPlaylist by dao.getSongsForPlaylist(playlist.id).collectAsState(initial = emptyList())

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .clickable { viewingPlaylist = playlist },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isDarkTheme) Color(0xFF141C24) else Color(0xFFF1F5F9)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        PlaylistMosaicCover(
                                                            artworkUrls = songsForThisPlaylist.map { it.artworkUrl },
                                                            modifier = Modifier.size(56.dp),
                                                            shape = RoundedCornerShape(10.dp),
                                                            fallbackIconSize = 24.dp
                                                        )

                                                        Spacer(modifier = Modifier.width(14.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = playlist.name,
                                                                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${songsForThisPlaylist.size} tracks",
                                                                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            item { Spacer(modifier = Modifier.height(90.dp)) }
                                        }
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
                                                    Text(song.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                                                    Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
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
                                                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
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

            if (activeSongId.isNotBlank()) {
                val progressFraction = if (totalDuration > 0) (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f

                // Horizontal swipe drag offset with bouncy spring animation
                var miniplayerDragOffsetX by remember { mutableFloatStateOf(0f) }
                val animatedMiniplayerOffsetX by animateFloatAsState(
                    targetValue = miniplayerDragOffsetX,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "miniplayerSwipeOffset"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .offset { IntOffset(animatedMiniplayerOffsetX.roundToInt(), 0) }
                        // Horizontal swipe: Left for Next, Right for Previous
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                miniplayerDragOffsetX = (miniplayerDragOffsetX + delta * 0.65f).coerceIn(-280f, 280f)
                            },
                            onDragStopped = { velocity ->
                                val threshold = 110f
                                if (miniplayerDragOffsetX < -threshold || velocity < -450f) {
                                    // Swipe Left -> Skip to Next track
                                    controller?.let { player ->
                                        if (player.mediaItemCount > 0 && player.hasNextMediaItem()) {
                                            player.seekToNextMediaItem()
                                        }
                                    }
                                } else if (miniplayerDragOffsetX > threshold || velocity > 450f) {
                                    // Swipe Right -> Skip to Previous track
                                    controller?.let { player ->
                                        if (player.currentPosition > 3000L) {
                                            player.seekTo(0L)
                                        } else if (player.hasPreviousMediaItem()) {
                                            player.seekToPreviousMediaItem()
                                        }
                                    }
                                }
                                miniplayerDragOffsetX = 0f
                            }
                        )
                        // Vertical swipe: Drag up to expand
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                if (delta < -20f) {
                                    isPlayerExpanded = true
                                }
                            }
                        )
                        // Tap to expand
                        .clickable { isPlayerExpanded = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        animatedDominantColor.copy(alpha = 0.85f),
                                        animatedSecondaryColor.copy(alpha = 0.92f)
                                    )
                                )
                            )
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
                                    .clickable { togglePlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White,
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

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isPlayerExpanded = true }
                            ) {
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
                                    color = Color(0xFFD1D5DB),
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
            }

            // Bottom Navigation Bar with top border and high-contrast theme styling
            Surface(
                color = if (isDarkTheme) Color(0xFF080C10) else Color(0xFFFFFFFF),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Subtle top divider line for clear separation from screen background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDarkTheme) Color(0xFF1E2836) else Color(0xFFE2E8F0))
                    )

                    NavigationBar(
                        containerColor = if (isDarkTheme) Color(0xFF080C10) else Color(0xFFFFFFFF),
                        contentColor = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedItemColor = if (isDarkTheme) Color(0xFFB388FF) else Color(0xFF6200EE)
                        val unselectedItemColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        val indicatorBgColor = if (isDarkTheme) Color(0xFF263242) else Color(0xFFEDE7F6)

                        NavigationBarItem(
                            selected = selectedNavTab == 0,
                            onClick = { selectedNavTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = "Home",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    "Home",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedItemColor,
                                selectedTextColor = selectedItemColor,
                                unselectedIconColor = unselectedItemColor,
                                unselectedTextColor = unselectedItemColor,
                                indicatorColor = indicatorBgColor
                            )
                        )
                        NavigationBarItem(
                            selected = selectedNavTab == 1,
                            onClick = { selectedNavTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    "Search",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedItemColor,
                                selectedTextColor = selectedItemColor,
                                unselectedIconColor = unselectedItemColor,
                                unselectedTextColor = unselectedItemColor,
                                indicatorColor = indicatorBgColor
                            )
                        )
                        NavigationBarItem(
                            selected = selectedNavTab == 2,
                            onClick = { selectedNavTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.LibraryMusic,
                                    contentDescription = "Library",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    "Library",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = selectedItemColor,
                                selectedTextColor = selectedItemColor,
                                unselectedIconColor = unselectedItemColor,
                                unselectedTextColor = unselectedItemColor,
                                indicatorColor = indicatorBgColor
                            )
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                animatedDominantColor.copy(alpha = 0.95f),
                                animatedSecondaryColor.copy(alpha = 0.90f),
                                Color(0xFF070B10)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 60f) {
                                isPlayerExpanded = false
                                showLiveLyrics = false
                            }
                        }
                    }
            ) {
                // Synced Live Karaoke Lyrics View
                AnimatedVisibility(
                    visible = showLiveLyrics,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    modifier = Modifier.fillMaxSize()
                ) {
                    SyncedLyricsView(
                        providerName = activeLyricsProvider,
                        lyrics = activeSyncedLyrics,
                        currentPositionMs = currentPosition,
                        isLoading = isLyricsLoading,
                        onSeekRequested = { targetMs ->
                            controller?.seekTo(targetMs)
                            currentPosition = targetMs
                        },
                        onCloseRequested = { showLiveLyrics = false },
                        trackTitle = activeTitle,
                        trackArtist = activeArtist,
                        artworkUrl = activeArtworkUrl
                    )
                }

                // Standard Player View
                AnimatedVisibility(
                    visible = !showLiveLyrics,
                    enter = slideInHorizontally(initialOffsetX = { -it }),
                    exit = slideOutHorizontally(targetOffsetX = { -it }),
                    modifier = Modifier.fillMaxSize()
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
                                    color = Color(0xFFD1D5DB),
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
                            IconButton(onClick = { showLiveLyrics = !showLiveLyrics }) {
                                Icon(imageVector = Icons.Rounded.Notes, contentDescription = "Live Lyrics", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
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
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        DynamicAudioVisualizer(
                            isPlaying = isPlaying,
                            primaryColor = animatedDominantColor,
                            secondaryColor = animatedSecondaryColor,
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .padding(horizontal = 8.dp),
                            barCount = 30,
                            visualizerHeight = 36.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
                                        color = Color(0xFFCBD5E1),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0x40FFFFFF))
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
                                            .background(Color(0x40FFFFFF))
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
                                    inactiveTrackColor = Color(0x40FFFFFF)
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
                                    color = Color(0xFFD1D5DB)
                                )
                                Text(
                                    text = formatTime(totalDuration),
                                    fontSize = 12.sp,
                                    color = Color(0xFFD1D5DB)
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
                                    .background(Color(0x50FFFFFF))
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
                                Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(28.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color.White)
                                    .clickable { togglePlayPause() }
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
                                    .background(Color(0x50FFFFFF))
                                    .clickable {
                                        controller?.let { player ->
                                            if (player.mediaItemCount > 0 && player.hasNextMediaItem()) {
                                                player.seekToNextMediaItem()
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }

                        // Bottom Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x30FFFFFF))
                                    .clickable { showQueueDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = Color.White, modifier = Modifier.size(20.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (sleepTimerActiveMinutes > 0) Color(0xFF7C4DFF) else Color(0x30FFFFFF))
                                    .clickable { showSleepTimerDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Rounded.Bedtime, contentDescription = "Timer", tint = Color.White, modifier = Modifier.size(16.dp))
                                    if (sleepTimerActiveMinutes > 0 && sleepTimerSecondsRemaining > 0) {
                                        val mins = sleepTimerSecondsRemaining / 60
                                        val secs = sleepTimerSecondsRemaining % 60
                                        Text(
                                            text = String.format(Locale.ROOT, "%d:%02d", mins, secs),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isShuffleActive) Color(0xFF7C4DFF) else Color(0x30FFFFFF))
                                    .clickable {
                                        isShuffleActive = !isShuffleActive
                                        controller?.shuffleModeEnabled = isShuffleActive
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Equalizer Button (opens EqualizerSheet)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (EqualizerManager.isEnabled) Color(0xFF7C4DFF) else Color(0x30FFFFFF))
                                    .clickable { showEqualizerSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Rounded.Tune, contentDescription = "Equalizer", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRepeatActive) Color(0xFF7C4DFF) else Color(0x30FFFFFF))
                                    .clickable {
                                        isRepeatActive = !isRepeatActive
                                        repeatModeState = if (isRepeatActive) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
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
                                    .background(Color.White)
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

    if (showEqualizerSheet) {
        EqualizerSheet(
            isDarkTheme = isDarkTheme,
            onDismiss = { showEqualizerSheet = false }
        )
    }
}