package github.users.eirikma.iteratorgenerators;

import java.util.Iterator;
import java.util.function.BiConsumer;

public interface PipelineBuilder {
    interface SourceSpecifier<T> {
        <O> PipelineStage<T, O> from(Iterator<T> source);
    }
    interface PipelineStage<I, O> {
        PipelineStage<I, O> to(BiConsumer<IteratorExt<I>, Yield<O>> consumer);

        IteratorExt<O> build() ;
    }
}
