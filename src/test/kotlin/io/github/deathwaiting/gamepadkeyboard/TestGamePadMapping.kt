package io.github.deathwaiting.gamepadkeyboard

import io.github.deathwaiting.gamepadkeyboard.GamePadKeyMapping
import io.github.deathwaiting.gamepadkeyboard.GamePadKeys
import io.github.deathwaiting.gamepadkeyboard.GamePadKeys.L_STICK_PRESS
import io.github.deathwaiting.gamepadkeyboard.GamePadKeys.R_STICK_RIGHT
import net.java.games.input.Controller
import net.java.games.input.Event
import net.java.games.input.EventQueue
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals


class TestGamePadMapping {
   @Test
   fun gamePadMappingRead(): Unit {
      val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));

      val mapping = GamePadKeyMapping.fromYml(mappingYml);

      assertEquals("k", mapping.of(setOf(R_STICK_RIGHT, L_STICK_PRESS)))
   }

//   @Test
//   fun gamePadMapperTest() {
//      val mappingYml = Files.readString(Path.of("./src/test/resources/qwerty_mapping.yml"));
//
//      val mapping = GamePadKeyMapping.fromYml(mappingYml)
//      val mockedController = createMockedController()
//      val keyboardEmulator = GamePadToKeyboardMapper(mapping, mockedController, MapperConfigs(1000))
//      keyboardEmulator.onEmulatedKey { key ->
//         assertEquals("k", key)
//      }
//   }
//
//   private fun createMockedController(): Controller {
//      val eventQueue = EventQueue(5)
//      val event = Event()
//      event.set()
//      eventQueue.add()
//      val mocked = Mockito.mock(Controller::class.java)
//      Mockito.`when`(mocked.poll()).thenReturn(true)
//   }

}
