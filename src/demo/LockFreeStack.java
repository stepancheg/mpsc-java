package demo;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack<T> {
    private AtomicReference<Node<T>> stack = new AtomicReference<>(null);

    private static class Node<T> {
        /** Next element in the stack */
        Node<T> next;
        /** Null is shutdown */
        T payload;
        /** Size of the stack */
        int size;
    }

    public void enqueue(T element) {
        LockFreeStack.Node<T> newHead = new LockFreeStack.Node<>();
        newHead.payload = element;

        for (;;) {
            LockFreeStack.Node<T> head = stack.get();
            newHead.next = head;
            newHead.size = head != null ? head.size + 1 : 1;
            if (stack.weakCompareAndSet(head, newHead)) {
                return;
            }
        }
    }

    private static final Object[] emptyArray = new Object[0];

    public Object[] dequeueAll() {
        Node<T> node = stack.getAndSet(null);
        if (node == null) {
            return emptyArray;
        }

        int size = node.size;
        T[] array = (T[]) new Object[size];

        for (int i = 0; i != size; ++i) {
            array[size - 1 - i] = node.payload;
            node = node.next;
        }

        return array;
    }

}
