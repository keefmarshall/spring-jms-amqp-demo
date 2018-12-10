package net.hmcts.springjmsdemo.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class TopicConsumer {
    private final Logger logger = LoggerFactory.getLogger(TopicConsumer.class);

    public TopicConsumer() {
        logger.info("TopicConsumer created");
    }

    public void init() {
        // null method, used for lazy init
        logger.info("TopicConsumer: init()");
    }

    @JmsListener(destination = "${amqp.topic}",
            containerFactory = "topicJmsListenerContainerFactory",
            subscription = "${amqp.subscription}")
    public void onMessage(String message) {
        logger.info("Received message from topic: {}", message);
    }
}
