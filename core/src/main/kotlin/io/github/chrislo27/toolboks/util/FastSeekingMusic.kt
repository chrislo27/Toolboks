package io.github.chrislo27.toolboks.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.util.gdxutils.MusicUtils
import io.github.chrislo27.toolboks.util.gdxutils.copyHandle
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

/**
 * A utility class that wraps a [com.badlogic.gdx.audio.Music] instance.
 *
 * It uses two Music instances and ping-pongs between them when playing, so the other can seek in a coroutine.
 */
class FastSeekingMusic(val handle: FileHandle)
    : Disposable {

    val completionListener = CompletionListener()
    private val instances: Array<Music>

    init {
        instances = arrayOf(newMusic(), newMusic())
    }

    @Volatile
    private var currentIndex: Int = 0

    private val currentActiveMusic: Music
        get() = instances[currentIndex]

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
    @Volatile private var coroutine: Job? = null

    private fun newMusic(): Music {
        val music = Gdx.audio.newMusic(handle.copyHandle())
        music.setOnCompletionListener(completionListener)
        return music
    }

    private fun _setPosition(seconds: Float) {
        if (seconds < 0)
            throw IllegalArgumentException("Seconds ($seconds) cannot be negative")
        if (!currentActiveMusic.isPlaying)
            return

        runBlocking {
            coroutine?.join()
            coroutine = null
        }

        currentActiveMusic.pause()
        currentIndex = if (currentIndex == 0) 1 else 0
        currentActiveMusic.play()
        currentActiveMusic.volume = 0.5f
        currentActiveMusic.position = seconds

        coroutine = launch(CommonPool) {
            val mus = instances[if (currentIndex == 0) 1 else 0]
            mus.play()
            mus.pause()
            MusicUtils.instance
                    .setPositionNonBlocking(mus, (seconds - 1f).coerceAtLeast(0f), false)
                    .update(10000000f)
            mus.pause()
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
