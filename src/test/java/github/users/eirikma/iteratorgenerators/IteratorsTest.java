package github.users.eirikma.iteratorgenerators;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static github.users.eirikma.iteratorgenerators.Iterators.*;
import static github.users.eirikma.iteratorgenerators.Maps.entry;
import static github.users.eirikma.iteratorgenerators.Maps.map;
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
    public void repeatShouldRepeatIterations() {
//        assertThat(collect(repeat(values("a", "b"), 3)), is(asList("a", "b", "a", "b", "a", "b")));
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


//    @Test
//    public void testInputProcessorShouldTransformInput() throws Exception {
//        assertThat(
//                collect(inputProcessor(values(1, 2, 3), (iterator, state) -> {
//                    if (iterator.hasNext()) {
//                        state.yield("value-" + iterator.next());
//                    }
//                })),
//                is(asList("value-1", "value-2", "value-3"))
//        );
//    }

//    @Test
//    public void testInputProcessorWithContextShouldTransformInput() throws Exception {
//        String context = "ctx";
//        assertThat(
//                collect(inputProcessor(context, values(1, 2, 3), (iterator, state) -> {
//                    // not so advanced use of context, but it could have been a database lookup or something.
//                    String cont = state.context();
//                    if (iterator.hasNext()) {
//                        state.yield("value-" + cont + "-" + iterator.next());
//                    }
//                })),
//                is(asList("value-ctx-1", "value-ctx-2", "value-ctx-3"))
//        );
//    }


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



    /** a somewhat long example to demonstrate the viability of the iterator approach:  parse some imaginary sql files in oracle sql plus syntax */
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