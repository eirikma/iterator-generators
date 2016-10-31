package github.users.eirikma.iteratorgenerators;


import com.google.common.collect.UnmodifiableIterator;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Iterators {
    private Iterators() {
    }

    public static <T> Iterable<T> eachOf(final Iterator<T> iterator) {
        return () -> {
            return (iterator instanceof Iterable ? ((Iterable) iterator).iterator() : iterator);
        };
    }

    public static <T> Iterator<T> values(T... values) {
        return reInitializableCollectionIterator(Arrays.asList(values));
    }


    public static <T> Iterator<T> generator(Generator<T, T> generator) {
        return generatorWithState(null, generator);
    }


    public static <T, S> Iterator<T> generatorWithState(final S initialState,
                                                        Generator<T, S> generator) {
        checkNotNull(generator);
        class Holder<E> {
            public Holder(E element) {
                this.element = element;
            }

            E element;
        }
        return new UnmodifiableIterator<T>() {
            private LinkedList<T> yieldedValues = new LinkedList<T>();
            private final Holder<S> stateHolder = new Holder<S>(initialState);
            private final Holder<T> lastYield = new Holder<T>(null);

            @Override
            public boolean hasNext() {
                if (yieldedValues.isEmpty()) {
                    generator.yieldNextValues(new Yield<S, T>() {
                        @Override
                        public void yield(T element) {
                            yieldedValues.addLast(element);
                            // consider moving this til after yieldNextValues returns:
                            lastYield.element = element;
                        }

                        @Override
                        public S getState() {
                            return stateHolder.element;
                        }

                        @Override
                        public void setState(S state) {
                            stateHolder.element = state;
                        }

                        @Override
                        public T previous() {
                            return lastYield.element;
                        }
                    });
                }
                return !yieldedValues.isEmpty();
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return yieldedValues.removeFirst();
                }
                throw new NoSuchElementException("next");
            }
        };
    }


    /**
     * invoke code and get return value without checked exceptions
     *
     * @param callable
     * @param <V>      type of return value
     * @return V
     */
    static <V> V unchecked(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * flattens one level of iterators: iterating the values contained in the provided iterators.
     *
     * @param iterators and iterator over iterators
     * @param <T>       type of elements in those iterators
     * @return the elements inside the iterators
     */
    public static <T> Iterator<T> flatten(Iterator<Iterator<T>> iterators) {
        return generatorWithState((iterators.hasNext() ? iterators.next() : null), yield -> {
            Iterator<T> current = yield.getState();
            while (current != null && !current.hasNext()) {
                current = iterators.hasNext() ? iterators.next() : null;
                yield.setState(current);
            }
            if (current != null && current.hasNext()) {
                yield.yield(current.next());
            }
        });
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
     *
     * @param iterator
     * @param times
     * @param <T>
     * @return the iteration, iterated 'times' times.
     */
    public static <T> Iterator<T> repeat(Iterator<T> iterator, int times) {
        final int[] repetitions = {0};
        final Iterator[] iter = {iterator};
        return generator((state) -> {
            if (iter[0].hasNext()) {
                state.yield((T) iter[0].next());
            } else if (++repetitions[0] < times) {
                if (iter[0] instanceof RepeatableIterator) {
                    ((RepeatableIterator) iter[0]).reset();
                } else if (iterator instanceof Iterable) {
                    iter[0] = ((Iterable<T>) iterator).iterator();
                }
                if (iter[0].hasNext()) {
                    state.yield((T) iter[0].next());
                }
            }
        });
    }


    private static <T> Iterator<T> reInitializableCollectionIterator(Collection<T> collection) {
        class It<T> implements Iterable<T>, Iterator<T>, RepeatableIterator<T> {
            private final Collection<T> collection;
            private Iterator<T> iter = null;

            public It(Collection<T> collection) {
                this.collection = collection;
                reInit();
            }

            @Override
            public Iterator<T> iterator() {
                reInit();
                return iter;
            }

            @Override
            public void reset() {
                reInit();
            }

            private void reInit() {
                this.iter = collection.iterator();
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


//    private static class RepeatablePushbackableIterator<T> implements Iterator<T>, RepeatableIterator<T>, PushBackIterator<T> {
//        protected final RepeatableIterator<T> source;
//        private Stack<T> pushbackStack = new Stack<>();
//
//        public RepeatablePushbackableIterator(RepeatableIterator<T> source) {
//            this.source = source;
//        }
//
//        @Override
//        public void pushback(T element) {
//            pushbackStack.push(element);
//        }
//
//        @Override
//        public void reset() {
//            // todo: what about the pushback stack? should that be reset as well?
//            source.reset();
//        }
//
//        @Override
//        public boolean hasNext() {
//            return !pushbackStack.isEmpty() || source.hasNext();
//        }
//
//        @Override
//        public T next() {
//            if (pushbackStack.size() > 0) {
//                return pushbackStack.pop();
//            }
//            return source.next();
//        }
//    }
//
//    private static class IterableRepeatablePushbackableIterator<T> extends RepeatablePushbackableIterator<T> implements Iterable<T> {
//
//        private final Iterable<T> sourceIterable;
//
//        public IterableRepeatablePushbackableIterator(RepeatableIterator<T> source) {
//            super(source);
//            sourceIterable = (Iterable<T>) source;
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            // hmmm... this suppresses the call to source.iterator(), but we don't know if that is going to return the same
//            // iterator of a different one, and it already said it was repeatable, which is more or less the same.
//            reset();
//            return this;
//        }
//
//    }
//
//    private static class IterablePushbackableIterator<T> implements Iterator<T>, Iterable<T>, PushBackIterator<T> {
//        private final Iterator<T> source;
//        private final Iterable<T> sourceIterable;
//        private final Stack<T> pushbackStack = new Stack<>();
//
//        public IterablePushbackableIterator(Iterator<T> source) {
//            this.source = source;
//            this.sourceIterable = (Iterable<T>) source;
//        }
//
//        @Override
//        public void pushback(T element) {
//            pushbackStack.push(element);
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            // no other way to handle this, sorry.
//            return pushbackable(sourceIterable.iterator());
//        }
//
//        @Override
//        public boolean hasNext() {
//            if (!pushbackStack.isEmpty()) return true;
//            return source.hasNext();
//        }
//
//        @Override
//        public T next() {
//            if (pushbackStack.size() > 0) {
//                return pushbackStack.pop();
//            }
//            return source.next();
//        }
//    }

}
