package github.users.eirikma.iteratorgenerators;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Not thread safe
 */
class MultiThreadedObjectPipe<T> implements ObjectPipe<T> {

    private final BlockingQueue<T> buffer;
    private final AtomicBoolean closed = new AtomicBoolean(false);
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
                try {
                    buffer.put(value);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public long count() {
                return yieldCount;
            }

            @Override
            public boolean isClosed() {
                return closed.get();
            }

            @Override
            public void close() throws IOException {
                closed.set(true);;
            }
        };

        iterator = new IteratorExt<T>() {

            @Override
            public int available() {
                return buffer.size();
            }

            @Override
            public boolean hasNext() {
                 if (buffer.size() > 0 ) {
                    return true;
                }
                // wait for some outcome from the other thread.
                while (!closed.get() && buffer.size() <= 0) {
                     Thread.yield();
                }
                return buffer.size() > 0;
            }

            @Override
            public T next() {
                if (hasNext() && !isClosed()) {
                    try {
                        T value = null;
                        while (value == null && !isClosed()) {
                            value = buffer.poll(10, TimeUnit.MILLISECONDS);
                        }
                        return value;
                    } catch (InterruptedException e) {
                        closed.set(true);
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
