package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.function.Function;

/**
 *
 */
public interface Processor <I, S, O> {
   default void process(Iterator<I> input, Yield<S, O> output, Function<I, O> function) {
       if (input.hasNext()) output.yield(function.apply(input.next()));
   }
}
