package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.function.Function;

public class Processors {

    public static <I, S, O>  Processor<I,S,O> transform(Iterator<I> input, Yield<S,O> yield, Function<I, O> function) {
        return new Processor<I, S, O>() {
            @Override
            public void process(Iterator<I> input, Yield<S, O> output, Function<I, O> function) {
                if (input.hasNext()) {
                    output.yield(function.apply(input.next()));
                }
            }
        };
    }
}
