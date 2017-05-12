package github.users.eirikma.iteratorgenerators;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static github.users.eirikma.iteratorgenerators.Iterators.*;
import static github.users.eirikma.iteratorgenerators.Maps.entry;
import static github.users.eirikma.iteratorgenerators.Maps.map;
import static github.users.eirikma.iteratorgenerators.Maps.reverseMap;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IteratorsTest {


    @Test
    public void testValuesShouldIterateAllValues() throws Exception {

        assertThat(values('a', 'b', 'c'), instanceOf(Iterator.class));
        assertThat(values('a', 'b').hasNext(), is(true));
        assertThat(values('a').next(), is('a'));

        // typical iterator usage with 'while' loop
        Iterator<String> numbers = values("1", "2", "3");
        while (numbers.hasNext()) {
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
        int count = 0;
        for (String v : eachOf(values)) {
            // do something with value v
            assertThat(v.length(), is(1));
            count++;
        }

        assertThat(count ,is(3));
    }

    @Test
    public void testCollectShouldCreateCollectionFromIterator() {
        assertThat(collect(values("A", "B")), is(asList("A", "B")));
    }

    @Test
    public void testGeneratorShouldIterateAllValuesProduced() throws Exception {
        Iterator<String> generator = generator(yield -> {
            if (yield.count() < 5) {
                yield.yield("test-" + yield.count());
            }
        });
        assertThat(collect(generator), is(asList("test-0", "test-1", "test-2", "test-3", "test-4")));
    }

    @Test
    public void generatorWithPreviousAndCountCanHoldState() {
        assertThat(collect(
                generatorWithState(0, yield -> {
                    if (yield.count() >= 4) {
                        return;
                    }
                    final Integer prev = yield.getState();
                    int value = prev + 1;
                    yield.yield("" + prev + "-is-followed-by-" + value);
                    yield.setState(value);
                })
                ),
                is(asList("0-is-followed-by-1", "1-is-followed-by-2", "2-is-followed-by-3", "3-is-followed-by-4")));
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
    public void testEmptyBackgroundIteratorShouldTerminateImmediately() {
        final IteratorExt<Object> background = background(yield -> {
            // nada
        });

        final Collection<Object> values = collect(background);
        assertThat(values, is(asList()));
    }

    @Test
    public void testBackgroundIteratorShouldNotLoseObjects() {
        final long GENERATOR_MAX = 20L * Short.MAX_VALUE;
        IteratorExt<Long> longSource = background((yield) -> {
            for (long i = 0, value = yield.count(); i < 3 && value < GENERATOR_MAX; i++) {
                yield.yield(++value);
            }
        });

        assertThat(collect(longSource).size(), is((int) GENERATOR_MAX ));

        Long maxValue = Iterators.stream(Iterators.<Long>background((yield) -> {
            for (long i = 0, value = yield.count(); i < 3 && value < GENERATOR_MAX; i++) {
                yield.yield(++value);
            }
        })).reduce(0L, Long::max);

        assertThat(maxValue, is(GENERATOR_MAX));

        Long count = Iterators.stream(Iterators.<Long>background((yield) -> {
            for (long i = 0, value = yield.count(); i < 3 && value < GENERATOR_MAX; i++) {
                yield.yield(++value);
            }
        })).reduce(0L, (l1, l2) -> l1 + 1);

        assertThat(count, is(GENERATOR_MAX));

    }



    @Test
    public void scenarioSimulatingDataMargeAndEnhancementAcrossSeveralDatabasesShouldRunSmooth() throws Exception {
        final long GENERATOR_MAX = 20_000;
        final long BATCH_SIZE_SOURCE = 1000L;
        final int BATCH_DELAY_MS = 200;

        // wait, then generate one batch of new numbers
        // simulates a long and slow db query returning lots if rows.
        IteratorExt<Long> batchSource = background((yield) -> {
            delayMs(BATCH_DELAY_MS);
            long startValue = yield.count();
            for (long i = startValue; i <  startValue + BATCH_SIZE_SOURCE && i < GENERATOR_MAX ; i++) {
                yield.yield(i);
            }
        });
        // wait, then generate one batch of new numbers
        // simulates looking up addidional data per row from the previous loop
        IteratorExt<Tuple3<Long, String, String>> enriched = background(batchSource, (input, yield) -> {
            if (input.hasNext()) {
                // consider batchning up a few rows
                delayMs(1);
                Long next = input.next();
                yield.yield(new Tuple3(next, Long.toString(next), Long.toBinaryString(next)));
            }
        });

        Iterators.stream(enriched).forEach(System.out::println);
    }

    private synchronized void delayMs(int delay_ms) {
        try {
            wait(delay_ms);
        } catch (InterruptedException e) {
            // ignored
        }
    }


    @Test
    public void flattenShouldFlattenIterators() {
        assertThat(collect(
                flatten(values(
                        values(),
                        values("a", "b", "c"),
                        values("d"),
                        values(),
                        values("e", "f")
                ))
                ),
                is(asList("a", "b", "c", "d", "e", "f"))
        );
    }


    /**
     * a somewhat long example to demonstrate the viability of the iterator approach:  parse some imaginary sql files in oracle sql plus syntax
     */
    @Test
    @Ignore
    public void parseComplexInputInPipelineShouldBeShortAndReadableCode() throws Exception {

        // an imaginary set of sql files (with file contents). lets keep'em here for readability
        Map<String, String> sqlFiles = map(
                entry("install-all-prod.sql", "\n"
                        + "-- setup file for prod \n"
                        + "define app_env 'prod'; \n"
                        + "@setup-all.sql;\n"
                        + ""),

                entry("setup-all.sql", "\n"
                        + "@create_users.sql;\n"
                        + "@create_schema.sql;\n"
                        + "@insert-config-data-&app_env..sql;\n"
                        + ""),

                entry("create-users.sql", "\n"
                        + " define APP_USER 'APPLICATION_&app_env.' ; \n"
                        + "\n"
                        + "create user &APP_USER. identified by &APP_USER. ; \n"
                        + "grant resource to &APP_USER. ;\n"
                        + ""),

                entry("create-schema.sql", "-- set up application tables . \n"
                        + "\n"
                        + "-- bloody table \n"
                        + "create table fil ("
                        + "  id number(19,0) primary key,"
                        + "  fileName varchar2(255) not null,"
                        + "  c"),

                entry("insert-config-data-prod.sql", "")
        );

    }

}