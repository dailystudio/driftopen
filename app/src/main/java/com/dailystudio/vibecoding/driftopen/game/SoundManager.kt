package com.dailystudio.vibecoding.driftopen.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.dailystudio.vibecoding.driftopen.R

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private var bgPlayer: MediaPlayer? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Preload sounds
        loadSound("shoot", R.raw.shoot)
        loadSound("explosion", R.raw.explosion)
        loadSound("powerup", R.raw.powerup)
        loadSound("hit", R.raw.hit)
    }

    private fun loadSound(name: String, resId: Int) {
        sounds[name] = soundPool.load(context, resId, 1)
    }

    fun playSound(name: String) {
        sounds[name]?.let {
            soundPool.play(it, 0.4f, 0.4f, 0, 0, 1f)
        }
    }

    fun startMusic() {
        if (bgPlayer == null) {
            try {
                bgPlayer = MediaPlayer.create(context, R.raw.bg_music)
                bgPlayer?.isLooping = true
                bgPlayer?.setVolume(0.8f, 0.8f)
                // Explicitly set completion listener to ensure looping
                bgPlayer?.setOnCompletionListener {
                    it.start()
                }
                bgPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (bgPlayer?.isPlaying == false) {
            bgPlayer?.start()
        }
    }

    fun pauseMusic() {
        if (bgPlayer?.isPlaying == true) {
            bgPlayer?.pause()
        }
    }

    fun resumeMusic() {
        if (bgPlayer != null && bgPlayer?.isPlaying == false) {
            bgPlayer?.start()
        }
    }

    fun stopMusic() {
        bgPlayer?.stop()
        bgPlayer?.release()
        bgPlayer = null
    }

    fun release() {
        soundPool.release()
        stopMusic()
    }
}
