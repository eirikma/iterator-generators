package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

class GeneratorImpl<T> implements RepeatableIterator<T>, Iterable<T>, PushBackIterator<T> {
    private long counter = 0L;
    private GeneratorOutput<T> state;
    private final LinkedList<T> output = new LinkedList<>();
    private final Stack<T> pushbacks = new Stack<>();
    private final Generator<T> generator;

    GeneratorImpl(Generator<T> g) {
        generator = g;
        reInitialize();
    }

    private void reInitialize() {
        pushbacks.clear();
        counter = 0L;
        state = new GeneratorOutput<T>() {
            T last = null;
            @Override
            public void yield(T value) {
                last = value;
                output.addLast(value);
            }

            public T last() {
                return last;
            }

            @Override
            public long invocationNumber() {
                return counter;
            }
        };
        generator.initialize();
    }

    @Override
    public Iterator<T> iterator() {
        //DONT USE Iterable as RepatableIterator!   reInitialize();
        return this;
    }

    @Override
    public boolean hasNext() {
        if (pushbacks.size() > 0) return true;
        if (output.size() > 0) return true;
        generator.nextValue(state);
        return output.size() > 0;
    }

    @Override
    public T next() {
        counter++;
        if (hasNext()) {
            if (pushbacks.size() > 0) {
                return pushbacks.pop();
            }
            return output.removeFirst();
        }
        throw new NoSuchElementException("'next'");
    }

    @Override
    public void reset() {
        reInitialize();
    }

    @Override
    public void pushback(T element) {
        pushbacks.push(element);
    }
}
