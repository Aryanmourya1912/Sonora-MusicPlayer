override fun onTaskRemoved(rootIntent: Intent?) {
    val player = mediaSession?.player
    player?.pause()
    player?.stop()
    stopSelf()
    super.onTaskRemoved(rootIntent)
}