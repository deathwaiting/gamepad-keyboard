package io.github.deathwaiting.gamepadkeyboard

import io.github.deathwaiting.gamepadkeyboard.GamePadKey.*
import io.smallrye.mutiny.Multi
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan



class GamePadToKeyboardMapper(val keyMapping:GamePadKeyMapping, val controller:GamePadController ) {
    val pressedKeys: Multi<Map<GamePadKey, Boolean>> = controller.inputs.map(::toLogicalKeyPress)
    val keyStrokes: Multi<String> = pressedKeys.map(::mapToKeyboardPress)
                                        .filter(String::isNotBlank)

    private fun mapToKeyboardPress(gamePadKeys: Map<GamePadKey, Boolean>):String {
        val pressed = gamePadKeys.filter { gamePadKey -> gamePadKey.value }.keys
        return keyMapping.mapping[pressed] ?: ""
    }

    private fun toLogicalKeyPress(inputs:Map<GamePadInput,Float>): Map<GamePadKey,Boolean> {
        val pressedKeys = ConcurrentHashMap<GamePadKey,Boolean>()
        inputs.mapNotNull(::processButtonInput).forEach{ keyPress -> pressedKeys[keyPress.key] = keyPress.pressed }
        processAnalogSticksInput(inputs, pressedKeys)
        return pressedKeys
    }

    private fun processAnalogSticksInput(inputs: Map<GamePadInput, Float>, pressedKeys: MutableMap<GamePadKey, Boolean>) {

        if( (inputs[GamePadInput.L_STICK_X]?: 0.0f) < 0.2f  && (inputs[GamePadInput.L_STICK_Y]?: 0.0f) < 0.2f) {
            GamePadKey.entries.filter { it.name.startsWith("L_STICK_") && it != L_STICK_PRESS }
                .forEach{ pressedKeys[it] = false}
        } else {
            val lStickAngle = getStickAngle(inputs[GamePadInput.L_STICK_X], inputs[GamePadInput.L_STICK_Y])
            pressedKeys[L_STICK_DOWN] = lStickAngle in 256..284
            pressedKeys[L_STICK_UP] = lStickAngle in 75..105
            pressedKeys[L_STICK_RIGHT] = lStickAngle in 345..360 || lStickAngle in 0..15
            pressedKeys[L_STICK_LEFT] = lStickAngle in 165..195
            pressedKeys[L_STICK_DOWN_LEFT] = lStickAngle in 300..330
            pressedKeys[L_STICK_DOWN_RIGHT] = lStickAngle in 210..240
            pressedKeys[L_STICK_UP_RIGHT] = lStickAngle in 30..60
            pressedKeys[L_STICK_UP_LEFT] = lStickAngle in 120..150
        }

        if( (inputs[GamePadInput.R_STICK_X]?: 0.2f) < 0.1f  && (inputs[GamePadInput.R_STICK_Y]?: 0.0f) < 0.2f) {
            GamePadKey.entries.filter { it.name.startsWith("R_STICK_") && it != R_STICK_PRESS }
                .forEach { pressedKeys[it] = false }
        } else {
            val rStickAngle = getStickAngle(inputs[GamePadInput.R_STICK_X], inputs[GamePadInput.R_STICK_Y])
            pressedKeys[R_STICK_DOWN] = rStickAngle in 256..284
            pressedKeys[R_STICK_UP] = rStickAngle in 75..105
            pressedKeys[R_STICK_RIGHT] = rStickAngle in 345..360 || rStickAngle in 0..15
            pressedKeys[R_STICK_LEFT] = rStickAngle in 165..195
            pressedKeys[R_STICK_DOWN_LEFT] = rStickAngle in 300..330
            pressedKeys[R_STICK_DOWN_RIGHT] = rStickAngle in 210..240
            pressedKeys[R_STICK_UP_RIGHT] = rStickAngle in 30..60
            pressedKeys[R_STICK_UP_LEFT] = rStickAngle in 120..150
        }
    }


    private fun getStickAngle(origXValue: Float?, origYValue: Float?) : Int {
        val cos = (origXValue ?: 0.0f).toDouble()
        val sine:Double  = (origYValue ?: 0.0f).toDouble()
        return ((atan(sine/cos) * (180 / Math.PI))).toInt()
    }

    private fun processButtonInput(input: Map.Entry<GamePadInput, Float>): KeyPress? {
        return when(input.key) {
            GamePadInput.L_TRIGGER, GamePadInput.R_TRIGGER -> KeyPress(GamePadKey.valueOf(input.key.name), input.value > 0.1)
            GamePadInput.L_STICK_X, GamePadInput.L_STICK_Y, GamePadInput.R_STICK_X, GamePadInput.R_STICK_Y -> null
            else -> KeyPress(GamePadKey.valueOf(input.key.name), input.value > 0.5)
        }
    }

    private data class KeyPress(val key: GamePadKey, val pressed: Boolean)
}