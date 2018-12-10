package net.hmcts.springjmsdemo.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class QueueReceiver {
    private final Logger logger = LoggerFactory.getLogger(QueueReceiver.class);

    @JmsListener(destination = "${amqp.queue}")
    public void onMessage(String message) {
        logger.info("Received message from queue: {}", message);
        // topicPublisher.sendPong();
    }
}