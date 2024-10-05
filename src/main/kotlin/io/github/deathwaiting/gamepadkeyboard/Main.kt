package io.github.deathwaiting.gamepadkeyboard
import net.java.games.input.*
import java.nio.file.Files
import java.nio.file.Path


fun main(args: Array<String>) {
    // Create an event object for the underlying plugin to populate
    // Create an event object for the underlying plugin to populate


    // Get the available controllers

    // Get the available controllers
    val controllers: Array<Controller> = ControllerEnvironment.getDefaultEnvironment().controllers

    for (i in controllers.indices) {
        println("Controller " + i + ": " + controllers[i].getName())

        val controller:GamePadController = DefaultGamePadController(controllers[i])
        val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));
        val mapping = GamePadKeyMapping.fromYml(mappingYml)
        val mapper = GamePadToKeyboardMapper(mapping, controller)

        mapper.keyStrokes.subscribe().with(::print)

        while(true) {

        }

    }
}