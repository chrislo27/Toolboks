package io.github.chrislo27.toolboks

import com.badlogic.gdx.Screen


public abstract class ToolboksScreen<out G : ToolboksGame>(public val main: G) : Screen {

    abstract fun renderUpdate()

    abstract fun tickUpdate()

    fun getDebugStrings(list: MutableList<String>) {

    }

}