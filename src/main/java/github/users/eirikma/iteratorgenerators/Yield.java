package github.users.eirikma.iteratorgenerators;

/**
 * Yield: interface for outputting a stream/ or sequence of objects generated to some kind of consumer.
 * If Iterator resembles an InputStream for higher-level objects, then Yield corresponds to OutputStream for objects:
 * a target interface for delivering a stream of objects that others can consume (for instance using an Iterator).
 *
 * Consumer implementations can be for instance an Iterator reading from the Yield, appending the items
 * to a concurrent queue that an other thread is reading from,
 * serializing objects and writing them to an OutputStream or whatever. This interface decouples the
 * actual consumer from the producer of the objects passed on.
 *
 * @param <S> "state" that may be kept between invocations. Any object you might like to keep.
 *            Use for instance a HashMap or Tuple if you want to keep several values.
 * @param <T> "target" or result object type output from this generator. If the Consumer is an Iterator, this will be the type of objects iterated.
 *
 */
public interface Yield<S, T> extends Closeable {

    /**
     * yield a value to output from the iterator
     */
    void yield(T value);


    /**
     * returns the last element yielded
     */
    T previous();

    /**
     * Set some state that the Generator might want to keep between calls.
     *
     * @param state
     */
    void setState(S state);

    /**
     * Get the state that was saved using setState(S).
     */
    S getState();

    /**
     * @return number of items yielded
     */
    long count();


    /**
     * Yield many values from a buffer. Similar to  'write(byte[] buffer, int offset, int len)' in java.io.OutputStream,
     * but for objects instead of bytes.
     */
    default int yieldN(T[] values, int offset, int count) {
        int yielded = 0;
        for (int i = offset; i < min(values.length, offset + count); i++) {
            yield(values[i]);
            yielded++;
        }
        return yielded;
    }


    @Override
    default void close() throws IOException {}

    boolean isClosed();


    /**
     * The number of items that can be yielded without blocking. Similar to 'available()' in java.io.InputStream,
     * but this time for output and counted in terms of objects instead of bytes.
     *
     * @return number of items that can be yielded without blocking. default 1, irrespective of the underlying target for the yield.
     */
    default int capacity() {
        return 1;
    }
}
