package task;

import model.CurrencyPairRate;
import processor.PriceProcessor;
import queue.CustomQueue;

public class ProcessorUpdateTask implements Runnable {
    private PriceProcessor processor;
    private CustomQueue<CurrencyPairRate> queue;

    public ProcessorUpdateTask(PriceProcessor processor, CustomQueue<CurrencyPairRate> queue) {
        this.processor = processor;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            CurrencyPairRate pairRate = queue.poll();

            if (pairRate == null) {
                break;
            }

            processor.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }
    }
}
