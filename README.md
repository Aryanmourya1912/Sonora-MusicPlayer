# 🎵 Sonora

### A personal Android music player built around the way I actually listen to music.

Sonora is an Android music player built with **Kotlin and Jetpack Compose**. It brings online music search, streaming, downloads, playlists, lyrics, queue management, and local playback into one app.

The project started as a personal experiment and has gradually grown into a full music player with its own playback system, search, library, settings, and Android media controls.

> **Note:** Sonora is still under active development. Some parts of the app are being improved and changed as development continues.

---

## ✨ Features

### 🎧 Playback

* ▶️ Stream music directly from the internet
* 📥 Play downloaded songs
* 🔊 Background playback
* ⏯️ Play, pause, seek, previous and next
* 🎚️ Progress control
* 📋 Up Next queue
* 🔀 Queue management
* 🎵 Android media playback integration
* 🔒 Playback continues while navigating through the app

---

### 🔎 Search & Discovery

Find music without leaving the app.

* Search YouTube Music
* Search suggestions while typing
* Extended search results using continuation pages
* Song title, artist, artwork and duration
* Search history
* Artist-based discovery
* Trending and recommended music
* Duplicate-result filtering

Search results can be added directly to the playback queue.

---

### 📥 Downloads

Save music for listening later.

* Download songs
* Download status indicators
* Local song storage
* Play downloaded songs without streaming again
* Access downloaded music from the library

---

### 📋 Up Next

The queue is designed to stay out of the way while still giving you control over what plays next.

You can:

* View upcoming songs
* See the currently playing track
* Remove songs
* Change the order
* Add songs from search
* Continue listening without rebuilding the queue

Long song titles are allowed to wrap onto multiple lines instead of being unnecessarily cut off.

---

### 🎤 Lyrics

Lyrics are available directly from the player when they can be found.

* Search for lyrics for the current song
* Display lyrics inside the player
* Match lyrics with the current track
* Support synced lyrics when available

---

### 💿 Library

Keep frequently used music together in one place.

The library includes access to:

* Downloaded songs
* Saved music
* Playlists
* Search history
* Recently used music

---

### 🎶 Playlists

Organize songs into playlists instead of keeping everything in one long list.

Songs can be added to playlists and managed from within the app.

---

### 🎛️ Audio & Playback Settings

Sonora also handles several Android audio events automatically.

#### Bluetooth & Headphones

When an audio device disconnects while music is playing:

**Connected → Music playing → Device disconnected → Music pauses**

If **Auto Resume on Reconnect** is enabled:

**Device reconnects → Music resumes**

If the option is disabled, the music remains paused.

#### Phone Calls

Incoming calls can pause music temporarily, with playback able to continue after the call ends.

---

### ⏱️ Sleep Timer

Set a timer when you don't want music playing indefinitely.

Useful for listening before sleeping without having to manually stop playback.

---

## 🎨 Interface

Sonora uses **Jetpack Compose** and **Material 3** for its interface.

The app currently includes:

* 🌙 Dark theme
* ☀️ Light theme
* 🏠 Home
* 🔎 Search
* 📚 Library
* 🎵 Mini player
* 🎧 Full player
* 📋 Queue
* ⚙️ Settings
* 🎤 Lyrics
* 👤 Artist and song views

The interface is still evolving as new parts of the app are added and existing screens are refined.

---

## 🛠️ Built With

| Technology            | Used for                         |
| --------------------- | -------------------------------- |
| **Kotlin**            | Main programming language        |
| **Jetpack Compose**   | User interface                   |
| **Material 3**        | UI components and design         |
| **Android Media3**    | Media playback                   |
| **ExoPlayer**         | Audio playback                   |
| **MediaSession**      | Android media controls           |
| **Kotlin Coroutines** | Background and asynchronous work |
| **JSON**              | Handling music-service responses |

---

## 🧩 How It Works

At a basic level, Sonora is split into a few main parts:

```text
                ┌──────────────────┐
                │      Sonora      │
                └────────┬─────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
      🔎 Search       🎵 Player       📚 Library
          │              │              │
          ▼              ▼              ▼
    Music Search     Media3 /       Downloads &
      Results        ExoPlayer       Playlists
                         │
                         ▼
                  📱 Android
                  MediaSession
```

Search finds the music, the player handles playback, and the library keeps downloaded and organized content accessible.

---

## 📱 Android Integration

Sonora isn't just a screen that plays audio.

It also connects with Android's media system to handle things such as:

* Background playback
* Media controls
* Audio-device changes
* Bluetooth/headset events
* Phone-call interruptions
* Playback state
* Queue management

This allows music to behave more like a normal Android media application rather than being tied to the currently visible screen.

---

## 🔍 Search Pagination

One of the parts of Sonora that has been specifically developed around the way YouTube Music responds to searches is search pagination.

Instead of stopping at the first batch of results, Sonora can use continuation information returned by the service to request additional result pages.

```text
Search
  │
  ▼
First result page
  │
  ▼
Continuation token
  │
  ▼
Next result page
  │
  ▼
More results
  │
  ▼
Duplicate filtering
  │
  ▼
Search screen
```

This allows the search screen to provide a larger set of results without simply duplicating the first page.

---

## 🧪 Project Status

**Sonora is currently a work in progress.**

The main parts of the app are already being developed and tested, but the project isn't considered finished yet.

Some areas are still receiving changes, especially:

* Search
* Playback
* Queue handling
* Downloads
* Lyrics
* UI
* Settings
* Android audio-device behavior

So if something occasionally behaves strangely, there's a good chance it is already on the list of things being worked on.

---

## 💡 Why Sonora?

The idea behind Sonora is fairly simple.

I wanted a music player that had the features I actually wanted to use without making the whole experience unnecessarily complicated.

Building it myself also gave me a reason to learn more about:

* Android development
* Kotlin
* Jetpack Compose
* Media3
* ExoPlayer
* Networking
* Background services
* Android audio handling
* Local storage
* Music-player architecture

What started as a music-player experiment has gradually turned into a much larger Android project.

---

## ⚠️ Important

Sonora uses YouTube Music-related services for online music search and streaming.

Because these services are not a stable public API designed specifically for third-party applications, changes on the service side can affect parts of the app.

Online features also require an internet connection.

---

## 🙌 Credits

Sonora is built using open-source technologies and libraries from the Android and Kotlin ecosystem.

A big thank-you goes to the developers and contributors behind the projects that make Android development, media playback, networking, and modern UI development possible.

---

<div align="center">

### 🎵 Sonora

**Music, the way I wanted it.**

Built with Kotlin • Jetpack Compose • Media3

</div>
