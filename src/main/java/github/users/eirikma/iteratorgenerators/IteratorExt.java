package github.users.eirikma.iteratorgenerators;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface IteratorExt<T> extends Iterator<T>, Closeable, Iterable<T> {

    @Override
    default Iterator<T> iterator() {
        return this;
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        for (T t : this) {
            action.accept(t);
        }
    }

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
    }

    /**
     * same as  'available()' in java.io.InputStream , but in terms of objects, not bytes :
     * the number of items that can be retrieved immediately without any blocking.
     * For iterators that iterate in-memory collections, this is of course the remaining number of elements.
     *
     * default:
     *  */
    default int available() {
        return hasNext() ? 1 : 0;
    }

    /**
     * Same as  'read(byte[] buffer, int offset, int count)' in java.io.InputStream, but in terms of objects not bytes:
     * retrieve many items into an array, and get the number of items back.
     *
     * @param buffer - destination
     * @param offset - start put at this index
     * @param count - maximum number of items to get
     * @return actually items gotten.
     */
    default int nextN(T[] buffer, int offset, int count) {
        // could also limit to 'available()'
        int toGet = Math.min(buffer.length - offset, count);
        int gotten = 0;
        for (int i = 0; i < toGet && hasNext(); i++) {
            buffer[i] = next();
            gotten++;
        }
        return gotten;
    }




    @Override
    default void close() throws IOException {
    }

    default boolean isClosed() {
        return ! hasNext();
    }

    /**
     * Guaranteed to throw an exception and leave the underlying data unmodified.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    default void remove() {
        throw new UnsupportedOperationException("Iterator.remove()");
    }


    default <R, A> R collect(Collector<? super T, A, R> collector){
        // code stolen from java stream
        A container = collector.supplier().get();
        BiConsumer<A, ? super T> accumulator = collector.accumulator();
        forEach(u -> accumulator.accept(container, u));
        return collector.finisher().apply(container);
    }
}
