package com.iapp.rodsher.util;

import java.util.Objects;

/**
 * This is an immutable class that stores two object references
 * @author Igor Ivanov
 * @version 1.0
 * */
public final class Pair<K, V> {

    /** first object */
    private final K key;
    /** second object */
    private final V value;

    /** Creates instance objects with two references, can be null */
    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }

    /** returns first object */
    public K getKey() {
        return key;
    }

    /** creates a new object with new key but old value */
    public Pair<K, V> setKey(K key) {
        return new Pair<>(key, value);
    }

    /** returns second object */
    public V getValue() {
        return value;
    }

    /** creates a new object with new value but old key */
    public Pair<K, V> setValue(V value) {
        return new Pair<>(key, value);
    }

    /** returns true if the key reference is identical */
    public boolean containsKey(K key) {
        return this.key == key;
    }

    /** returns true if the value reference is identical */
    public boolean containsValue(V value) {
        return this.value == value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
