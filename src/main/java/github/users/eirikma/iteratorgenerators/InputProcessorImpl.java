package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

class InputProcessorImpl<I,O> implements Iterator<O>, Iterable<O>, PushBackIterator<O> {
    private GeneratorOutput<O> state;
    private long counter = 0L;
    private final LinkedList<O> output = new LinkedList<>();
    private final Stack<O> pushbacks = new Stack<>();
    private final PushBackIterator<I> input;
    private final InputProcessor<I,O> generator;

    InputProcessorImpl(PushBackIterator<I> input, InputProcessor<I, O> p) {
        this.input = input;
        generator = p;
        reInitialize();
    }

    private void reInitialize() {
        counter = 0L;
        state = new GeneratorOutput<O>() {
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
        generator.nextValue(input, state);
        return output.size() > 0;
    }

    @Override
    public O next() {
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
    public void pushback(O element) {
        pushbacks.push(element);
    }

}
