package github.users.eirikma.iteratorgenerators;

/**
 * interface for Generator lambda
 */
public interface Generator<T,C> {
    public void nextValue(GeneratorState<T,C> state) throws Exception;

    public default void initialize() throws Exception {};
}
