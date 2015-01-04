package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;

/**
 * Interface for iterators where you can 'undo' the next()-operation by
 * pushing back values to the iterator if you discover you have iterated too far.
 * Implemented by a utility function in the iterator class.
 *
 * @param <T> the type of the elements iterated by the source iterator.
 */
public interface PushBackIterator<T> extends Iterator<T> {
    public void pushback(T element);
}
