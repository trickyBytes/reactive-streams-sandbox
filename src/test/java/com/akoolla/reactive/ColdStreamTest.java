package com.akoolla.reactive;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

/**
 * Unit test for simple App.
 */
public class ColdStreamTest {

    private final Logger logger = LoggerFactory.getLogger(ColdStreamTest.class);
    private Flux<String> stringFlux = Flux.just("1", "2", "3", "4");
    private List<String> elements = new ArrayList<>();

    @Test
    public void subscribingToAStream() throws Exception {
        // Full Implementation
        stringFlux.log()
                  .subscribe(new Subscriber<String>() {
                      @Override
                      public void onSubscribe(Subscription s) {
                          s.request(Long.MAX_VALUE);
                      }

                      @Override
                      public void onNext(String t) {
                          elements.add(t);
                      }

                      @Override
                      public void onError(Throwable t) {
                      }

                      @Override
                      public void onComplete() {
                      }
                  });

        // This
        stringFlux.log()
                  .subscribe(new Consumer<String>() {
                      @Override
                      public void accept(String t) {
                          elements.add(t);
                      }
                  });

        // Is the same as this
        stringFlux.log()
                  .subscribe(s -> elements.add(s));

        // Which is the same as this
        stringFlux.log()
                  .subscribe(elements::add);
    }

    @Test
    public void subscribeWithSomeBackPressure() throws Exception {
        stringFlux.log()
                  .subscribe(new Subscriber<String>() {
                      private Subscription subscription;
                      int onNextAmount;

                      @Override
                      public void onSubscribe(Subscription s) {
                          this.subscription = s;
                          s.request(2);
                      }

                      @Override
                      public void onNext(String t) {
                          elements.add(t);
                          onNextAmount++;
                          if (onNextAmount % 2 == 0) {
                              subscription.request(2);
                          }
                      }

                      @Override
                      public void onError(Throwable t) {
                      }

                      @Override
                      public void onComplete() {
                      }
                  });
    }

    @Test
    public void operateOnAStream() throws Exception {
        stringFlux.log()
                  .map(i -> i + "n")
                  .zipWith(Flux.range(0, Integer.MAX_VALUE),
                          (two, one) -> String.format("%s, %s", one, two))
                  .subscribe(elements::add);
        
        elements.stream().forEach(logger::debug);
    }
}
