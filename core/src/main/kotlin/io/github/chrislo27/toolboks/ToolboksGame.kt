package io.github.chrislo27.toolboks

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.version.Version

public abstract class ToolboksGame(val version: Version, val fixedSize: Pair<Float, Float>? = null) : Game() {

    val defaultCamera: OrthographicCamera = OrthographicCamera()

    override fun create() {
        resetCamera()
    }

    override fun render() {
        defaultCamera.update()
        super.render()
    }

    fun resetCamera() {
        if (fixedSize != null) {
            defaultCamera.setToOrtho(false, fixedSize.first, fixedSize.second)
        } else {
            defaultCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
    }

    override fun dispose() {
        super.dispose()
        ScreenRegistry.dispose()
        AssetRegistry.dispose()
    }
}
