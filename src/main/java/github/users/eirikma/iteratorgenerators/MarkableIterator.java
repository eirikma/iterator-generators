package github.users.eirikma.iteratorgenerators;

import java.io.InputStream;

/**
 * Interface for iterators where you can set a mark in the object stream and go back to that point in the stream later,
 * just like with mark(int) / reset() in java.io.InputStream
 *
 * @param <T> the type of the elements iterated by the source iterator.
 */
public interface MarkableIterator<T> extends IteratorExt<T> {

    /**
     * set the go-back-mark here, clearing any previous mark. Set also the maximum number of items that can be
     * read before the mark is automatically removed.
     *
     * Same as 'mark' in java.io.InputStream   for input streams that supports 'mark'
     *
     * @param maxReadaheadLimit
     */
    void mark(int maxReadaheadLimit);

    /**
     * go back to the last posisioned mark.
     */
    void reset();

}
