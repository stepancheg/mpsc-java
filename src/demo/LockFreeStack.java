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
        for (;;) {
            LockFreeStack.Node<T> head = stack.get();
            newHead.next = head;
            newHead.payload = element;
            newHead.size = head != null ? head.size + 1 : 1;
            if (stack.weakCompareAndSet(head, newHead)) {
                return;
            }
        }
    }

    private static final Object[] emptyArray = new Object[0];

    public Object[] dequeueAllReverseOrder() {
        Node<T> node = stack.getAndSet(null);
        if (node == null) {
            return emptyArray;
        }

        int size = node.size;
        T[] array = (T[]) new Object[size];

        for (int i = 0; i != size; ++i) {
            array[i] = node.payload;
            node = node.next;
        }

        return array;
    }

    public Object[] dequeue() {
        Object[] array = dequeueAllReverseOrder();
        reverseArray(array);
        return array;
    }

    private static <T> void reverseArray(T[] array) {
        for (int i = 0; i != array.length / 2; ++i) {
            T tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
    }
}
