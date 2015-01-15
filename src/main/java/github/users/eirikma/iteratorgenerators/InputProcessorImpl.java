package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

class InputProcessorImpl<C, I,O> implements Iterator<O>, Iterable<O>, PushBackIterator<O> {
    private C context;
    private GeneratorState<O,C> state;
    private long counter = 0L;
    private final LinkedList<O> output = new LinkedList<>();
    private final Stack<O> pushbacks = new Stack<>();
    private final PushBackIterator<I> input;
    private final InputProcessor<I,O,C> generator;

    InputProcessorImpl(PushBackIterator<I> input, InputProcessor<I, O, C> p) {
        this(null, input, p);
    }

    InputProcessorImpl(C context, PushBackIterator<I> input, InputProcessor<I, O, C> p) {
        this.context = context;
        this.input = input;
        generator = p;
        reInitialize();
    }

    private void reInitialize() {
        counter = 0L;
        state = new GeneratorState<O,C>() {
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
                return context;
            }
        };
        generator.initialize();
    }

    @Override
    public Iterator<O> iterator() {
        reInitialize();
        return this;
    }

    @Override
    public boolean hasNext() {
        if (pushbacks.size() > 0) return true;
        if (output.size() > 0) return true;
        counter++;
        generator.nextValue(input, state);
        return output.size() > 0;
    }

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
    public void pushback(O element) {
        pushbacks.push(element);
    }

}
