package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.audio.Music


abstract class MusicUtils {

    companion object {
        lateinit var instance: MusicUtils
            set
    }

    abstract fun setPositionNonBlocking(music: Music, seconds: Float, shouldResetOnEnd: Boolean): PositionUpdate

    abstract class PositionUpdate(val music: Music) {

        abstract fun update(delta: Float): Float

    }

}
