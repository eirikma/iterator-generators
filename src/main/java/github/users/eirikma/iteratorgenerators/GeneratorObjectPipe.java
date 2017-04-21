package github.users.eirikma.iteratorgenerators;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Not thread safe
 */
class GeneratorObjectPipe<T> implements ObjectPipe<T> {

    private final Generator<T, ? extends Yield<T>> generator;
    private LinkedList<T> buffer = new LinkedList<T>();
    private volatile boolean closed;
    private long yieldCount = 0L;
    private Yield<T> yield;
    private IteratorExt<T> iterator;

    GeneratorObjectPipe(Generator<T, Yield<T>> generator) {
        this.generator = generator;

        yield = new Yield<T>() {
            @Override
            public void yield(T value) {
                yieldCount++;
                if (isClosed()) {
                    throw new RuntimeException("closed");
                }
                buffer.addLast(value);
            }

            @Override
            public long count() {
                return yieldCount;
            }

            @Override
            public boolean isClosed() {
                return closed;
            }


        };

        iterator = new IteratorExt<T>() {
            @Override
            public boolean hasNext() {
                if (buffer.size() > 0) {
                    return true;
                }
                if (closed) return false;
                generator.yieldNextValues(yield);
                return buffer.size() > 0;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return buffer.pop();
                }
                throw new NoSuchElementException("next");
            }

        };
    }

    public Yield<T> getYieldTarget() {
        return yield;
    }

    public IteratorExt<T> getIterator() {
        return iterator;
    }
}
