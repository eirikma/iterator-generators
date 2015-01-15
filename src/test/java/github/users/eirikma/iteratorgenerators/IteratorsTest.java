package github.users.eirikma.iteratorgenerators;

import org.junit.Test;

import java.util.Iterator;

import static github.users.eirikma.iteratorgenerators.Iterators.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IteratorsTest {


    @Test
    public void testValuesShouldIterateAllValues() throws Exception {

        assertThat(values('a', 'b', 'c'), instanceOf(Iterator.class));
        assertThat(values('a', 'b').hasNext(), is(true));
        assertThat(values('a').next(), is('a'));

        // typical iterator usage with 'while' loop
        Iterator<String> numbers = values("1", "2", "3");
        while(numbers.hasNext()) {
            int number = Integer.parseInt(numbers.next());
            // do something with 'number'
            assertThat(number > 0, is(true));
        }
    }

    @Test
    public void testEachOfShouldCreateIterable() throws Exception {

        assertThat(eachOf(values('a', 'b', 'c')), instanceOf(Iterable.class));

        // typical iterator/iterable usage with 'for' loop:
        Iterator<String> values = values("A", "B", "C");
        for(String v: eachOf(values)) {
            // do something with value v
            assertThat(v.length(), is(1));
        }
    }

    @Test
    public void testCollectShouldCreateCollectionFromIterator() {
        assertThat(collect(values("A", "B")), is(asList("A", "B")));
    }

    @Test
    public void testGeneratorShouldIterateAllValuesProduced() throws Exception {
        RepeatableIterator<String> generator = generator(state -> {
            if (state.invocationNumber() < 5) {
                state.yield("test-" + state.invocationNumber());
            }
        });
        assertThat(collect(generator), is(asList("test-1", "test-2", "test-3", "test-4")));
    }

    @Test
    public void generatorShouldBeAbleToIterateSeveralTimesWithReinitializationInClosure() {
        RepeatableIterator<String> generator = generator(new Generator<String, Void>() {
            int count = 0;
            @Override
            public void initialize() {
                count = 0;
            }
            @Override
            public void nextValue(GeneratorState<String, Void> state) {
                if (++count < 5) {
                    state.yield("test-" + count);
                }
            }
        });

        assertThat(collect(generator), is(asList("test-1", "test-2", "test-3", "test-4")));

        generator.reset();

        assertThat(collect(((Iterable<String>) generator).iterator()), is(asList("test-1", "test-2", "test-3", "test-4")));
    }

    @Test
    public void repeatShouldRepeatIterations() {
        assertThat(collect(repeat(values("a", "b"), 3)), is(asList("a", "b", "a", "b", "a", "b")));
    }


    @Test
    public void testPushbackableShouldCreateIteratorSupportingPushback() throws Exception {

        // assert that it works as an ordinary iterator
        PushBackIterator<String> pushBackIterator = pushbackable(values("A", "B", "C"));
        String first = pushBackIterator.next();
        assertThat(first, is("A"));
        assertThat(collect(pushBackIterator), is(asList("B", "C")));

        // assert that it can push back as well
        pushBackIterator = pushbackable(values("A", "B", "C"));
        first = pushBackIterator.next();
        assertThat(first, is("A"));
        pushBackIterator.pushback(first);
        assertThat(collect(pushBackIterator), is(asList("A", "B", "C")));
    }


    @Test
    public void testInputProcessorShouldTransformInput() throws Exception {
        assertThat(
                collect(inputProcessor(values(1, 2, 3), (iterator, state) -> {
                    if (iterator.hasNext()) {
                        state.yield("value-" + iterator.next());
                    }
                })),
                is(asList("value-1", "value-2", "value-3"))
        );
    }

    @Test
    public void testInputProcessorWithContextShouldTransformInput() throws Exception {
        String context = "ctx";
        assertThat(
                collect(inputProcessor(context, values(1, 2, 3), (iterator, state) -> {
                    // not so advanced use of context, but it could have been a database lookup or something.
                    String cont = state.context();
                    if (iterator.hasNext()) {
                        state.yield("value-" + cont + "-" + iterator.next());
                    }
                })),
                is(asList("value-ctx-1", "value-ctx-2", "value-ctx-3"))
        );
    }

}