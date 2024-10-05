package io.github.deathwaiting.gamepadkeyboard

import io.github.deathwaiting.gamepadkeyboard.GamePadInput.*
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor
import org.awaitility.Awaitility
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals


class TestGamePadMapping {
   @Test
   fun gamePadMappingRead(): Unit {
      val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));

      val mapping = GamePadKeyMapping.fromYml(mappingYml);

      assertEquals("k", mapping.of(setOf(GamePadKey.R_STICK_RIGHT, GamePadKey.L_STICK_PRESS)))
   }

   @Test
   fun gamePadMapperTest() {
      var outputKey = ""
      val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));

      val mapping = GamePadKeyMapping.fromYml(mappingYml)
      val mockedController = MockController()
      val keyboardEmulator = GamePadToKeyboardMapper(mapping, mockedController)
      keyboardEmulator.keyStrokes.subscribe().with { s -> outputKey = s; print(s)}

      mockedController
         .sendInput(R_STICK_X, 0.944f)
         .sendInput(R_STICK_Y, 0.07f)
         .sendInput(L_STICK_PRESS, 1f)

      Awaitility.await().atMost(1, TimeUnit.SECONDS)
         .untilAsserted { assertEquals("k", outputKey) }
   }
}


class MockController(override val name: String = "Mocked") : GamePadController {

   private val mockedInputs : MutableMap<GamePadInput,Float> = ConcurrentHashMap()
   override val inputs: BroadcastProcessor<Map<GamePadInput,Float>> = BroadcastProcessor.create()

   init {
      inputs.subscribe().with { s -> print(s)}
   }
   fun sendInput(key:GamePadInput, value: Float): MockController {
      mockedInputs[key] = value
      inputs.onNext(mockedInputs)
      return this
   }
}