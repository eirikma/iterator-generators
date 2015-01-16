package github.users.eirikma.iteratorgenerators;

class GeneratorImpl<O, C> extends AbstractGeneratorBase<O,C> {

    private final Generator<O,C> generator;

    GeneratorImpl(Generator<O,C> g, C context) {
        super(context);
        generator = g;
        reInitialize();
    }

    GeneratorImpl(Generator<O,C> g) {
        this(g, null);
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
            generator.nextValue(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
