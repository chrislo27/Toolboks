package io.github.chrislo27.toolboks.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.util.gdxutils.copyHandle
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.system.measureNanoTime

/**
 * A utility class that wraps a [com.badlogic.gdx.audio.Music] instance.
 *
 * It splits it up based on the provided [granularity] in seconds. This is to allow
 * for faster seeking.
 *
 * By having a music instance for every [X][granularity] seconds, you can seek more quickly.
 * Seeking backwards can be done by asking the music instance behind that point to move up,
 * thus reducing the time to seek compared to starting from the beginning and going up to that point.
 */
class GranularMusic(val handle: FileHandle, val granularity: Float = 30.0f)
    : Disposable {

    val completionListener = CompletionListener()
    private val instances: MutableList<Music> = mutableListOf()

    init {
        instances += newMusic()
        instances += newMusic()
    }

    @Volatile
    private var currentActiveMusic: Music = instances.first()

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
        val music = Gdx.audio.newMusic(handle.copyHandle())
        music.setOnCompletionListener(completionListener)
        return music
    }

    private fun _setPosition(seconds: Float) {
        if (seconds < 0)
            throw IllegalArgumentException("Seconds ($seconds) cannot be negative")
        if (!currentActiveMusic.isPlaying)
            return

        val grainStep: Int = (seconds / granularity).toInt()
//        if (grainStep >= instances.size) {
//            // time to add more instances
//            for (i in instances.size..grainStep) {
//                val new = newMusic()
//
//                instances.add(0, new)
//                println("Added $i")
//            }
//        }

        sortList()

        currentActiveMusic.pause()
        currentActiveMusic = instances.firstOrNull { it.position <= seconds } ?: currentActiveMusic
        // calculate closest music
        val oldPos = currentActiveMusic.position
        currentActiveMusic.play()
        currentActiveMusic.volume = 0.5f
        currentActiveMusic.position = seconds

        if (instances.size >= 2) {
            launch(CommonPool) {
                val maybe = instances.lastOrNull { it != currentActiveMusic }
                if (maybe != null) {
                    maybe.play()
                    maybe.pause()
                    maybe.position = (seconds - 1f).coerceAtLeast(0f)
                    maybe.volume = 0f
                    println(maybe.volume)
                    println("after position")
                    maybe.pause()
                }
            }
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

    private fun sortList() {
        if (instances.size <= 1)
            return
        instances.sortByDescending { it.position }
    }

    private fun reseek() {
        val coroutines = instances.mapIndexed { index, music ->
            launch(CommonPool) {
                if (!music.isPlaying)
                    music.play()
                val newPos = index * granularity
                val oldPos = music.position
                val nano = measureNanoTime {
                    music.position = newPos
                }
                println("Took ${nano / 1_000_000f} ms to reseek number $index from $oldPos to ${music.position}")
                music.pause() // IMPORTANT - do not use stop as this defeats the purpose
                if (!MathUtils.isEqual(music.position, newPos, 0.05f)) {
                    // TODO out of duration?
                }
            }
        }
        runBlocking {
            coroutines.forEach { it.join() }
        }
        println("New shuffled AND reseeked list: ${instances.map{it.position}}")
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
