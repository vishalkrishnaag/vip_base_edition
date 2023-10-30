package org.vip.Codegen;

import org.vip.Exception.VipCompilerException;

public class Pair<K,V> {
    public K key;
    public V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair() {
        this.key = null;
        this.value = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Pair) {
            Pair<K, V> other = (Pair<K, V>) obj;
            return key.equals(other.key) && value.equals(other.value);
        }

        return false;
    }

    public boolean contains(String s) throws VipCompilerException {
        if (this.key.equals(s)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode();
    }

    @Override
    public String toString() {
        return "(" + key + ", " + value + ")";
    }
}

