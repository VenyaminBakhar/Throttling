package queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import model.CurrencyPairRate;

public class UpdateOrAddCCYPairRateQueue implements CustomQueue<CurrencyPairRate> {
    private Queue<CurrencyPairRate> queue;
    private ReentrantLock locker;

    public UpdateOrAddCCYPairRateQueue() {
        this.queue = new LinkedList<>();
        this.locker = new ReentrantLock();
    }

    public void add(CurrencyPairRate pairRate) {
        locker.lock();

        try {
            updateOrAdd(pairRate);
        } finally {
            locker.unlock();
        }
    }

    public CurrencyPairRate poll() {
        locker.lock();

        CurrencyPairRate elem;

        try {
            elem = queue.poll();
        } finally {
            locker.unlock();
        }

        return elem;
    }

    private void updateOrAdd(CurrencyPairRate newPairRate) {
        boolean isUpdated = false;

        for (CurrencyPairRate pairRate : queue) {
            if (pairRate.getCcyPair().equals(newPairRate.getCcyPair())) {
                pairRate.setRate(newPairRate.getRate());
                isUpdated = true;
                break;
            }
        }

        if (!isUpdated) {
            queue.add(newPairRate);
        }
    }
}
