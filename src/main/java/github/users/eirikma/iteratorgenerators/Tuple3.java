package github.users.eirikma.iteratorgenerators;

/**
 * tuple of 3 different values of the same or different type
 */
public class Tuple3<A,B,C> extends Tuple2<A, B> {

    protected final C third;

    public Tuple3(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }


    @Override
    public String toString() {
        return "Tuple3{" +
                first +
                "," + second +
                "," + third +
                '}';

    }
}
