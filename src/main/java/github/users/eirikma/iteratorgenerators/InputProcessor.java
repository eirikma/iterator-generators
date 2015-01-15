package github.users.eirikma.iteratorgenerators;

public interface InputProcessor<I, O, C> {

    public void nextValue(PushBackIterator<I> input, GeneratorState<O,C> output);

    public default void initialize(){};

}
