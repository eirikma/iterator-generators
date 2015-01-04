package github.users.eirikma.iteratorgenerators;

public interface GeneratorState<T> {
    void yield(T value);
    public T last();
}
