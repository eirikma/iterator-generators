package github.users.eirikma.iteratorgenerators;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Map;

import static github.users.eirikma.iteratorgenerators.Maps.entry;
import static github.users.eirikma.iteratorgenerators.Maps.map;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MapsTest {

    @Test
    public void testMapEntries() throws Exception {
        Map<String, Integer> map = map(entry("A", 1), entry("B", 2));

        assertThat(map.keySet(), hasItems("A", "B"));
        assertThat(map.values(), hasItems(1, 2));
        assertThat(map.get("A"), is(1));
        assertThat(map.get("B"), is(2));
    }
}