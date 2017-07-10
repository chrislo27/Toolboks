package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input


private val inputMap: MutableMap<Int, Boolean> = mutableMapOf()
private val buttonMap: MutableMap<Int, Boolean> = mutableMapOf()

fun Input.isKeyJustReleased(key: Int): Boolean {
    if (inputMap[key] == null)
        inputMap[key] = false

    val old = inputMap[key]
    val state = Gdx.input.isKeyPressed(key)

    inputMap[key] = state

    return !state && old == true
}

fun Input.isButtonJustReleased(button: Int): Boolean {
    if (buttonMap[button] == null)
        buttonMap[button] = false

    val old = buttonMap[button]
    val state = Gdx.input.isButtonPressed(button)

    inputMap[button] = state

    return state && old == false
}

fun Input.isButtonJustPressed(button: Int): Boolean {
    if (buttonMap[button] == null)
        buttonMap[button] = false

    val old = buttonMap[button]
    val state = Gdx.input.isButtonPressed(button)

    inputMap[button] = state

    return !state && old == true
}
