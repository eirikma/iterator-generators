package github.users.eirikma.iteratorgenerators;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

public class Pipelines {

    public static <I> PipelineBuilder.SourceSpecifier<I> sequentialPipeline() {
        return null;
    }


    public static <I> PipelineBuilder.SourceSpecifier<I> backgroundPipeline() {
//        return new SourceSpecifierImpl<I>();
        return null;
    }


    private interface StageSpec<T> {

    }
    private static class SourceSpecifierImpl<I> implements StageSpec<I>{

        public <O> PipelineBuilder.PipelineStage<I, O> from(Iterator<I> source) {
            return new PipelineBuilder.PipelineStage<I, O>() {
                @Override
                public PipelineBuilder.PipelineStage<I, O> to(BiConsumer<IteratorExt<I>, Yield<O>> consumer) {
                    return null;
                }

                @Override
                public IteratorExt<O> build() {
                    return null;
                }
            };
        }

    }

    private interface ConnectableStage<T> {
        void connect(Yield<T> outputDestination);
    }

    private static class SourceStage<T> implements ConnectableStage<T> {
        @Override
        public void connect(Yield<T> outputDestination) {
        }
    }
    private static class BackgroundThreadStage<I,O> implements ConnectableStage<O> {
        @Override
        public void connect(Yield<O> outputDestination) {
        }
    }
    private static class InlineStage<O> implements ConnectableStage<O> {
        @Override
        public void connect(Yield<O> outputDestination) {
        }
    }


    static class QueueYield<T> implements Yield<T> {
        private Queue<T> queue;
        private long yielded = 0L;
        private boolean closed = false;

        public QueueYield(Queue<T> queue) {
            this.queue = queue;
        }

        @Override
        public void yield(T value) {
            if (closed) {
                throw new IllegalStateException("is closed!");
            }
            queue.add(value);
            yielded++;
        }

        @Override
        public long count() {
            return yielded;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public int capacity() {
            if (closed) return 0; // or throw
            if (queue instanceof BlockingQueue) {
                BlockingQueue blockingQueue = (BlockingQueue) queue;
                return blockingQueue.remainingCapacity();
            };
            return Integer.MAX_VALUE - queue.size();
        }
    }
}
