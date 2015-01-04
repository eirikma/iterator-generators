package github.users.eirikma.iteratorgenerators;

import java.util.*;
import java.util.function.Function;

import static github.users.eirikma.iteratorgenerators.Iterators.eachOf;

public class Maps {

    private Maps(){}

    /**
     * creates a Map containing the specified elements.
     * Suitable for statically declared maps (which are sometimes replaced by 'rich' enums but not always).
     * see method entry(key,value), which ca be used like this:
     * <code>
     *     Map m = map( entry("A", 1), entry("B", 2) );
     *
     * </code>
     */

    public static <K,V> Map<K,V> map(Map.Entry<K,V>... entries) {
        HashMap<K, V> kvHashMap = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            kvHashMap.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(kvHashMap);
    }

    /**
     * creates parameters for the map(entries...)  function above.
     */
    public static <K,V> Map.Entry<K,V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }


    /**
     * creates a map constructed by applying the value function to the keys provided.
     */
    public static <K,V> Map<K,V> map(Iterator<K> keys, Function<K,V> valueFunction) {
        HashMap<K, V> retval = new HashMap<>();
        for (K key : eachOf(keys)) {
            retval.put(key, valueFunction.apply(key));
        }
        return Collections.unmodifiableMap(retval);
    }

    /**
     * creates a map constructed by applying the key function to the values provided.
     */
    public static <K,V> Map<K,V> reverseMap(Iterator<V> values, Function<V,K> keyFunction) {
        HashMap<K, V> retval = new HashMap<>();
        for (V value : eachOf(values)) {
            retval.put(keyFunction.apply(value), value);
        }
        return Collections.unmodifiableMap(retval);
    }


    /**
     * creates a map constructed by applying key and value functions to the input provided.
     */
    public static <I,K,V> Map<K,V> map(Iterator<I> input, Function<I,K> keyFunction,  Function<I,V> valueFunction) {
        HashMap<K, V> retval = new HashMap<>();
        for (I item : eachOf(input)) {
            retval.put(keyFunction.apply(item), valueFunction.apply(item));
        }
        return Collections.unmodifiableMap(retval);
    }

}
