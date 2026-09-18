package com.example.music

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.Dispatchers
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

// Decrypts JioSaavn's encrypted CDN media URLs directly on device
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
        // Upgrade preview audio to standard 160kbps stream
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

// Queries JioSaavn's official backend API directly (no middleman / no Vercel quotas)
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

            // Artist resolution
            var artistName = item.optString("subtitle", "")
            if (artistName.isBlank() && moreInfo != null) {
                artistName = moreInfo.optString("music", "")
            }
            if (artistName.isBlank()) {
                artistName = item.optString("primary_artists", "Unknown Artist")
            }
            artistName = sanitizeText(artistName)

            // High-resolution artwork
            var artUrl = item.optString("image", "")
            if (artUrl.isBlank() && moreInfo != null) {
                artUrl = moreInfo.optString("image", "")
            }
            artUrl = artUrl.replace("150x150", "500x500").replace("50x50", "500x500")
            if (artUrl.startsWith("http://")) {
                artUrl = artUrl.replaceFirst("http://", "https://")
            }

            // Audio Stream Decryption
            val encryptedUrl = when {
                moreInfo != null && moreInfo.has("encrypted_media_url") -> moreInfo.optString("encrypted_media_url")
                item.has("encrypted_media_url") -> item.optString("encrypted_media_url")
                else -> ""
            }
            val playableStream = decryptMediaUrl(encryptedUrl)

            // Duration in seconds
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

@Composable
fun SonoraPlayerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FullTrackItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchStatusMessage by remember { mutableStateOf<String?>(null) }

    var activeTitle by remember { mutableStateOf("No Track Playing") }
    var activeArtist by remember { mutableStateOf("Search and tap any song above") }
    var activeArtworkUrl by remember { mutableStateOf("") }

    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableStateOf(0f) }

    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            controller = mediaController
            isPlaying = mediaController.isPlaying

            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val dur = mediaController.duration
                        totalDuration = if (dur > 0) dur else 0L
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    activeTitle = mediaItem?.mediaMetadata?.title?.toString() ?: "Unknown Track"
                    activeArtist = mediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist"
                    activeArtworkUrl = mediaItem?.mediaMetadata?.artworkUri?.toString() ?: ""
                    currentPosition = 0L
                }
            })
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            controller?.release()
        }
    }

    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            controller?.let { player ->
                currentPosition = max(0L, player.currentPosition)
                val dur = player.duration
                if (dur > 0) totalDuration = dur
            }
            delay(500L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sonora",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

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
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        isSearching = true
                        searchStatusMessage = null
                        coroutineScope.launch {
                            val (results, errorMsg) = searchOfficialSongs(searchQuery)
                            searchResults = results
                            searchStatusMessage = errorMsg
                            isSearching = false
                        }
                    }
                },
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = searchStatusMessage ?: "Type a song name and tap Search",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { song ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    controller?.let { player ->
                                        val metadata = MediaMetadata.Builder()
                                            .setTitle(song.title)
                                            .setArtist(song.artist)
                                            .setArtworkUri(Uri.parse(song.artworkUrl))
                                            .build()

                                        val mediaItem = MediaItem.Builder()
                                            .setUri(Uri.parse(song.audioUrl))
                                            .setMediaMetadata(metadata)
                                            .build()

                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
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

        Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.width(8.dp))

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

                val maxDurationFloat = max(1L, totalDuration).toFloat()
                val currentProgressFloat = if (isDraggingSlider) sliderDragValue else currentPosition.toFloat()

                Slider(
                    value = currentProgressFloat.coerceIn(0f, maxDurationFloat),
                    onValueChange = { newPos ->
                        isDraggingSlider = true
                        sliderDragValue = newPos
                    },
                    onValueChangeFinished = {
                        controller?.seekTo(sliderDragValue.toLong())
                        currentPosition = sliderDragValue.toLong()
                        isDraggingSlider = false
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
