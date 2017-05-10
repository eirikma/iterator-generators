package github.users.eirikma.iteratorgenerators;


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;

public final class Iterators {
    private Iterators() {
    }

    public static <T> Iterable<T> eachOf(final Iterator<T> iterator) {
        return () -> (iterator instanceof Iterable ? ((Iterable) iterator).iterator() : iterator);
    }

    public static <T> IteratorExt<T> iteratorExt(Iterator<T> iterator) {
        return iterator instanceof IteratorExt ?
                (IteratorExt<T>) iterator :
                new IteratorExt<T>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public T next() {
                        return null;
                    }
                };
    }

    public static <T> IteratorExt<T> values(T... values) {
        Iterator<T> iterator = asList(values).iterator();
        return new IteratorExt<T>() {
            int pos = 0;
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                T next = iterator.next();
                pos++;
                return next;
            }

            @Override
            public int available() {
                int length = values.length;
                return length - pos;
            }
        };
    }


    public static <I, O> Iterator<O> process(final Iterator<I> input, Processor<I, O> processor) {
        return generator(yield -> processor.process(input, yield));
    }


    public static <T> IteratorExt<T> generator(Generator<T, Yield<T>> generator) {
        checkNotNull(generator);
        // faking it: there is actually only one implementation: the one capable of holding state.
        return generatorWithState(null, (GeneratorWithState<T, T>) yieldTarget -> generator.yieldNextValues(yieldTarget));
    }


    public static <T, S> IteratorExt<T> generatorWithState(final S initialState,
                                                        GeneratorWithState<T, S> generator) {
        checkNotNull(generator);
        class Holder<E> {
            Holder(E element) {
                this.element = element;
            }
            E element;
        }
        return new IteratorExt<T>() {
            private final LinkedList<T> yieldedValues = new LinkedList<T>();
            private final Holder<S> stateHolder = new Holder<S>(initialState);
            private final Holder<T> lastYield = new Holder<T>(null);
            private long yieldCount = 0L;
            boolean closed = false;
            private final StatefulYield<S, T> yield = new StatefulYield<S, T>() {

                @Override
                public void yield(T element) {
                    yieldedValues.addLast(element);
                    lastYield.element = element;
                    yieldCount++;
                }
                public S getState() {
                    return stateHolder.element;
                }
                public void setState(S state) {
                    stateHolder.element = state;
                }
                public T previous() {
                    return lastYield.element;
                }
                @Override
                public long count() {
                    return yieldCount;
                }

                @Override
                public void close() throws IOException {
                    closeIterator();
                }

                @Override
                public boolean isClosed() {
                    return yieldedValues.isEmpty() && closed;
                }
            };

            @Override
            public boolean hasNext() {
                if (yieldedValues.isEmpty()) {
                    generator.yieldNextValues(yield);
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

            @Override
            public int available() {
                // hm....    hasNext() might block
                return hasNext() ? yieldedValues.size() : 0;
            }

            @Override
            public void close() throws IOException {
                closeIterator();
            }

            private void closeIterator() {
                this.closed = false;
            }

            @Override
            public boolean isClosed() {
                return closed;
            }
        };
    }


    public static <T> IteratorExt<List<T>> batchesOf(int batchSize, Iterator<T> iterator) {
        ArrayList<T> batch = new ArrayList<T>(batchSize);
        return generator((yield -> {
            while (iterator.hasNext() && batch.size() < batchSize) {
                batch.add(iterator.next());
            }
            if (!batch.isEmpty()) {
                yield.yield(new ArrayList<T>(batch));
                batch.clear();
            }
        }));
    }

    /**
     * flattens one level of iterators: iterating the values contained in the provided iterators.
     *
     * @param iterators and iterator over iterators
     * @param <T>       type of elements in those iterators
     * @return the elements inside the iterators
     */
    public static <T> IteratorExt<T> flatten(Iterator<Iterator<T>> iterators) {
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

    public static <T> MarkableIterator<T> markable(Iterator<T> inputSource) {
        return new MarkableIterator<T>() {

            boolean markIsSet = false;
            int maxReadAhead = 0;
            ArrayList<T> markBuffer = new ArrayList<T>();
            ArrayList<T> readBuffer = new ArrayList<T>();
            Iterator<T> readSource = inputSource;

            @Override
            public void mark(int maxReadaheadLimit) {
                if (maxReadaheadLimit < 0) {
                    throw new IllegalArgumentException("maxReadAhead must be >= 0, not: " + maxReadaheadLimit);
                }
                clearMarkIfExists();
                markIsSet = true;
                maxReadAhead = maxReadaheadLimit;
                markBuffer = new ArrayList<T>(maxReadaheadLimit);
            }

            private void clearMarkIfExists() {
                maxReadAhead = 0;
                markIsSet = false;
            }

            @Override
            public void reset() {
                if(!markIsSet) {
                    throw new IllegalStateException("mark is not set");
                }
                switchToReReadItemsFromMark();
                maxReadAhead = 0;
                markIsSet = false;
            }

            private void switchToReReadItemsFromMark() {
                readBuffer.clear();
                readBuffer.addAll(markBuffer);
                readSource = readBuffer.iterator();
            }

            @Override
            public boolean hasNext() {
                return readSource.hasNext();
            }

            @Override
            public T next() {
                if (markIsSet && markBuffer.size() == maxReadAhead) {
                    clearMarkIfExists();
                    readSource = inputSource;
                }

                T next = readSource.next();
                if (markIsSet) {
                    markBuffer.add(next);
                }
                return next;
            }

            @Override
            public int available() {
                return 0;
            }
        };
    }

    public static <T> T[] toArray(Iterator<T> iter, Class<? extends T> type) {
        Collection<T> collection = collect(iter);
        T[] array = (T[]) Array.newInstance(type, collection.size());
        return collection.toArray(array);
    }

    public static  <I,O> IteratorExt<O> background(Iterator<I> input, Processor<I,O> processor) {
        return background(yield -> processor.process(input, yield));
    }

    public static  <O> IteratorExt<O> background(Generator<O,Yield<O>> generator) {
        MultiThreadedObjectPipe<O> pipe = new MultiThreadedObjectPipe<>();
        Thread thread = new Thread(() -> {
            Yield<O> yield = pipe.getYieldTarget();
            while(!yield.isClosed()) {
                long before = yield.count();
                generator.yieldNextValues(yield);
                if (yield.count() == before) {
                    try {
                        yield.close();
                    } catch (IOException e) {
                        // not going to happen
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return pipe.getIterator();
    }

    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, ORDERED | IMMUTABLE), false);
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


}
