package io.github.deathwaiting.gamepadkeyboard
import io.github.deathwaiting.gamepadkeyboard.utils.SysPropLogbackConfigurator
import net.java.games.input.*
import java.nio.file.Files
import java.nio.file.Path


fun main(args: Array<String>) {
    //should be for generic controller ... on linux
    val genericControllerMapping = mapOf(
        "rz" to GamePadInput.R_STICK_X, "z" to GamePadInput.R_STICK_Y,
        "x" to GamePadInput.L_STICK_X, "y" to GamePadInput.L_STICK_Y,
        "Thumb 2" to GamePadInput.A, "Thumb" to GamePadInput.B, "Top" to GamePadInput.X, "Trigger" to GamePadInput.Y, "Top 2" to GamePadInput.L_BUTTON, "Pinkie" to GamePadInput.R_BUTTON, "Base" to GamePadInput.L_TRIGGER, "Base 2" to GamePadInput.R_TRIGGER,
        "Base 6" to GamePadInput.R_STICK_PRESS, "Base 5" to GamePadInput.L_STICK_PRESS
    )

    SysPropLogbackConfigurator.configLogsFromSysProperties()

    // Get the available controllers
    val controllers: Array<Controller> = ControllerEnvironment.getDefaultEnvironment().controllers

    for (i in controllers.indices) {
        println("Controller " + i + ": " + controllers[i].getName())

        val controller:GamePadController = DefaultGamePadController(controllers[i], ControllerConfigs(inputComponentMapping = genericControllerMapping))
        val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));
        val mapping = GamePadKeyMapping.fromYml(mappingYml)
        val mapper = GamePadToKeyboardMapper(mapping, controller)

        mapper.keyStrokes.subscribe().with(::print)

        while(true) {

        }

    }
}