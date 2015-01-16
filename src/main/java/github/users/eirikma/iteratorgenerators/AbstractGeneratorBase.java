package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Created by Eirik on 16.01.2015.
 */
public abstract class AbstractGeneratorBase<O, C> implements RepeatableIterator<O>, Iterable<O>, PushBackIterator<O> {
    protected final Holder<C> context = new Holder<>();
    protected final LinkedList<O> output = new LinkedList<>();
    protected final Stack<O> pushbacks = new Stack<>();
    protected long counter = 0L;
    protected GeneratorState<O, C> state;

    public AbstractGeneratorBase(C context) {
        this.context.set(context);
    }

    protected void reInitialize() {
        pushbacks.clear();
        counter = 0L;
        state = new GeneratorState<O, C>() {
            O last = null;
            @Override
            public void yield(O value) {
                last = value;
                output.addLast(value);
            }

            public O last() {
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
        reInitializeGenerator();
    }

    protected abstract void reInitializeGenerator();

    @Override
    public Iterator<O> iterator() {
        //DONT USE Iterable as RepatableIterator!   reInitialize();
        return this;
    }

    @Override
    public boolean hasNext() {
        if (pushbacks.size() > 0) return true;
        if (output.size() > 0) return true;
        counter++;
        invokeGenerator();
        return output.size() > 0;
    }

    protected abstract void invokeGenerator();

    @Override
    public O next() {

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
    public void pushback(O element) {
        pushbacks.push(element);
    }

    class Holder<X> {
        X x;
        X get(){return x;}
        void set(X value) {x = value;}
    }
}
