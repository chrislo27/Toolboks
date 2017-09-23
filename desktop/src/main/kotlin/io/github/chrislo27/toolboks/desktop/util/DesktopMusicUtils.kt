package io.github.chrislo27.toolboks.desktop.util

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudio
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.utils.FloatArray
import io.github.chrislo27.toolboks.util.gdxutils.MusicUtils
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL10.alSourcePlay
import org.lwjgl.openal.AL10.alSourceQueueBuffers
import org.lwjgl.openal.AL10.alSourceStop
import org.lwjgl.openal.AL10.alSourceUnqueueBuffers
import org.lwjgl.openal.AL10.alSourcef
import org.lwjgl.openal.AL11
import java.nio.ByteBuffer
import java.nio.IntBuffer


class DesktopMusicUtils : MusicUtils() {

    companion object {
        private val bufferSize by lazy {
            val field = OpenALMusic::class.java.getDeclaredField("bufferSize")
            field.isAccessible = true
            field.getInt(null)
        }
    }

    override fun setPositionNonBlocking(music: Music, seconds: Float, shouldResetOnEnd: Boolean)
            : DesktopPositionUpdate {
        music as OpenALMusic

        val tempBytes: ByteArray = ByteArray(bufferSize)
        val tempBuffer: ByteBuffer = BufferUtils.createByteBuffer(bufferSize)

        /* REFLECTION: START! */
        val audio = run {
            val field = OpenALMusic::class.java.getDeclaredField("audio")
            field.isAccessible = true
            field.get(music) as OpenALAudio
        }
        val noDevice = run {
            val field = OpenALAudio::class.java.getDeclaredField("noDevice")
            field.isAccessible = true
            field.getBoolean(audio)
        }
        if (noDevice) return NoOpDesktopPositionUpdate(music)

        val sourceID = run {
            val field = OpenALMusic::class.java.getDeclaredField("sourceID")
            field.isAccessible = true
            field.getInt(music)
        }
        if (sourceID == -1) return NoOpDesktopPositionUpdate(music)

        val isPlayingField = run {
            val field = OpenALMusic::class.java.getDeclaredField("isPlaying")
            field.isAccessible = true
            field
        }
        fun setIsPlaying(value: Boolean) {
            isPlayingField.setBoolean(music, value)
        }

        val renderedSecondsQueueField = run {
            val field = OpenALMusic::class.java.getDeclaredField("renderedSecondsQueue")
            field.isAccessible = true
            field
        }
        val renderedSecondsField = run {
            val field = OpenALMusic::class.java.getDeclaredField("renderedSeconds")
            field.isAccessible = true
            field
        }
        val maxSecondsPerBuffer = run {
            val field = OpenALMusic::class.java.getDeclaredField("maxSecondsPerBuffer")
            field.isAccessible = true
            field.getFloat(music)
        }
        val bufferCount = run {
            val field = OpenALMusic::class.java.getDeclaredField("bufferCount")
            field.isAccessible = true
            field.getInt(null)
        }
        val renderedSecondsQueue: FloatArray = renderedSecondsQueueField.get(music) as FloatArray

        fun getRenderedSeconds(): Float = renderedSecondsField.getFloat(music)
        fun setRenderedSeconds(value: Float) {
            renderedSecondsField.setFloat(music, value)
        }
        fun addRenderedSeconds(value: Float) {
            renderedSecondsField.setFloat(music, getRenderedSeconds() + value)
        }

        val buffers = run {
            val field = OpenALMusic::class.java.getDeclaredField("buffers")
            field.isAccessible = true
            field.get(music) as IntBuffer
        }

        val loopMethod = run {
            val method = OpenALMusic::class.java.getDeclaredMethod("loop")
            method.isAccessible = true
            method
        }
        fun loop() {
            loopMethod.invoke(music)
        }

        val format = run {
            val field = OpenALMusic::class.java.getDeclaredField("format")
            field.isAccessible = true
            field.getInt(music)
        }
        val sampleRate = run {
            val field = OpenALMusic::class.java.getDeclaredField("sampleRate")
            field.isAccessible = true
            field.getInt(music)
        }

        /* FINISH! */

        fun OpenALMusic.fill(bufferID: Int): Boolean {
            tempBuffer.clear()
            var length = read(tempBytes)
            if (length <= 0) {
                if (isLooping) {
                    loop()
                    length = read(tempBytes)
                    if (length <= 0) return false
                    if (renderedSecondsQueue.size > 0) {
                        renderedSecondsQueue.set(0, 0f)
                    }
                } else
                    return false
            }
            val previousLoadedSeconds = if (renderedSecondsQueue.size > 0) renderedSecondsQueue.first() else 0f
            val currentBufferSeconds = maxSecondsPerBuffer * length.toFloat() / bufferSize.toFloat()
            renderedSecondsQueue.insert(0, previousLoadedSeconds + currentBufferSeconds)

            tempBuffer.put(tempBytes, 0, length).flip()
            AL10.alBufferData(bufferID, format, tempBuffer, sampleRate)
            return true
        }

        return object : DesktopPositionUpdate(music) {
            var first = true
            var done = false
            val wasPlaying = music.isPlaying

            override fun update(delta: Float): Float {
                if (first) {
                    first = false

                    music.apply {
                        setIsPlaying(false)
                        alSourceStop(sourceID)
                        alSourceUnqueueBuffers(sourceID, buffers)
                        while (renderedSecondsQueue.size > 0) {
                            setRenderedSeconds(renderedSecondsQueue.pop())
                        }
                        if (seconds <= getRenderedSeconds()) {
                            reset()
                            setRenderedSeconds(0f)
                        }
                    }
                }

                fun getPercentage(): Float {
                    return music.run {
                        (getRenderedSeconds() / position - maxSecondsPerBuffer).coerceIn(0f, 1f)
                    }
                }

                music.apply {
                    val nanoStart = System.nanoTime()
                    var errors = 0
                    while (getRenderedSeconds() < seconds - maxSecondsPerBuffer) {
                        try {
                            val bytes = read(tempBytes)

                            if (bytes <= 0) {
                                break
                            }

                            addRenderedSeconds(maxSecondsPerBuffer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            reset()
                            errors++

                            if (errors > 10) {
                                throw e
                            }
                        }

                        if ((System.nanoTime() - nanoStart) / 1_000_000f > delta * 1000) {
                            return getPercentage()
                        }
                    }
//                    println("${(System.nanoTime() - nanoStart) / 1000000f}")
                    done = true
                }

                if (done) {
                    renderedSecondsQueue.add(getRenderedSeconds())
                    music.apply {
                        var filled = false
                        for (i in 0..bufferCount - 1) {
                            val bufferID = buffers.get(i)
                            if (!fill(bufferID)) break
                            filled = true
                            alSourceQueueBuffers(sourceID, bufferID)
                        }
                        renderedSecondsQueue.pop()
                        if (!filled) {
                            run {
                                val field = OpenALMusic::class.java.getDeclaredField("onCompletionListener")
                                field.isAccessible = true
                                field.get(music) as Music.OnCompletionListener?
                            }?.onCompletion(music)
                            if (shouldResetOnEnd) {
                                stop()
                            }
                        }
                        alSourcef(sourceID, AL11.AL_SEC_OFFSET, position - getRenderedSeconds())
                        if (wasPlaying && !shouldResetOnEnd) {
                            alSourcePlay(sourceID)
                            setIsPlaying(true)
                        }
                    }
                    return 1f
                }
                return getPercentage()
            }
        }
    }

    abstract class DesktopPositionUpdate(music: OpenALMusic) : PositionUpdate(music)

    class NoOpDesktopPositionUpdate(music: OpenALMusic) : DesktopPositionUpdate(music) {
        override fun update(delta: Float): Float {
            return 1f
        }
    }
}
