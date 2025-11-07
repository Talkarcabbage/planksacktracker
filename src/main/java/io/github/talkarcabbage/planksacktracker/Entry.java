package io.github.talkarcabbage.planksacktracker;

import lombok.Getter;

public class Entry<K,V> {
    @Getter
    private final K key;
    @Getter
    private final V value;
    public Entry(K key, V value){
        this.key=key;
        this.value=value;
    }
}
