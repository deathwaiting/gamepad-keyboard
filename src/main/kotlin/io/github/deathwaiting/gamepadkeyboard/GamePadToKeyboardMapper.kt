package io.github.deathwaiting.gamepadkeyboard

import io.github.deathwaiting.gamepadkeyboard.GamePadKeys.*
import mu.KotlinLogging
import net.java.games.input.Component
import net.java.games.input.Controller
import net.java.games.input.Event
import net.java.games.input.EventQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.function.Consumer
import kotlin.math.atan

private val DEFAULT_CONTROLLER_MAPPING = mapOf(
    "rx" to R_STICK_X, "ry" to R_STICK_Y,
    "x" to R_STICK_X, "y" to R_STICK_Y,
    "A" to A, "B" to B, "X" to X, "Y" to Y, "Left Thumb" to L_BUTTON, "Right Thumb" to R_BUTTON, "lz" to L_TRIGGER, "rz" to R_TRIGGER
)

data class MapperConfigs(val samplingPeriod: Long = 100,
                         val inputComponentMapping : Map<String,GamePadKeys> = DEFAULT_CONTROLLER_MAPPING)

class GamePadToKeyboardMapper(val keyMapping:GamePadKeyMapping, val controller:Controller, val configs: MapperConfigs = MapperConfigs() ) {
    private val logger = KotlinLogging.logger {}
    private val inputs = HashMap<GamePadKeys,Float>()
    private val inputChangeListeners : MutableList<Consumer<Map<GamePadKeys, Boolean>>> = ArrayList()
    private val keyStrokeListeners : MutableList<Consumer<String>> = ArrayList()

    private val pressedKeys = ConcurrentHashMap<GamePadKeys,Boolean>()


    init {
        logger.info { ">> Using controller : ${controller.name} , input sampling rate: ${configs.samplingPeriod} ms" }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(::pollControllerInput, 0, configs.samplingPeriod, MILLISECONDS)
        inputChangeListeners.add{keys ->
            val pressedKeys = keys.filter {entry ->  entry.value}.keys
            keyMapping.mapping[pressedKeys]
                ?.let{emulatedKey -> keyStrokeListeners.forEach{listener -> listener.accept(emulatedKey)}}
        }
    }


    fun onKeysChange(consumer:Consumer<Map<GamePadKeys,Boolean>>) {
        inputChangeListeners.add(consumer)
    }

    fun onEmulatedKey(consumer:Consumer<String>) {
        keyStrokeListeners.add(consumer)
    }

    private fun setLogicalKeyPress() {
        inputs.map(::processButtonInput).forEach{ keyPress -> pressedKeys[keyPress.key] = keyPress.pressed }
        processAnalogSticksInput()
    }


    private fun pollControllerInput() {
        controller.poll()

        val queue: EventQueue = controller.eventQueue
        val event = Event()
        // Read events until the queue is empty
        var newEvents:Boolean = false;
        while (queue.getNextEvent(event)) {
            val comp: Component = event.component

            println("input : ${comp.name} -> ${event.value}")

            val key = configs.inputComponentMapping[comp.name]
            key?.let { input -> inputs[input] = event.value }
            setLogicalKeyPress()
            newEvents = true
        }
        if(newEvents) {
            inputChangeListeners.forEach { it.accept(pressedKeys) }
        }
    }

    private fun processAnalogSticksInput() {
        val lStickAngle = getStickAngle(inputs[L_STICK_X], inputs[L_STICK_Y])
        val rStickAngle = getStickAngle(inputs[R_STICK_X], inputs[R_STICK_Y])
        pressedKeys[L_STICK_DOWN] = lStickAngle in 256..284
        pressedKeys[L_STICK_UP] = lStickAngle in 75..105
        pressedKeys[L_STICK_RIGHT] = lStickAngle in 345..360 || lStickAngle in 0..15
        pressedKeys[L_STICK_LEFT] = lStickAngle in 165..195
        pressedKeys[L_STICK_DOWN_LEFT] = lStickAngle in 300..330
        pressedKeys[L_STICK_DOWN_RIGHT] = lStickAngle in 210..240
        pressedKeys[L_STICK_UP_RIGHT] = lStickAngle in 30..60
        pressedKeys[L_STICK_UP_LEFT] = lStickAngle in 120..150

        pressedKeys[R_STICK_DOWN] = rStickAngle in 256..284
        pressedKeys[R_STICK_UP] = rStickAngle in 75..105
        pressedKeys[R_STICK_RIGHT] = rStickAngle in 345..360 || rStickAngle in 0..15
        pressedKeys[R_STICK_LEFT] = rStickAngle in 165..195
        pressedKeys[R_STICK_DOWN_LEFT] = rStickAngle in 300..330
        pressedKeys[R_STICK_DOWN_RIGHT] = rStickAngle in 210..240
        pressedKeys[R_STICK_UP_RIGHT] = rStickAngle in 30..60
        pressedKeys[R_STICK_UP_LEFT] = rStickAngle in 120..150
    }


    private fun getStickAngle(origXValue: Float?, origYValue: Float?) : Int {
        val cos = (origXValue ?: 0.0f).toDouble()
        val sine:Double  = (origYValue ?: 0.0f).toDouble()
        return ((atan(sine/cos) * (180 / Math.PI))).toInt()
    }

    private fun processButtonInput(input: Map.Entry<GamePadKeys, Float>): KeyPress {
        return when(input.key) {
            L_TRIGGER, R_TRIGGER -> KeyPress(input.key, input.value > 0.1)
            else -> KeyPress(input.key, input.value > 0.5)
        }
    }

    private data class KeyPress(val key: GamePadKeys, val pressed: Boolean)
}