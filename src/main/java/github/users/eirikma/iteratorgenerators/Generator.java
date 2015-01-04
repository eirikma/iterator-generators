package github.users.eirikma.iteratorgenerators;

/**
 * interface for Generator lambda
 */
public interface Generator<T> {
    public void nextValue(GeneratorState<T> state);

    public default void initialize(){};
}