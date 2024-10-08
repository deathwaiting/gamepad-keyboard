package io.github.deathwaiting.gamepadkeyboard;

import io.smallrye.mutiny.Multi
import mu.KotlinLogging
import net.java.games.input.Component
import net.java.games.input.Controller
import net.java.games.input.Event
import net.java.games.input.EventQueue
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class GamePadKey {
    X, Y, B, A,
    UP, DOWN, RIGHT, LEFT,
    L_STICK_UP, L_STICK_DOWN, L_STICK_RIGHT, L_STICK_LEFT, L_STICK_UP_RIGHT, L_STICK_UP_LEFT, L_STICK_DOWN_RIGHT, L_STICK_DOWN_LEFT, L_STICK_PRESS,
    R_STICK_UP, R_STICK_DOWN, R_STICK_RIGHT, R_STICK_LEFT, R_STICK_UP_RIGHT, R_STICK_UP_LEFT, R_STICK_DOWN_RIGHT, R_STICK_DOWN_LEFT, R_STICK_PRESS,
    L_TRIGGER, R_TRIGGER, L_BUTTON, R_BUTTON;
}

enum class GamePadInput {
    X, Y, B, A,
    UP, DOWN, RIGHT, LEFT,
    L_STICK_X, L_STICK_Y, R_STICK_X, R_STICK_Y, L_STICK_PRESS, R_STICK_PRESS,
    L_TRIGGER, R_TRIGGER, L_BUTTON, R_BUTTON;
}


interface GamePadController {
    val inputs: Multi<Map<GamePadInput,Float>>
    val name:String
}

//should be for xbox controller ... on linux
private val DEFAULT_CONTROLLER_MAPPING = mapOf(
    "rx" to GamePadInput.R_STICK_X, "ry" to GamePadInput.R_STICK_Y,
    "x" to GamePadInput.L_STICK_X, "y" to GamePadInput.L_STICK_Y,
    "A" to GamePadInput.A, "B" to GamePadInput.B, "X" to GamePadInput.X, "Y" to GamePadInput.Y, "Left Thumb" to GamePadInput.L_BUTTON, "Right Thumb" to GamePadInput.R_BUTTON, "lz" to GamePadInput.L_TRIGGER, "rz" to GamePadInput.R_TRIGGER
)

data class ControllerConfigs(val samplingPeriod: Long = 100,
                             val inputComponentMapping : Map<String,GamePadInput> = DEFAULT_CONTROLLER_MAPPING)



class DefaultGamePadController(val controller: Controller, val configs: ControllerConfigs = ControllerConfigs(),
                               override val name: String = controller.name
) : GamePadController{
    private val logger = KotlinLogging.logger {}
    private val inputsValues : MutableMap<GamePadInput,Float> = ConcurrentHashMap()

    override val inputs: Multi<Map<GamePadInput,Float>> =
            Multi.createFrom().ticks()
                .onExecutor(Executors.newSingleThreadScheduledExecutor())
                .every(Duration.ofMillis(configs.samplingPeriod))
                .map{pollControllerInput()}


    init {
        logger.info { ">> Using controller : ${controller.name} , input sampling rate: ${configs.samplingPeriod} ms" }
    }

    private fun pollControllerInput(): Map<GamePadInput,Float> {
        controller.poll()

        val queue: EventQueue = controller.eventQueue
        val event = Event()
        // Read events until the queue is empty
        while (queue.getNextEvent(event)) {
            val comp: Component = event.component

            logger.trace { "input : ${comp.name} -> ${event.value}"}

            val key = configs.inputComponentMapping[comp.name]
            key?.let { input -> inputsValues[input] = event.value }
        }
        return inputsValues
    }
}