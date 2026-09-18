package com.example.music

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
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
import kotlin.math.max

data class SongItem(
    val title: String,
    val artist: String,
    val previewUrl: String,
    val artworkUrl: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerScreen()
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

// Searches iTunes API and grabs high-res album art
suspend fun searchItunesSongs(query: String): List<SongItem> = withContext(Dispatchers.IO) {
    val resultsList = mutableListOf<SongItem>()
    try {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val endpoint = "https://itunes.apple.com/search?term=$encodedQuery&entity=song&limit=25"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000

        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(responseText)
        val jsonArray = jsonObject.optJSONArray("results") ?: return@withContext emptyList()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.optString("trackName", "")
            val artist = item.optString("artistName", "")
            val preview = item.optString("previewUrl", "")
            val artwork = item.optString("artworkUrl100", "").replace("100x100bb", "500x500bb")

            if (name.isNotBlank() && preview.isNotBlank()) {
                resultsList.add(
                    SongItem(
                        title = name,
                        artist = artist,
                        previewUrl = preview,
                        artworkUrl = artwork
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext resultsList
}

@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var activeTitle by remember { mutableStateOf("No Song Selected") }
    var activeArtist by remember { mutableStateOf("Search and tap a song above to play") }
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
        // Search Input Bar
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
                        coroutineScope.launch {
                            searchResults = searchItunesSongs(searchQuery)
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

        // Results List with Album Art Thumbnails
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Type an artist or song name and press Search",
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
                                            .setUri(Uri.parse(song.previewUrl))
                                            .setMediaMetadata(metadata)
                                            .build()

                                        player.setMediaItem(mediaItem)
                                        player.prepare()
                                        player.play()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
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
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = song.artist,
                                        color = Color.Gray,
                                        fontSize = 13.sp,
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

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Player Bar with Cover Artwork & Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
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
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeArtist,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(totalDuration),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        controller?.let { player ->
                            if (player.isPlaying) player.pause() else player.play()
                        }
                    },
                    enabled = controller != null && controller?.mediaItemCount != 0
                ) {
                    Text(if (isPlaying) "Pause ⏸" else "Play ▶")
                }
            }
        }
    }
}
