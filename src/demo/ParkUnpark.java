package demo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class ParkUnpark {
    public static void main(String[] args) throws Exception {
        AtomicInteger count = new AtomicInteger();
        AtomicBoolean shutdown = new AtomicBoolean();

        Thread t1 = new Thread(() -> {
            int i = 0;
            while (!shutdown.get()) {
                LockSupport.park();
                ++i;
            }
            count.set(i);
        });
        t1.start();

        ArrayList<Thread> wakers = new ArrayList<>();
        for (int i = 0; i != 5; ++i) {
            Thread t2 = new Thread(() -> {
                for (int j = 0; j != 10_000; ++j) {
                    LockSupport.unpark(t1);
                }
            });
            wakers.add(t2);
        }

        for (Thread waker : wakers) {
            waker.start();
        }


        for (Thread waker : wakers) {
            waker.join();
        }

        shutdown.set(true);
        LockSupport.unpark(t1);
        t1.join();

        System.out.println(count);
    }
}
