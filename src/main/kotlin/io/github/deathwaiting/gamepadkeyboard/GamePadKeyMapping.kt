package io.github.deathwaiting.gamepadkeyboard;

import org.yaml.snakeyaml.Yaml
import java.util.function.Consumer



class GamePadKeyMapping private constructor(mappingRaw: Map<String, String>) {
    class MappingYml {
        lateinit var mapping:Map<String,String>
    }

    val mapping: Map<Set<GamePadKey>,String>

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
        .map(GamePadKey::valueOf)
        .toSet()

    fun of(keyCombination: Set<GamePadKey>): String? {
        return mapping[keyCombination];
    }
}
