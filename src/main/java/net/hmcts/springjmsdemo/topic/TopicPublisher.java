package net.hmcts.springjmsdemo.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.Session;

/**
 * Essentially identical to the QueuePublisher
 */
@Service
@Lazy
public class TopicPublisher {
    private final Logger logger = LoggerFactory.getLogger(TopicPublisher.class);

    @Value("${amqp.topic}")
    private String destination;

    private final JmsTemplate jmsTemplate;

    @Autowired
    public TopicPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        logger.info("TopicPublisher created");
    }

    public void init() {
        // null method, used for lazy init
        logger.info("TopicPublisher: init()");
    }

    @PostConstruct
    public void afterConstruct() throws Exception {
        while(true) {
            try {
                sendPing();
            } catch (Exception e) { // Critically important to catch exceptions here
                logger.error("Publisher caught exception trying to send: ", e);
            }
            Thread.sleep(5000);
        }
    }

    // TODO: Spring Retry
    public void sendPing() {
        logger.info("Sending pong to topic...");
        jmsTemplate.send(destination, (Session session) -> session.createTextMessage("pong"));
        logger.info("...sent.");
    }

}
