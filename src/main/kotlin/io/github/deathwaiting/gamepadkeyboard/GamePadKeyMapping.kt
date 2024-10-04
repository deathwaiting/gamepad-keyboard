package io.github.deathwaiting.gamepadkeyboard;

import org.yaml.snakeyaml.Yaml

enum class GamePadKeys {
    X, Y, B, A, 
    UP, DOWN, RIGHT, LEFT,
    L_STICK_X, L_STICK_Y, R_STICK_X, R_STICK_Y,
    L_STICK_UP, L_STICK_DOWN, L_STICK_RIGHT, L_STICK_LEFT, L_STICK_UP_RIGHT, L_STICK_UP_LEFT, L_STICK_DOWN_RIGHT, L_STICK_DOWN_LEFT, L_STICK_PRESS,
    R_STICK_UP, R_STICK_DOWN, R_STICK_RIGHT, R_STICK_LEFT, R_STICK_UP_RIGHT, R_STICK_UP_LEFT, R_STICK_DOWN_RIGHT, R_STICK_DOWN_LEFT, R_STICK_PRESS,
    L_TRIGGER, R_TRIGGER, L_BUTTON, R_BUTTON;
    }



class GamePadKeyMapping private constructor(mappingRaw: Map<String, String>) {
    class MappingYml {
        lateinit var mapping:Map<String,String>
    }

    val mapping: Map<Set<GamePadKeys>,String>

    init {
        mapping = mappingRaw.mapKeys(::getGamePadKeysCombination)
    }

    companion object {
        fun fromYml(yml:String): GamePadKeyMapping {
            val mapping = Yaml().loadAs(yml, MappingYml::class.java);
            return GamePadKeyMapping(mapping.mapping)
        }
    }

    private fun getGamePadKeysCombination(it: Map.Entry<String, String>) = it.key.replace(" ", "")
        .split("+")
        .map(GamePadKeys::valueOf)
        .toSet()

    fun of(keyCombination: Set<GamePadKeys>): String? {
        return mapping[keyCombination];
    }
}
