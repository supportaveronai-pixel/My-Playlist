package id.myplaylist.app

import android.app.Application
import id.myplaylist.app.data.SettingsStore
import id.myplaylist.app.player.PlayerRepository

class MyPlaylistApp : Application() {
    lateinit var settingsStore: SettingsStore
        private set

    lateinit var playerRepository: PlayerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
        playerRepository = PlayerRepository(this, settingsStore)
    }

    override fun onTerminate() {
        playerRepository.release()
        super.onTerminate()
    }
}
