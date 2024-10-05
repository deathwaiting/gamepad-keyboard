package io.github.deathwaiting.gamepadkeyboard

import io.smallrye.mutiny.Uni
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Flow
import java.util.function.Consumer

class ReactiveMap<K, V> {

    private val logger = KotlinLogging.logger {}
    val internalMap: Uni<MutableMap<K, V>> = Uni.createFrom().item(ConcurrentHashMap<K, V>())



    fun put(key: K, value: V): Uni<MutableMap<K,V>> {
        return internalMap.flatMap{ data -> data[key] = value ; Uni.createFrom().item(data) }
    }

    fun get(key: K): V? {
        return internalMap.await().indefinitely()[key]
    }

    fun remove(key: K): Uni<Map<K,V>> {
        return internalMap.flatMap{ data -> data.remove(key) ; Uni.createFrom().item(data) }
    }

    operator fun set(input: K, value: V) :Uni<Unit>{
        return put(input, value).replaceWith{}
    }

    fun subscribe(consumer: Consumer<Map<K, V>>) {
        internalMap.map(MutableMap<K,V>::toMap).onItem().invoke{ data -> consumer.accept(data) }
    }
}