package github.users.eirikma.iteratorgenerators;

import org.junit.Test;

import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by eirik on 16.11.2016.
 */
public class PipelinesTest {

    @Test
    public void sequentialPipeline() throws Exception {
        assertThat(Pipelines.sequentialPipeline().from(Iterators.values(1, 2, 3, 4, 5))
                .to( (in, out) -> {
                    if (in.hasNext()) {
                        out.yield("" + in.next());
                    }
                })
                .build().collect(Collectors.toList()),
        is(asList("1", "2", "3", "4", "5")));
    }

    @Test
    public void backgroundPipeline() throws Exception {

    }

}