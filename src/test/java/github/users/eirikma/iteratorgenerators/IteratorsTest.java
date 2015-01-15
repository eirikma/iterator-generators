package github.users.eirikma.iteratorgenerators;

import org.junit.Test;

import java.util.Iterator;

import static github.users.eirikma.iteratorgenerators.Iterators.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IteratorsTest {


    @Test
    public void testValuesShouldIterateAllValues() throws Exception {
        assertThat(values('a', 'b', 'c'), instanceOf(Iterator.class));
        assertThat(values('a', 'b').hasNext(), is(true));
        assertThat(values('a').next(), is('a'));
    }

    @Test
    public void testEachOfShouldCreateIterable() throws Exception {
        assertThat(eachOf(values('a', 'b', 'c')), instanceOf(Iterable.class));
    }

    @Test
    public void testCollectShouldCreateCollectionFromIterator() {
        assertThat(collect(values("A", "B")), is(asList("A", "B")));
    }

    @Test
    public void testGeneratorShouldIterateAllValuesProduced() throws Exception {
        RepeatableIterator<String> generator = generator(state -> {
            if ( state.invocationNumber() < 5) {
                state.yield("test-" + state.invocationNumber());
            }
        });
        assertThat(collect(generator), is(asList("test-1", "test-2", "test-3", "test-4")));
    }

    @Test
    public void generatorShouldBeAbleToIterateSeveralTimesWithReinitializationInClosure() {
        RepeatableIterator<String> generator = generator(new Generator<String>() {
            @Override
            public void initialize() {
                count = 0;
            }

            int count = 0;
            @Override
            public void nextValue(GeneratorOutput state) {
                if ( count++ < 4) {
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
        fail("not implemented");
    }

}