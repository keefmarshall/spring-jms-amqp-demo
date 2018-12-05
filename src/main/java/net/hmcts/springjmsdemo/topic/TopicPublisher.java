package net.hmcts.springjmsdemo.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.Session;

/**
 * Essentially identical to the QueuePublisher
 */
@Service
public class TopicPublisher {
    private final Logger logger = LoggerFactory.getLogger(TopicPublisher.class);

    @Value("${amqp.topic}")
    private String destination;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void afterConstruct() {
        sendPing();
    }

    public void sendPing() {
        logger.info("Sending pong to topic");
        jmsTemplate.send(destination, (Session session) -> session.createTextMessage("pong"));
    }

}
