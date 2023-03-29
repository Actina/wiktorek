package pl.gov.mf.etoll.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import pl.gov.mf.etoll.interfaces.SoundNotificationController
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import javax.inject.Inject


class SoundNotificationsControllerImpl @Inject constructor(
    private val context: Context,
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase
) : SoundNotificationController {

    override fun onEventTriggered() {
        if (readSettingsUseCase.executeForBoolean(Settings.SOUND_NOTIFICATIONS)) {
            // play notification
            val defaultRingtoneUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val mediaPlayer = MediaPlayer()

            try {
                mediaPlayer.setDataSource(context, defaultRingtoneUri)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                        .build()
                )
                mediaPlayer.prepare()
                mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}