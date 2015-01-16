package github.users.eirikma.iteratorgenerators;

class InputProcessorImpl<C, I, O>  extends AbstractGeneratorBase<O,C> {

    private final InputProcessor<I,O,C> generator;
    private final PushBackIterator<I> input;

    InputProcessorImpl(PushBackIterator<I> input, InputProcessor<I, O, C> p) {
        this(null, input, p);
    }

    InputProcessorImpl(C context, PushBackIterator<I> input, InputProcessor<I, O, C> p) {
        super(context);
        this.input = input;
        generator = p;
        reInitialize();
    }

    @Override
    protected void reInitializeGenerator() {
        try {
            generator.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void invokeGenerator() {
        try {
            generator.nextValue(input, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
