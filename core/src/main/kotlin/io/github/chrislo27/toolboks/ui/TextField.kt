package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask


open class TextField<S : ToolboksScreen<*, *>>(override var palette: UIPalette, parent: UIElement<S>,
                                               parameterStage: Stage<S>)
    : UIElement<S>(parent, parameterStage), Palettable, Backgrounded {

    companion object {
        const val BACKSPACE: Char = 8.toChar()
        const val ENTER_DESKTOP = '\r'
        const val ENTER_ANDROID = '\n'
        const val TAB = '\t'
        const val DELETE: Char = 127.toChar()
        const val BULLET: Char = 149.toChar()
        const val CARET_BLINK_RATE: Float = 0.5f
        const val CARET_WIDTH: Float = 2f
    }

    override var background: Boolean = false
    var text: String = ""
        set(value) {
            val old = field
            field = value
            if (old != value) {
                layout.setText(getFont(), text)
                calculateTextPositions()
            }

            caret = caret.coerceIn(0, text.length)
        }
    private val textPositions: List<Float> = mutableListOf()
    private val layout = GlyphLayout()
    private var xOffset: Float = 0f
    var textAlign: Int = Align.left
    /**
     * 0 = start, the number is the index and then behind that
     */
    var caret: Int = 0
        set(value) {
            caretTimer = 0f
            field = value.coerceIn(0, text.length)

            if (layout.width > location.realWidth) {
                val caretX: Float = textPositions[caret]
                if (caretX < xOffset) {
                    xOffset = Math.max(0f, caretX)
                } else if (caretX > xOffset + location.realWidth) {
                    xOffset = Math.min(layout.width - location.realWidth, caretX - location.realWidth + CARET_WIDTH)
                }
            } else {
                xOffset = 0f
            }
        }
    var hasFocus: Boolean = false
        set(value) {
            field = value
            caretTimer = 0f
        }
    var canTypeText: (Char) -> Boolean = {
        true
    }
    private var caretTimer: Float = 0f

    open fun getFont(): BitmapFont =
            palette.font

    protected fun calculateTextPositions() {
        textPositions as MutableList
        textPositions.clear()

        val advances = layout.runs.firstOrNull()?.xAdvances ?: run {
            textPositions as MutableList
            textPositions.addAll(arrayOf(0f, 0f))
            return
        }
        for (i in 0 until advances.size) {
            if (i == 0) {
                textPositions += advances[i]
            } else {
                textPositions += advances[i] + textPositions[i - 1]
            }
        }

    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        if (background) {
            val old = batch.packedColor
            batch.color = palette.backColor
            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
            batch.setColor(old)
        }

        caretTimer += Gdx.graphics.deltaTime

        val labelWidth = location.realWidth
        val labelHeight = location.realHeight

        val textHeight = getFont().getTextHeight(text, labelWidth, false)

        val y: Float
        y = location.realY + location.realHeight / 2 + textHeight / 2

        val oldColor = getFont().color
        val oldScale = getFont().scaleX
        getFont().color = palette.textColor
        getFont().data.setScale(palette.fontScale)

        shapeRenderer.prepareStencilMask(batch) {
            this.projectionMatrix = batch.projectionMatrix
            this.setColor(1f, 1f, 1f, 1f)
            this.begin(ShapeRenderer.ShapeType.Filled)
            this.rect(location.realX, location.realY, location.realWidth, location.realHeight)
            this.end()
        }.useStencilMask {
            val layout: GlyphLayout = getFont().draw(batch, text, location.realX - xOffset, y, labelWidth, textAlign,
                                                     false)

            val caretBlink: Boolean = hasFocus && (caretTimer % (CARET_BLINK_RATE * 2)) <= 0.5f
            if (caretBlink) {
                val oldColor = batch.packedColor
                batch.color = getFont().color

                batch.fillRect(location.realX - xOffset + textPositions[caret], y, CARET_WIDTH,
                               -getFont().capHeight)

                batch.setColor(oldColor)
            }
        }

        getFont().color = oldColor
        getFont().data.setScale(oldScale)
    }

    override fun keyTyped(character: Char): Boolean {
        if (!hasFocus)
            return false

        if (!canTypeText(character))
            return false

        when (character) {
            TAB -> return false
            BACKSPACE -> {
                if (text.isNotEmpty() && caret > 0) {
                    text = text.substring(0, caret - 1) + text.substring(caret)
                    caret--
                    return true
                } else {
                    return false
                }
            }
            ENTER_ANDROID, ENTER_DESKTOP -> {
//                if (multiline) {
//                    text += "\n"
//                    return true
//                } else {
//                    return false
//                }
                return false
            }
            else -> {
                if (character < 32.toChar()) return false

                text = text.substring(0, caret) + character + text.substring(caret)
                caret++

                return true
            }
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (super.touchDown(screenX, screenY, pointer, button))
            return true

        if (hasFocus && !isMouseOver()) {
            hasFocus = false
            return false
        }

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        if (!hasFocus)
            return false
        when (keycode) {
            Input.Keys.LEFT -> {
                caret--
                return true
            }
            Input.Keys.RIGHT -> {
                caret++
                return true
            }
        }

        return false
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)

        if (isMouseOver())
            hasFocus = true

        // copied from Kotlin stdlib with modifications
        fun <T> List<T>.binarySearch(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
            var low = fromIndex
            var high = toIndex - 1

            while (low <= high) {
                val mid = (low + high).ushr(1) // safe from overflows
                val midVal = get(mid)
                val cmp = comparison(midVal)

                if (cmp < 0)
                    low = mid + 1
                else if (cmp > 0)
                    high = mid - 1
                else
                    return mid // key found
            }
            return low // key not found
        }

        val px = (xPercent * location.realWidth + xOffset)
        caret = textPositions.sorted().binarySearch {
            it.compareTo(px)
        }
    }
}