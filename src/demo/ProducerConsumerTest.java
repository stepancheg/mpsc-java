package demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ProducerConsumerTest {
    @Test
    public void random() throws Exception {
        AtomicBoolean failed = new AtomicBoolean();

        ArrayList<Integer> consumed = new ArrayList<>();

        ProducerConsumer<Integer> pc = new ProducerConsumer<>(consumed::add, e -> failed.set(true));

        List<Thread> threads = IntStream.range(0, 5).mapToObj(i -> {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10000; ++j) {
                    pc.enqueue(i + j * 5);
                }
            });
            thread.start();
            return thread;
        }).collect(Collectors.toList());

        for (Thread thread : threads) {
            thread.join();
        }

        pc.shutdown();
        pc.join();

        Assertions.assertEquals(50000, consumed.size());
        consumed.sort(Comparator.comparingInt(i -> i));

        for (int i = 0; i != 50000; ++i) {
            Assertions.assertEquals(i, consumed.get(i).intValue());
        }
    }

}
