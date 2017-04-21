package github.users.eirikma.iteratorgenerators;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Not thread safe
 */
class MultiThreadedObjectPipe<T> implements ObjectPipe<T> {

    private final BlockingQueue<T> buffer;
    private volatile  boolean closed;
    private volatile long yieldCount = 0L;
    private Yield<T> yield;
    private IteratorExt<T> iterator;

    MultiThreadedObjectPipe() {
        this(50000);
    }
    MultiThreadedObjectPipe(int bufferCapacity) {

        buffer = new LinkedBlockingQueue<T>(5000);

        yield = new Yield<T>() {
            @Override
            public void yield(T value) {
                yieldCount += 1;
                if (isClosed()) {
                    throw new RuntimeException("closed");
                }
                buffer.add(value);
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
                return buffer.size() > 0 || !closed;
            }

            @Override
            public T next() {
                if (hasNext() && !isClosed()) {
                    try {
                        return buffer.take();
                    } catch (InterruptedException e) {
                        closed = true;
                        throw new RuntimeException(e);
                    }
                }
                throw new NoSuchElementException("next");
            }
        };
    }

    public Yield<T> getYieldTarget() {
        return yield;
    }
    public IteratorExt<T> getIterator() { return iterator;}
}
