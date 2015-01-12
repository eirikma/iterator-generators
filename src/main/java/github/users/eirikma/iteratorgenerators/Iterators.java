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

    public static <I,O> Iterator<O> inputProcessor(Iterator<I> input, InputProcessor<I,O> processor) {
        return new InputProcessorImpl<>(pushbackable(input), processor);
    }

    public static <T> Collection<T> collect(final Iterator<T> iterator) {
        ArrayList<T> retval = new ArrayList<T>();
        while (iterator.hasNext()) {
            retval.add(iterator.next());
        }
        return Collections.unmodifiableList(retval);
    }

    public static <T> T[] toArray(Iterator<T> iter, Class<? extends T> type) {
        Collection<T> collection = collect(iter);
        T[] array = (T[]) Array.newInstance(type, collection.size());
        return collection.toArray(array);
    }

    public static <T> PushBackIterator<T> pushbackable(final Iterator<T> source) {

        if (source instanceof PushBackIterator) {
            return (PushBackIterator<T>) source;
        }

        boolean isRepeatable = (source instanceof RepeatableIterator);
        boolean isIterable = (source instanceof Iterable);

        if (isIterable && isRepeatable) {
            return new IterableRepeatablePushbackableIterator<T>((RepeatableIterator<T>) source);
        } else if (isRepeatable) {
            return new RepeatablePushbackableIterator<T>((RepeatableIterator<T>) source);
        } else if (isIterable) {
            return new IterablePushbackableIterator<T>(source);
        } else {
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
    }


    /**
     * zip together two sequences of values to a sequence of tuples holding
     * one value from each input sequence. The length of the result is equal to
     * the shortest of the input sequences.
     *
     * @param iter1
     * @param iter2
     * @param <T1>
     * @param <T2>
     * @return
     */
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





    private static class RepeatablePushbackableIterator<T> implements Iterator<T>, RepeatableIterator<T>, PushBackIterator<T> {
        protected final RepeatableIterator<T> source;
        private Stack<T> pushbackStack = new Stack<>();

        public RepeatablePushbackableIterator(RepeatableIterator<T> source) {
            this.source = source;
        }

        @Override
        public void pushback(T element) {
            pushbackStack.push(element);
        }

        @Override
        public void reset() {
            // todo: what about the pushback stack? should that be reset as well?
            source.reset();
        }

        @Override
        public boolean hasNext() {
            return !pushbackStack.isEmpty() || source.hasNext();
        }

        @Override
        public T next() {
            if (pushbackStack.size() > 0) {
                return pushbackStack.pop();
            }
            return source.next();
        }
    }

    private static class  IterableRepeatablePushbackableIterator<T> extends RepeatablePushbackableIterator<T> implements Iterable<T> {

        private final Iterable<T> sourceIterable;

        public IterableRepeatablePushbackableIterator(RepeatableIterator<T> source) {
            super(source);
            sourceIterable = (Iterable<T>) source;
        }

        @Override
        public Iterator<T> iterator() {
            // hmmm... this suppresses the call to source.iterator(), but we don't know if that is going to return the same
            // iterator of a different one, and it already said it was repeatable, which is more or less the same.
            reset();
            return this;
        }

    }

    private static class IterablePushbackableIterator<T> implements Iterator<T>, Iterable<T>, PushBackIterator<T> {
        private final Iterator<T> source;
        private final Iterable<T> sourceIterable;
        private final Stack<T> pushbackStack = new Stack<>();

        public IterablePushbackableIterator(Iterator<T> source) {
            this.source = source;
            this.sourceIterable = (Iterable<T>) source;
        }

        @Override
        public void pushback(T element) {
            pushbackStack.push(element);
        }

        @Override
        public Iterator<T> iterator() {
            // no other way to handle this, sorry.
            return pushbackable(sourceIterable.iterator());
        }

        @Override
        public boolean hasNext() {
            if (!pushbackStack.isEmpty()) return true;
            return source.hasNext();
        }

        @Override
        public T next() {
            if (pushbackStack.size() > 0) {
                return pushbackStack.pop();
            }
            return source.next();
        }
    }

}
