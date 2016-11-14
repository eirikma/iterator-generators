package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Created by emaus on 07.11.2016.
 */
public interface PipelineBuilder {
    interface SourceSpecifier<T> {
        PipelineStage from(Iterator<T> source);
    }
    interface PipelineStage<I, S, O> {
        PipelineStage<I, S, O> to(BiConsumer<Iterator<I>, Yield<O>> consumer);

        Iterator<O> build() ;
    }
}
