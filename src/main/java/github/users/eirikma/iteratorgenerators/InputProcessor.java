package github.users.eirikma.iteratorgenerators;

public interface InputProcessor<I, O> {

    public void nextValue(PushBackIterator<I> input, GeneratorOutput<O> output);

    public default void initialize(){};

}
