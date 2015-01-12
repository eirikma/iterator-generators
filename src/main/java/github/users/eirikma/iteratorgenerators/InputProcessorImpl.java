package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

class InputProcessorImpl<I,O> implements Iterator<O>, Iterable<O> {
    private GeneratorOutput<O> state;
    private final LinkedList<O> output = new LinkedList<>();
    private final PushBackIterator<I> input;
    private final InputProcessor<I,O> generator;

    InputProcessorImpl(PushBackIterator<I> input, InputProcessor<I, O> p) {
        this.input = input;
        generator = p;
        reInitialize();
    }

    private void reInitialize() {
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
        if (output.size() > 0) return true;
        generator.nextValue(input, state);
        return output.size() > 0;
    }

    @Override
    public O next() {
        if (hasNext()) {
            return output.removeFirst();
        }
        throw new NoSuchElementException("'next'");
    }

}
