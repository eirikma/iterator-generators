package github.users.eirikma.iteratorgenerators;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MarkableIteratorTest {

    @Test
    public void shouldMarkAndReturnForSimpleSequence() throws Exception {

        MarkableIterator<Integer> markable = Iterators.markable(Iterators.values(1, 2, 3, 4, 5, 6, 7));

        assertThat(markable.hasNext(), is(true));
        assertThat(markable.next(), is(1));
        assertThat(markable.hasNext(), is(true));
        assertThat(markable.next(), is(2));
        assertThat(markable.hasNext(), is(true));
        markable.mark(2);
        assertThat(markable.hasNext(), is(true));
        assertThat(markable.next(), is(3));
        assertThat(markable.hasNext(), is(true));
        assertThat(markable.next(), is(4));
        markable.reset();
        assertThat(markable.hasNext(), is(true));
        assertThat(markable.next(), is(3));
        markable.mark(1);
        assertThat(markable.next(), is(4));
        assertThat(markable.next(), is(5));
        try {
            markable.reset();
            fail("should have thrown");
        } catch (IllegalStateException e) {}
        assertThat(markable.next(), is(6));

    }
}