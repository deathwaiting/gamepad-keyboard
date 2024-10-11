package io.github.deathwaiting.gamepadkeyboard
import io.github.deathwaiting.gamepadkeyboard.utils.SysPropLogbackConfigurator
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import java.awt.Robot
import java.awt.event.KeyEvent
import java.nio.file.Files
import java.nio.file.Path


private val KEY_CODE_MAP: MutableMap<String, Int> = initKeystrokeMapping()
private val robot = Robot()

    // Add lowercase letters


fun main(args: Array<String>) {
    //should be for generic controller ... on linux
    val genericControllerMapping = mapOf(
        "rz" to GamePadInput.R_STICK_X, "z" to GamePadInput.R_STICK_Y,
        "x" to GamePadInput.L_STICK_X, "y" to GamePadInput.L_STICK_Y,
        "Thumb 2" to GamePadInput.A, "Thumb" to GamePadInput.B, "Top" to GamePadInput.X, "Trigger" to GamePadInput.Y, "Top 2" to GamePadInput.L_BUTTON, "Pinkie" to GamePadInput.R_BUTTON, "Base" to GamePadInput.L_TRIGGER, "Base 2" to GamePadInput.R_TRIGGER,
        "Base 6" to GamePadInput.R_STICK_PRESS, "Base 5" to GamePadInput.L_STICK_PRESS,
        "pov" to GamePadInput.LEFT
    )

    SysPropLogbackConfigurator.configLogsFromSysProperties()

    // Get the available controllers
    val controllers: Array<Controller> = ControllerEnvironment.getDefaultEnvironment().controllers

    for (i in controllers.indices) {
        println("Controller " + i + ": " + controllers[i].getName())

        val controller:GamePadController = DefaultGamePadController(controllers[i], ControllerConfigs(samplingPeriod = 100, inputComponentMapping = genericControllerMapping))
        val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));
        val mapping = GamePadKeyMapping.fromYml(mappingYml)
        val mapper = GamePadToKeyboardMapper(mapping, controller)

        mapper.keyStrokes.cache()
            .subscribe().with(::emulateKeyStroke)

        while(true) {

        }

    }
}

fun emulateKeyStroke(stroke:String) {
    KEY_CODE_MAP[stroke]
        ?.let{ event ->
            robot.keyPress(event)
            robot.keyRelease(event)
        }
}

fun initKeystrokeMapping(): MutableMap<String, Int> {
    val map: MutableMap<String, Int> = HashMap()

    val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
    for (c in lowercaseLetters.toCharArray()) {
        map[c.toString()] = KeyEvent.VK_A + (c - 'a')
    }

    // Add uppercase letters
    val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for (c in uppercaseLetters.toCharArray()) {
        map[c.toString()] = KeyEvent.VK_A + (c - 'A')
    }

    map["SHIFT"] = KeyEvent.VK_SHIFT
    map["ENTER"] = KeyEvent.VK_ENTER
    map["SPACE"] = KeyEvent.VK_SPACE
    map["CONTROL"] = KeyEvent.VK_CONTROL
    map["BACKSPACE"] = KeyEvent.VK_BACK_SPACE
    return map
}