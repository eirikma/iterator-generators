package github.users.eirikma.iteratorgenerators;

public interface GeneratorState<T,C> {
    void yield(T value);
    public T last();
    long invocationNumber();
    public C context();
    public void setContext(C newValue);
}
