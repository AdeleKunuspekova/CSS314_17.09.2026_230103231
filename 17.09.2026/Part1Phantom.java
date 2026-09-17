import java.util.concurrent.ThreadLocalRandom;

/**
Part 1: The Phantom Bug 
• Write a program that tosses $50,000,000$ random $(x, y)$ points into a $1 \times 1$ 
square to approximate $\pi$ ($\pi \approx 4 \times \frac{\text{hits}}{\text{total}}$).
• Split the loop across 4 native Java threads.
• Have all 4 threads update a shared variable: static long totalHits = 0; via totalHits++.
• Deliverable: Run it 5 times. Record the value of $\pi$.
• The Lesson: The code runs fast, but gives $\pi \approx 1.8$ to $2.4$ because threads 
overwrite each other's memory writes (data race).
 */
public class Part1Phantom {

    static long totalHits = 0;

    static final long TOTAL_POINTS = 50_000_000L;
    static final int NUM_THREADS = 4;

    public static void main(String[] args) throws InterruptedException {
        long pointsPerThread = TOTAL_POINTS / NUM_THREADS;

        Thread[] threads = new Thread[NUM_THREADS];
        for (int t = 0; t < NUM_THREADS; t++) {
            threads[t] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (long i = 0; i < pointsPerThread; i++) {
                    double x = rnd.nextDouble();
                    double y = rnd.nextDouble();
                    if (x * x + y * y <= 1.0) {
                        totalHits++;  
                    }
                }
            });
        }

        long start = System.nanoTime();
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        double pi = 4.0 * totalHits / (double) TOTAL_POINTS;
        System.out.printf("totalHits=%d  pi≈%.6f  time=%dms%n", totalHits, pi, elapsedMs);
    }
}
