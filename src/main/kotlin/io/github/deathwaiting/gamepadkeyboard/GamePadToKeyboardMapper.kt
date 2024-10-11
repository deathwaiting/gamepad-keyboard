package io.github.deathwaiting.gamepadkeyboard

import io.github.deathwaiting.gamepadkeyboard.GamePadKey.*
import io.smallrye.mutiny.Multi
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.atan



class GamePadToKeyboardMapper(val keyMapping:GamePadKeyMapping, val controller:GamePadController ) {
    private val logger = KotlinLogging.logger{}
    private val pressCoolDown = 700L/controller.configs.samplingPeriod
    private var lastPressedCombination:AtomicReference<KeyPressCounter> = AtomicReference(KeyPressCounter.NONE)
    private val logicalKeys: Multi<Map<GamePadKey, Boolean>> =
            controller.inputs
                .cache()
                .map(::toLogicalKeyPress)

    val keyStrokes: Multi<String> =
        logicalKeys
        .map(::getPressedKeys)
        .map { keys ->
            val counter = lastPressedCombination.updateAndGet{updateKeyPressCounter(it, keys)}
            Pair(counter, keys)
        }
        .filter { (keyCounter, pressed) ->
            //apply a cooldown to pressed buttons, so, they start to repeat after 1 second of pressing the same combination
            val pressCount = keyCounter.count
            val printStroke = (pressCount in 0..pressCoolDown/2 || pressCount >= pressCoolDown)
            if(printStroke) {
                logger.debug { "Pressed Key(s) $pressed to be printed with cooldown counter: $pressCount" }
            }
            printStroke
        }
        .map{(_, pressed) -> pressed}
        .map { pressed -> keyMapping.mapping[pressed] ?: "" }
        .filter(String::isNotBlank)

    init {
        logicalKeys.map(::getPressedKeys).subscribe().with { keys -> if(keys.isNotEmpty()) logger.debug { "Pressed Keys: ${keys.sorted()}" } }
        keyStrokes.subscribe().with {stroke -> logger.debug { "Writing Letter: $stroke"}}
    }


    private fun updateKeyPressCounter(counter:KeyPressCounter, pressed:Set<GamePadKey> ):KeyPressCounter {
        return if(pressed.isEmpty()) {
            KeyPressCounter.NONE
        }
        else if(counter.keys == pressed) {
            counter.increment()
        } else {
            KeyPressCounter(pressed, 1)
        }
    }

    private fun getPressedKeys(gamePadKeys: Map<GamePadKey, Boolean>):Set<GamePadKey> {
        return gamePadKeys.filterValues { isPressed -> isPressed }.keys
    }

    private fun toLogicalKeyPress(inputs:Map<GamePadInput,Float>): Map<GamePadKey,Boolean> {
        val pressedKeys = ConcurrentHashMap<GamePadKey,Boolean>()
        inputs.mapNotNull(::processButtonInput).forEach{ keyPress -> pressedKeys[keyPress.key] = keyPress.pressed }
        processAnalogSticksInput(inputs, pressedKeys)

        return pressedKeys
    }

    private fun processAnalogSticksInput(inputs: Map<GamePadInput, Float>, pressedKeys: MutableMap<GamePadKey, Boolean>) {

        if( abs(inputs[GamePadInput.L_STICK_X]?: 0.0f) < 0.2f  && abs(inputs[GamePadInput.L_STICK_Y]?: 0.0f) < 0.2f) {
            GamePadKey.entries.filter { it.name.startsWith("L_STICK_") && it != L_STICK_PRESS }
                .forEach{ pressedKeys[it] = false}
        } else {
            val lStickAngle = getStickAngle(inputs[GamePadInput.L_STICK_X], inputs[GamePadInput.L_STICK_Y])
            pressedKeys[L_STICK_RIGHT] = lStickAngle in 0..22 || lStickAngle in 338..360
            pressedKeys[L_STICK_UP_RIGHT] = lStickAngle in 23..67
            pressedKeys[L_STICK_UP] = lStickAngle in 68..112
            pressedKeys[L_STICK_UP_LEFT] = lStickAngle in 113..157
            pressedKeys[L_STICK_LEFT] = lStickAngle in 158..202
            pressedKeys[L_STICK_DOWN_LEFT] = lStickAngle in 203..247
            pressedKeys[L_STICK_DOWN] = lStickAngle in 248..292
            pressedKeys[L_STICK_DOWN_RIGHT] = lStickAngle in 293..337
        }

        if( abs(inputs[GamePadInput.R_STICK_X]?: 0.0f) < 0.1f  && abs(inputs[GamePadInput.R_STICK_Y]?: 0.0f) < 0.2f) {
            GamePadKey.entries.filter { it.name.startsWith("R_STICK_") && it != R_STICK_PRESS }
                .forEach { pressedKeys[it] = false }
        } else {
            val rStickAngle = getStickAngle(inputs[GamePadInput.R_STICK_X], inputs[GamePadInput.R_STICK_Y])
            pressedKeys[R_STICK_RIGHT] = rStickAngle in 0..22 || rStickAngle in 338..360
            pressedKeys[R_STICK_UP_RIGHT] = rStickAngle in 23..67
            pressedKeys[R_STICK_UP] = rStickAngle in 68..112
            pressedKeys[R_STICK_UP_LEFT] = rStickAngle in 113..157
            pressedKeys[R_STICK_LEFT] = rStickAngle in 158..202
            pressedKeys[R_STICK_DOWN_LEFT] = rStickAngle in 203..247
            pressedKeys[R_STICK_DOWN] = rStickAngle in 248..292
            pressedKeys[R_STICK_DOWN_RIGHT] = rStickAngle in 293..337
        }
    }


    private fun getStickAngle(origXValue: Float?, origYValue: Float?) : Int {
        val cos = (origXValue ?: 0.0f).toDouble()
        val sine:Double  = (origYValue ?: 0.0f).toDouble() * -1 //y-axis is inverted
        return getAngle(sine, cos)
    }

    private fun processButtonInput(input: Map.Entry<GamePadInput, Float>): KeyPress? {
        return when(input.key) {
            GamePadInput.L_TRIGGER, GamePadInput.R_TRIGGER -> KeyPress(GamePadKey.valueOf(input.key.name), input.value > 0.1)
            GamePadInput.L_STICK_X, GamePadInput.L_STICK_Y, GamePadInput.R_STICK_X, GamePadInput.R_STICK_Y -> null
            else -> KeyPress(GamePadKey.valueOf(input.key.name), input.value > 0.5)
        }
    }

    fun getAngle(sinVal: Double, cosVal: Double): Int {
        if (sinVal == 0.0 && cosVal == 0.0) {
            return 0 // Both sine and cosine are zero, so the angle is 0°
        }
        val angleRad = if (sinVal >= 0 && cosVal >= 0) {
            atan(sinVal / cosVal)
        } else if (sinVal >= 0 && cosVal <= 0) {
            Math.PI - atan(sinVal / abs(cosVal))
        } else if (sinVal < 0 && cosVal <= 0) {
            Math.PI + atan(abs(sinVal) / abs(cosVal))
        } else {
            2 * Math.PI - atan(abs(sinVal) / cosVal)
        }

        return Math.toDegrees(angleRad).toInt()
    }


    private data class KeyPress(val key: GamePadKey, val pressed: Boolean)


    data class KeyPressCounter(val keys:Set<GamePadKey>, val count:Int = 0) {
        companion object {
            val NONE = KeyPressCounter(emptySet(), 0)
        }

        fun increment(): KeyPressCounter {
            return KeyPressCounter(keys, count +1)
        }

    }
}