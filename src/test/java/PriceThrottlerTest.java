import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import model.CurrencyPairRate;
import processor.PriceThrottler;
import util.processors.DelayedPriceProcessor;

public class PriceThrottlerTest {
    private PriceThrottler throttler;

    @Before
    public void setUp() {
        if (throttler != null) {
            throttler.shutdown();
        }

        throttler = new PriceThrottler();
    }

    @Test
    public void onPrice_ThrottlerHasFastSubscribers_SubscribersUpdated() throws InterruptedException {
        //GIVEN
        DelayedPriceProcessor firstFastProcessor = new DelayedPriceProcessor(0);
        DelayedPriceProcessor secondFastProcessor = new DelayedPriceProcessor(0);

        throttler.subscribe(firstFastProcessor);
        throttler.subscribe(secondFastProcessor);

        List<CurrencyPairRate> expectedUpdates = new ArrayList<>();

        expectedUpdates.add(new CurrencyPairRate("EURUSD", 1.12));
        expectedUpdates.add(new CurrencyPairRate("EURRUB", 80.3));


        //WHEN
        for (CurrencyPairRate pairRate : expectedUpdates) {
            throttler.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }

        //THEN
        while(throttler.isWorking()) {
            Thread.sleep(1000);
        }

        assertEquals(expectedUpdates, firstFastProcessor.getUpdates());
        assertEquals(expectedUpdates, secondFastProcessor.getUpdates());
    }

    @Test
    public void onPrice_ThrottlerHasSlowSubscribers_SubscribersUpdated() throws InterruptedException {
        //GIVEN
        DelayedPriceProcessor firstProcessor = new DelayedPriceProcessor(2000);
        DelayedPriceProcessor secondProcessor = new DelayedPriceProcessor(2000);

        throttler.subscribe(firstProcessor);
        throttler.subscribe(secondProcessor);

        List<CurrencyPairRate> expectedUpdates = new ArrayList<>();

        expectedUpdates.add(new CurrencyPairRate("EURUSD", 1.12));
        expectedUpdates.add(new CurrencyPairRate("EURRUB", 80.3));


        //WHEN
        for (CurrencyPairRate pairRate : expectedUpdates) {
            throttler.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }

        //THEN
        while(throttler.isWorking()) {
            Thread.sleep(1000);
        }

        assertEquals(expectedUpdates, firstProcessor.getUpdates());
        assertEquals(expectedUpdates, secondProcessor.getUpdates());
    }

    @Test
    public void onPrice_ThrottlerHasSlowAndFastSubscribers_SubscribersUpdated() throws InterruptedException {
        //GIVEN
        DelayedPriceProcessor slowSub = new DelayedPriceProcessor(2000);
        DelayedPriceProcessor fastSub = new DelayedPriceProcessor(0);

        throttler.subscribe(slowSub);
        throttler.subscribe(fastSub);

        List<CurrencyPairRate> expectedUpdates = new ArrayList<>();

        expectedUpdates.add(new CurrencyPairRate("EURUSD", 1.12));
        expectedUpdates.add(new CurrencyPairRate("EURRUB", 80.3));


        //WHEN
        for (CurrencyPairRate pairRate : expectedUpdates) {
            throttler.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }

        //THEN
        while(throttler.isWorking()) {
            Thread.sleep(1000);
        }

        assertEquals(expectedUpdates, slowSub.getUpdates());
        assertEquals(expectedUpdates, fastSub.getUpdates());
    }

    @Test
    public void onPrice_ThrottlerHasSlowAndFastSubscribers_FastSubscriberCompletedUpdateFirst() throws InterruptedException {
        //GIVEN
        DelayedPriceProcessor slowSub = new DelayedPriceProcessor(2000);
        DelayedPriceProcessor fastSub = new DelayedPriceProcessor(0);

        throttler.subscribe(slowSub);
        throttler.subscribe(fastSub);

        List<CurrencyPairRate> updates = new ArrayList<>();

        updates.add(new CurrencyPairRate("EURUSD", 1.12));
        updates.add(new CurrencyPairRate("EURRUB", 80.3));


        //WHEN
        for (CurrencyPairRate pairRate : updates) {
            throttler.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }

        //THEN
        while(throttler.isWorking()) {
            Thread.sleep(1000);
        }

        boolean isFastProcessorFinishedEarly = slowSub.getLastUpdateTimestamp() - fastSub.getLastUpdateTimestamp() > 0;

        assertTrue(isFastProcessorFinishedEarly);
        assertEquals(updates, slowSub.getUpdates());
        assertEquals(updates, fastSub.getUpdates());
    }

    @Test
    public void onPrice_ThrottlerHasSlowSubscriber_SlowSubUpdatedWithLatestCYYPairRate() throws InterruptedException {
        //GIVEN
        DelayedPriceProcessor slowSub = new DelayedPriceProcessor(2000);

        throttler.subscribe(slowSub);

        CurrencyPairRate eurusd = new CurrencyPairRate("EURUSD", 1.12);
        CurrencyPairRate eurrubFirstUpdate = new CurrencyPairRate("EURRUB", 80.3);
        CurrencyPairRate eurrubSecondUpdate = new CurrencyPairRate("EURRUB", 120.8);

        List<CurrencyPairRate> updates = new ArrayList<>();

        updates.add(eurusd);
        updates.add(eurrubFirstUpdate);
        updates.add(eurrubSecondUpdate);

        List<CurrencyPairRate> slowProcessorExpectedUpdates = new ArrayList<>();

        slowProcessorExpectedUpdates.add(eurusd);
        slowProcessorExpectedUpdates.add(eurrubSecondUpdate);

        //WHEN
        for (CurrencyPairRate pairRate : updates) {
            throttler.onPrice(pairRate.getCcyPair(), pairRate.getRate());
        }

        //THEN
        while(throttler.isWorking()) {
            Thread.sleep(1000);
        }

        assertEquals(slowProcessorExpectedUpdates, slowSub.getUpdates());
    }
}
