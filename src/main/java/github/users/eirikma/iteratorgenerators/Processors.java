package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.function.Function;

public class Processors {

    public static <I, O>  Processor<I,O> transform(Iterator<I> input, Yield<O> yield, Function<I, O> function) {
        return new Processor<I, O>() {
            @Override
            public void process(Iterator<I> input, Yield<O> output, Function<I, O> function) {
                if (input.hasNext()) {
                    output.yield(function.apply(input.next()));
                }
            }
        };
    }
}
