import java.util.concurrent.ThreadLocalRandom;

/**
Part 3: OpenMP-Style Reduction 
• Eliminate shared-state locks. Give each thread a private, local counter (conceptually 
identical to #pragma omp parallel for reduction(+:totalHits)).
• Accumulate local hits inside the thread, then add the partial sums together only once 
at the very end when joining threads.
• Deliverable: Benchmark total execution time for $T \in \{1, 2, 4, 8, 16, 32\}$ threads 
on their machine with $100,000,000$ iterations. 

 */
public class Part3Reduction {

    static final long TOTAL_POINTS = 100_000_000L;
    static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16, 32};

    static long runOnce(int numThreads) throws InterruptedException {
        Thread[] threads = new Thread[numThreads];
        final long[] partialHits = new long[numThreads];

        long base = TOTAL_POINTS / numThreads;
        long remainder = TOTAL_POINTS % numThreads;

        long start = System.nanoTime();
        for (int t = 0; t < numThreads; t++) {
            final int idx = t;
            final long iterations = base + (t < remainder ? 1 : 0);
            threads[t] = new Thread(() -> {
                long localHits = 0;                     
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (long i = 0; i < iterations; i++) {
                    double x = rnd.nextDouble();
                    double y = rnd.nextDouble();
                    if (x * x + y * y <= 1.0) {
                        localHits++;                     
                    }
                }
                partialHits[idx] = localHits;           
            });
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();

        long totalHits = 0;
        for (long h : partialHits) totalHits += h;       

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        double pi = 4.0 * totalHits / (double) TOTAL_POINTS;
        System.out.printf("T=%-2d  hits=%d  pi≈%.6f  time=%dms%n", numThreads, totalHits, pi, elapsedMs);
        return elapsedMs;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Warm-up run (JIT warm-up, not recorded)...");
        runOnce(4);
        System.out.println();

        long[] times = new long[THREAD_COUNTS.length];
        for (int i = 0; i < THREAD_COUNTS.length; i++) {
            times[i] = runOnce(THREAD_COUNTS[i]);
        }

        long t1 = times[0];
        System.out.println();
        System.out.println("Threads(T) | Runtime(ms) | Speedup (T1/TN) | Efficiency (Speedup/T)");
        System.out.println("-----------------------------------------------------------------");
        for (int i = 0; i < THREAD_COUNTS.length; i++) {
            int T = THREAD_COUNTS[i];
            double speedup = t1 / (double) times[i];
            double efficiency = speedup / T * 100.0;
            System.out.printf("%-10d | %-11d | %-16.2fx | %.1f%%%n", T, times[i], speedup, efficiency);
        }
    }
}
