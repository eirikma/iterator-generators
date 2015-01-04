package github.users.eirikma.iteratorgenerators;


import java.lang.reflect.Array;
import java.util.*;

public final class Iterators {
    private Iterators() {
    }

    public static <T> Iterable<T> eachOf(final Iterator<T> iterator) {
        return () -> {return (iterator instanceof Iterable ? ((Iterable) iterator).iterator() : iterator);};
    }

    public static <T> Iterator<T> values(T... values) {
        return reInitializableCollectionIterator(Arrays.asList(values));
    }


    public static <T> Iterator<T> generator(Generator<T> g) {
        return new GeneratorImpl<T>(g);
    }

    public static <T> Collection<T> collect(final Iterator<T> iteration) {
        ArrayList<T> retval = new ArrayList<T>();
        for (T t : eachOf(iteration)) {
            retval.add(t);
        }
        return Collections.unmodifiableList(retval);
    }

    public static <T> T[] toArray(Iterator<T> iter, Class<? extends T> type) {
        Collection<T> collection = collect(iter);
        T[] array = (T[]) Array.newInstance(type, collection.size());
        return collection.toArray(array);
    }

    public static <T> PushBackIterator<T> pushbackable(final Iterator<T> source) {
        return new PushBackIterator<T>() {
            private Stack<T> pushbackStack = new Stack<>();

            @Override
            public void pushback(T element) {
                pushbackStack.push(element);
            }

            @Override
            public boolean hasNext() {
                return (pushbackStack.size() > 0) || (source.hasNext());
            }

            @Override
            public T next() {
                return (pushbackStack.size() > 0) ? pushbackStack.pop() : source.next();
            }
        };
    }


    public static <T1, T2> Iterator<Tuple2<T1, T2>> zip(final Iterator<T1> iter1, final Iterator<T2> iter2) {
        return generator((state) -> {
            if (iter1.hasNext() && iter2.hasNext()) {
                state.yield(new Tuple2<>(iter1.next(), iter2.next()));
            }
        });
    }

    /**
     * repeat the iteration if it is repeatable (i.e.: implements RepeatableIterator or Iterable).
     * @param iterator
     * @param times
     * @param <T>
     * @return the iteration, iterated 'times' times.
     */
    public static <T> Iterator<T> repeat(Iterator<T> iterator, int times) {
        final int[] repetitions = {0};
        final Iterator[] iter = {iterator};
        return generator((state)-> {
            if (iter[0].hasNext()) {
                state.yield((T) iter[0].next());
            } else if (++repetitions[0] < times) {
                if (iter[0] instanceof RepeatableIterator) {
                    ((RepeatableIterator)iter[0]).reset();
                } else if (iterator instanceof Iterable) {
                    iter[0] = ((Iterable<T>)iterator).iterator();
                }
                if (iter[0].hasNext()) {
                    state.yield((T) iter[0].next());
                }
            }
        });
    }


    private static <T> Iterator<T> reInitializableCollectionIterator(Collection<T> collection) {
        class It<T> implements Iterable<T>, Iterator<T>  {
            private final Collection<T> collection;
            private Iterator<T> iter = null;

            public It(Collection<T> collection) {
                this.collection = collection;
                iterator();
            }

            @Override
            public Iterator<T> iterator() {
                this.iter = collection.iterator();
                return iter;
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return iter.next();
            }
        }
        return new It(collection);
    }

}
