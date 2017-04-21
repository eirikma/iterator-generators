package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;

/**
 * Convenience interface for Generators that consume from an Iterator.
 * The iterator will be passed to the process method.
 * The same iterator instance will be passed each time.
 *
 * The processing is finished when a call results in no values being yielded.
 */
public interface Processor<I,  O> {
    /**
     * Convenience interface for Generators that consume from an Iterator.
     * The iterator will be passed to the process method.
     * The same iterator instance will be passed each time. The Yield (output channel) will of course also be the same each time.
     *
     * The processing is finished when no values are yielded.
     *
     * @param input
     * @param output
     */
    void process(Iterator<I> input, Yield<O> output);
}
