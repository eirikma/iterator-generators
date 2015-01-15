package github.users.eirikma.iteratorgenerators;

public interface InputProcessor<I, O, C> {

    public void nextValue(PushBackIterator<I> input, GeneratorState<O,C> output) throws Exception;

    public default void initialize() throws Exception {};

}
