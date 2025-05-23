package it.tournaments.overlay.controller;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.sse.Event;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Controller("/dev")
@Requires(env = "development")
@Slf4j
public class DevController implements ApplicationEventListener<StartupEvent> {
    
    private final AtomicLong startupTime = new AtomicLong();
    
    @Override
    public void onApplicationEvent(StartupEvent event) {
        startupTime.set(System.currentTimeMillis());
        log.info("Application started at: {}", startupTime.get());
    }
    
    @Get("/reload-events")
    @Produces(MediaType.TEXT_EVENT_STREAM)
    @ExecuteOn(TaskExecutors.IO)
    public Publisher<Event<String>> reloadEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> Event.of("startup:" + startupTime.get())
                        .name("ping")
                        .id(String.valueOf(sequence)))
                .doOnSubscribe(subscription -> log.debug("SSE client connected"))
                .doOnCancel(() -> log.debug("SSE client disconnected"))
                .doOnError(error -> log.error("SSE error", error));
    }
    
    @Get("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        return "{\"status\":\"ok\",\"startup\":" + startupTime.get() + "}";
    }
} 