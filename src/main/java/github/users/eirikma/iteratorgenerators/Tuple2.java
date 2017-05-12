package github.users.eirikma.iteratorgenerators;

import java.util.Map;

public class Tuple2<K,V> implements Map.Entry<K,V>, Tuple {

    protected final K first;
    protected final V second;

    public Tuple2(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public K getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return second;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("setValue(" + value + ")");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple2)) return false;

        Tuple2 tuple2 = (Tuple2) o;

        if (first != null ? !first.equals(tuple2.first) : tuple2.first != null) return false;
        if (second != null ? !second.equals(tuple2.second) : tuple2.second != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return first != null ? first.hashCode() : 0;
    }
}
