package net.hmcts.springjmsdemo.config;

import net.hmcts.springjmsdemo.topic.TopicConsumer;
import net.hmcts.springjmsdemo.topic.TopicPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DelayedQueueStarter {
    @Value("${amqp.startDelaySeconds:5}")
    private int startDelaySeconds = 5;

    final TopicConsumer topicConsumer;
    final TopicPublisher topicPublisher;

    @Lazy
    @Autowired
    public DelayedQueueStarter(TopicConsumer topicConsumer, TopicPublisher topicPublisher) {
        this.topicConsumer = topicConsumer;
        this.topicPublisher = topicPublisher;
    }

    @PostConstruct
    public void afterConstruct() {
        ScheduledExecutorService scheduler
                = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(this::startQueues, startDelaySeconds, TimeUnit.SECONDS);
        scheduler.shutdown();
    }

    private void startQueues() {
        topicConsumer.init();
//        topicPublisher.init();
    }
}
