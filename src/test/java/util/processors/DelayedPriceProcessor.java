package util.processors;

import java.util.ArrayList;
import java.util.List;

import model.CurrencyPairRate;
import processor.PriceProcessor;

public class DelayedPriceProcessor implements PriceProcessor {
    private List<CurrencyPairRate> updates;
    private long lastUpdateTimestamp;
    private int delayMillis;

    public DelayedPriceProcessor(int delayMillis) {
        this.updates = new ArrayList<>();
        this.delayMillis = delayMillis;
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updates.add(new CurrencyPairRate(ccyPair, rate));
        lastUpdateTimestamp = System.currentTimeMillis();
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {

    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {

    }

    public List<CurrencyPairRate> getUpdates() {
        return updates;
    }

    public long getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }
}
