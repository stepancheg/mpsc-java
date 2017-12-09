package demo;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class ProducerConsumer<T> {

    private final Consumer<T> consumer;
    private final Consumer<Throwable> uncaughtExceptionHandler;

    private final LockFreeStack<T> lockFreeStack = new LockFreeStack<>();

    private final Thread consumerThread;

    public ProducerConsumer(Consumer<T> consumer, Consumer<Throwable> uncaughtExceptionHandler) {
        this.consumer = consumer;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        consumerThread = new Thread(this::runConsumer, ProducerConsumer.class.getSimpleName());
        consumerThread.start();
    }

    public void enqueue(T element) {
        if (element == null) {
            throw new NullPointerException();
        }

        lockFreeStack.enqueue(element);
        LockSupport.unpark(consumerThread);
    }

    public void shutdown() {
        lockFreeStack.enqueue(null);
        LockSupport.unpark(consumerThread);
    }

    public void join() {
        try {
            consumerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runConsumer() {
        for (;;) {
            LockSupport.park();
            T[] elements = (T[]) lockFreeStack.dequeue();
            for (T element : elements) {
                if (element == null) {
                    return;
                }

                try {
                    consumer.accept(element);
                } catch (Throwable e) {
                    uncaughtExceptionHandler.accept(e);
                }
            }
        }
    }
}
