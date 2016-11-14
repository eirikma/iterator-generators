package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;

/**
 * Interface for iterators that can be 'reset', so that you can iterate ovethe elements from the beginning again.
 * Not all iterators supports this Some of the same functionality is provided via the java.util.Iterable interface as well.
 *
 */
@Deprecated
public interface RepeatableIterator<T> extends Iterator<T> {
    public void reset();
}
