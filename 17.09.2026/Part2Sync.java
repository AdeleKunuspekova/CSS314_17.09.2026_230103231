import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
Part 2: The Synchronization Trap 
• Fix the race condition by adding synchronized to the increment or using 
AtomicLong.incrementAndGet().
• Deliverable: Measure execution time vs. a plain, single-threaded for loop.
• The Lesson: The answer is now accurate ($\approx 3.1415$), but it runs drastically 
slower than a single thread because cores spend 95% of their cycles stalling on 
memory bus locks.
 */
public class Part2Sync {

    static final long TOTAL_POINTS = 50_000_000L;
    static final int NUM_THREADS = 4;

    public static void main(String[] args) throws InterruptedException {
        // ---- Multi-threaded, atomic (correct) ----
        AtomicLong totalHits = new AtomicLong(0);
        long pointsPerThread = TOTAL_POINTS / NUM_THREADS;

        Thread[] threads = new Thread[NUM_THREADS];
        for (int t = 0; t < NUM_THREADS; t++) {
            threads[t] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (long i = 0; i < pointsPerThread; i++) {
                    double x = rnd.nextDouble();
                    double y = rnd.nextDouble();
                    if (x * x + y * y <= 1.0) {
                        totalHits.incrementAndGet();   
                    }
                }
            });
        }

        long startMulti = System.nanoTime();
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();
        long multiMs = (System.nanoTime() - startMulti) / 1_000_000;

        double piMulti = 4.0 * totalHits.get() / (double) TOTAL_POINTS;

        long singleHits = 0;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long startSingle = System.nanoTime();
        for (long i = 0; i < TOTAL_POINTS; i++) {
            double x = rnd.nextDouble();
            double y = rnd.nextDouble();
            if (x * x + y * y <= 1.0) {
                singleHits++;
            }
        }
        long singleMs = (System.nanoTime() - startSingle) / 1_000_000;
        double piSingle = 4.0 * singleHits / (double) TOTAL_POINTS;

        System.out.printf("[4-thread AtomicLong] hits=%d  pi≈%.6f  time=%dms%n",
                totalHits.get(), piMulti, multiMs);
        System.out.printf("[1-thread plain long ] hits=%d  pi≈%.6f  time=%dms%n",
                singleHits, piSingle, singleMs);
        System.out.printf("Atomic version is %.2fx the single-thread time (slower if >1)%n",
                multiMs / (double) singleMs);
    }
}
