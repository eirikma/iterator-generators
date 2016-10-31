package github.users.eirikma.iteratorgenerators;

/**
 *
 * If Iterator resembles an InputStream for higher-level objects, then Yield corresponds to OutputStream for objects:
 * a target interface for delivering a stream of objects that others can consume (for instance using an Iterator).
 *
 * @param <S> "state" that may be kept between invocations. Any object you might like to keep.
 *            Use for instance a HashMap or Tuple if you want to keep several values.
 * @param <T> "target" or result object type output from this generator
 */
public interface Yield<S, T> {

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
}
