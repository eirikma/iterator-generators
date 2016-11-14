package github.users.eirikma.iteratorgenerators;

/**
 * Lambda type to use for generators: loops outputting (possibly infinite sequences of) values that should be iterated over.
 *
 * A Generator is called whenever someone wants to consume objects they produce. If the Generator
 * doesn't yield any values, the stream is considered ended. An Iterator delivering the values
 * yielded by the Generator will then reply with 'false' on the next call to 'hasNext()'.
 */
public interface Generator<T, S> {

    /**
         * Produce new value(s) for the iterator to iterate over.
         * This method gets called when ever someone calls the surrounding Iterator's next() or hasNext() methods,
         * and all the previously yielded values have been consumed.
         * If no values are yielded, the Iterators hasNext()-method will return false and the
         * generator is finished. More than one value may be yielded. These will be kept in memory
         * until they are consumed by the iterator.
         *
         * All code executes in the same thread as the surrounding iterator calling this method.
         * Infinite generators must can not loop forever, but must return now and then.
         * They will be called again in order to proceed, once the previously yielded elements are consumed.
         *
         * @param yieldTarget - the target containing the yield(t) method that must be called to yield values.
         */
    void yieldNextValues(Yield<S, T> yieldTarget);

}

