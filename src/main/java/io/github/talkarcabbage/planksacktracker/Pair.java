package io.github.talkarcabbage.planksacktracker;

import lombok.Getter;

/**
 * Simple immutable implementation of a pair of objects
 * @param <One>
 * @param <Two>
 */
public class Pair<One, Two> {
    @Getter private final One one;
    @Getter private final Two two;
    public Pair(One one, Two two) {
        this.one = one;
        this.two=two;
    }
}
