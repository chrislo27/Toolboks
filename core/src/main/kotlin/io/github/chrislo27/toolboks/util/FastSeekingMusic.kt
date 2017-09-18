package io.github.chrislo27.toolboks.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.util.gdxutils.MusicUtils
import io.github.chrislo27.toolboks.util.gdxutils.copyHandle
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * A utility class that wraps a [com.badlogic.gdx.audio.Music] instance.
 *
 * It uses two Music instances and ping-pongs between them when playing, so the other can seek in a coroutine.
 */
class FastSeekingMusic(val handle: FileHandle)
    : Disposable {

    companion object {

        var musicFactory: (FileHandle) -> Music = Gdx.audio::newMusic

    }

    val completionListener = CompletionListener()
    private val instances: Array<Music>

    init {
        instances = arrayOf(newMusic(), newMusic())
    }

    private val currentIndex = AtomicInteger(0)
    private val currentJob = AtomicReference<Job?>(null)

    private val currentActiveMusic: Music
        get() = instances[currentIndex.get()]

    var position: Float
        get() = currentActiveMusic.position
        set(value) {
            _setPosition(value)
        }
    var volume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            instances.forEach { it.volume = field }
        }
    val isPlaying: Boolean
        get() = currentActiveMusic.isPlaying

    private fun newMusic(): Music {
        val music = musicFactory(handle.copyHandle())
        music.setOnCompletionListener(completionListener)
        return music
    }

    fun update(delta: Float) {
        val job = currentJob.get() ?: return
        val progress = job.update(delta)

        if (progress >= 1f) {
            currentJob.set(null)
        }
    }

    @Synchronized
    private fun _setPosition(seconds: Float) {
        if (seconds < 0)
            throw IllegalArgumentException("Seconds ($seconds) cannot be negative")
        if (!currentActiveMusic.isPlaying)
            return

        val job = currentJob.get()
        if (job != null) {
            while (job.update(1f) < 1f);
        }

        val oldCurrent = currentActiveMusic
        val index = currentIndex.getAndUpdate {
            if (it == 0) 1 else 0
        }
        val newCurrent = currentActiveMusic
        oldCurrent.pause()
        newCurrent.play()
        newCurrent.position = seconds

        currentJob.set(Job(oldCurrent, (seconds - 1f).coerceAtLeast(0f)))

    }

    private inner class Job(val music: Music, val seconds: Float, val shouldReset: Boolean = false) {

        private var started = false
        private lateinit var pos: MusicUtils.PositionUpdate

        fun update(delta: Float): Float {
            if (!started) {
                started = true

                pos = MusicUtils.instance.setPositionNonBlocking(music, seconds, shouldReset)
                music.play()
                music.pause()
            }

            val progress = pos.update(delta)

            if (progress >= 1f) {
                music.pause()
            }

            return progress
        }

    }

    /**
     * Returns true if the music completed.
     */
    fun play(): Boolean {
        completionListener.invoked = false
        currentActiveMusic.play()
        val result = completionListener.invoked
        completionListener.invoked = false
        return result
    }

    fun pause() {
        instances.forEach(Music::pause)
    }

    fun stop() {
        instances.forEach(Music::stop)
    }

    override fun dispose() {
        instances.forEach(Disposable::dispose)
    }

    inner class CompletionListener internal constructor() : Music.OnCompletionListener {

        var duration: Float = -1f

        var invoked = false

        override fun onCompletion(music: Music) {
            invoked = true
        }
    }
}
