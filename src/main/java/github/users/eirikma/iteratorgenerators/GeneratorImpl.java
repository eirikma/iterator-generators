package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

class GeneratorImpl<T,C> implements RepeatableIterator<T>, Iterable<T>, PushBackIterator<T> {
    class Holder<T> {
        T t;
        T get(){return t;}
        void set(T value) {t = value;}
    }
    private final Holder<C> context = new Holder<>();
    private long counter = 0L;
    private GeneratorState<T,C> state;
    private final LinkedList<T> output = new LinkedList<>();
    private final Stack<T> pushbacks = new Stack<>();
    private final Generator<T,C> generator;

    GeneratorImpl(Generator<T,C> g, C context) {
        generator = g;
        this.context.set(context);
        reInitialize();
    }
    GeneratorImpl(Generator<T,C> g) {
        this(g, null);
    }

    private void reInitialize() {
        pushbacks.clear();
        counter = 0L;
        state = new GeneratorState<T,C>() {
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

            @Override
            public C context() {
                return context.get();
            }
            @Override
            public void setContext(C newValue) {
                context.set(newValue);
            }

        };
        try {
            generator.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        counter++;
        try {
            generator.nextValue(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output.size() > 0;
    }

    @Override
    public T next() {

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
