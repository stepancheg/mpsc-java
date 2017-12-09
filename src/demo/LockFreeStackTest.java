package demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LockFreeStackTest {
    @Test
    void simple() {
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        stack.enqueue(10);
        stack.enqueue(20);
        stack.enqueue(30);
        stack.enqueue(40);

        Object[] dequeue = stack.dequeueAll();
        Integer[] expected = new Integer[] { 10, 20, 30, 40 };
        Assertions.assertArrayEquals(expected, dequeue);
    }

    @Test
    void dequeueEmpty() {
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        Assertions.assertEquals(0, stack.dequeueAll().length);
    }
}
