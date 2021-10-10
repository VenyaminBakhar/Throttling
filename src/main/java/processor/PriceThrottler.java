package processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.CurrencyPairRate;
import queue.CustomQueue;
import queue.UpdateOrAddCCYPairRateQueue;
import task.ProcessorUpdateTask;

/**
 * PriceThrottler class provides throttling for PriceProcessor interface implementations.
 *
 * PriceProcessor objects can subscribe on PriceThrottler via 'subscribe' method to start listen updates from it.
 * PriceProcessor objects can unsubscribe from PriceThrottler via 'unsubscribe' method to stop listen updates from it.
 *
 * Instead of update all subscribers one by one in a single thread PriceThrottler does it asynchronously.
 * It uses thread pool for that purpose. Each PriceProcessor has it's own updates queue. When an update appears
 * PriceThrottler adds it to a queue for each processor and if there is no thread working with a processor queue it
 * starts a new one.
 *
 * To solve for slow processors PriceThrottler uses UpdateOrAddCCYPairRateQueue. This queue implementation either
 * updates a task information in a processor queue or adds it to the end of the queue in case when there is no same type
 * of task in the queue.
 *
 * So when slow processor is still busy with his current task and there are several new tasks of a same type were
 * added to the queue it will contain just one task of that type with a latest updates.
 *
 * To use UpdateOrAddCCYPairRateQueue as a processor task queue subscribe on PriceThrottler via
 * 'subscribe(PriceProcessor priceProcessor)' method.
 * To use your own queue implementation subscribe with a
 * 'subscribe(PriceProcessor priceProcessor, CustomQueue<CurrencyPairRate> queue)' method.
 *
 */

public class PriceThrottler implements PriceProcessor {
    private final Map<PriceProcessor, CustomQueue<CurrencyPairRate>> processorToTaskQueue;
    private final Map<PriceProcessor, Future<?>> processorToCurrentTask;
    private final ExecutorService executor;

    public PriceThrottler() {
        processorToTaskQueue = new ConcurrentHashMap<>();
        processorToCurrentTask = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        for (Map.Entry<PriceProcessor, CustomQueue<CurrencyPairRate>> processorToQueue : processorToTaskQueue.entrySet()) {
            PriceProcessor processor = processorToQueue.getKey();
            CustomQueue<CurrencyPairRate> processorQueue = processorToQueue.getValue();

            processorQueue.add(new CurrencyPairRate(ccyPair, rate));

            if (!isRunning(processor)) {
                runUpdate(processor, processorQueue);
            }
        }
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        processorToTaskQueue.put(priceProcessor, new UpdateOrAddCCYPairRateQueue());
    }

    public void subscribe(PriceProcessor priceProcessor, CustomQueue<CurrencyPairRate> queue) {
        processorToTaskQueue.put(priceProcessor, queue);
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        processorToTaskQueue.remove(priceProcessor);
        processorToCurrentTask.remove(priceProcessor);
    }

    public void shutdown() {
        executor.shutdown();
        processorToTaskQueue.clear();
        processorToCurrentTask.clear();
    }

    private boolean isRunning(PriceProcessor processor) {
        Future<?> currentRunTask = processorToCurrentTask.get(processor);
        return currentRunTask != null && !currentRunTask.isDone();
    }

    private void runUpdate(PriceProcessor processor, CustomQueue<CurrencyPairRate> queue) {
        Future<?> task = executor.submit(new ProcessorUpdateTask(processor, queue));
        processorToCurrentTask.put(processor, task);
    }
}
