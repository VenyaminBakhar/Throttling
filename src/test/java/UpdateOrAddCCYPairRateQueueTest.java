import org.junit.Before;
import org.junit.Test;

import model.CurrencyPairRate;
import queue.UpdateOrAddCCYPairRateQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UpdateOrAddCCYPairRateQueueTest {

    private UpdateOrAddCCYPairRateQueue queue;

    @Before
    public void setUp() {
        queue = new UpdateOrAddCCYPairRateQueue();
    }

    @Test
    public void poll_QueueWithElement_PollReturnElement() {
        //GIVEN
        CurrencyPairRate expectedPair = new CurrencyPairRate("EURUSD", 1.12);
        queue.add(expectedPair);

        //WHEN
        CurrencyPairRate actualPair = queue.poll();

        //THEN
        assertEquals(expectedPair, actualPair);
    }

    @Test
    public void poll_EmptyQueue_PollReturnNull() {
        //WHEN
        CurrencyPairRate actualPair = queue.poll();

        //THEN
        assertNull(actualPair);
    }

    @Test
    public void poll_QueueWithElements_PollReturnFirstAddedElement() {
        //GIVEN
        CurrencyPairRate firstElement = new CurrencyPairRate("EURUSD", 1.12);
        CurrencyPairRate secondElement = new CurrencyPairRate("EURRUB", 80.12);
        queue.add(firstElement);
        queue.add(secondElement);

        //WHEN
        CurrencyPairRate actualPair = queue.poll();

        //THEN
        assertEquals(firstElement, actualPair);
    }

    @Test
    public void add_EmptyQueue_AddedElementToQueue() {
        //GIVEN
        CurrencyPairRate addedPair = new CurrencyPairRate("EURUSD", 1.12);
        queue.add(addedPair);

        //WHEN
        CurrencyPairRate polledPair = queue.poll();

        //THEN
        assertEquals(addedPair, polledPair);
    }

    @Test
    public void add_QueueWithElementsOfSameCCYPair_UpdateCCYPairRate() {
        //GIVEN
        CurrencyPairRate ccyPair = new CurrencyPairRate("EURUSD", 1.12);
        CurrencyPairRate updatedCcyPair = new CurrencyPairRate("EURUSD", 80.12);
        queue.add(ccyPair);
        queue.add(updatedCcyPair);

        //WHEN
        CurrencyPairRate actualPair = queue.poll();

        //THEN
        assertEquals(updatedCcyPair, actualPair);
    }
}
