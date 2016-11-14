package github.users.eirikma.iteratorgenerators;

/**
 * Extended Yield that remembers something about previously yielded item(s) and
 * can hold one object of "iterator state" to compensate for lack of proper for-comprehensions in java.
 *
 *
 * @param <S> "state" that may be kept between invocations. Any object you might like to keep.
 *            Use for instance a HashMap or Tuple if you want to keep several values.
 * @param <T> "target" or result object type output from this generator. If the Consumer is an Iterator, this will be the type of objects iterated.
 */
public interface StatefulYield<S, T> extends Yield<T> {

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
}
