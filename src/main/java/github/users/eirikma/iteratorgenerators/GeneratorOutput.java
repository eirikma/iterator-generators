package github.users.eirikma.iteratorgenerators;

public interface GeneratorOutput<T> {
    void yield(T value);
    public T last();
}
