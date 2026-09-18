package com.example.music.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores songs marked as favorite
@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val artworkUrl: String,
    val duration: String,
    val addedAt: Long = System.currentTimeMillis()
)

// Stores user search queries for quick auto-fill
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Stores user-created playlist headers
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Stores tracks mapped to specific playlists
@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val songId: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val artworkUrl: String,
    val duration: String
)