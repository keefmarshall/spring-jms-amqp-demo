package net.hmcts.springjmsdemo.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Essentially identical to the QueuePublisher
 */
@Service
// @Lazy
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

    // NB DOES NOT TRIGGER RETRYABLE, DOESN'T CALL THE INSTRUMENTED INSTANCE OF THIS CLASS
    // - instead we have to inject ths class instance somewhere else and call ping() on the injected instance.
//    @PostConstruct
//    public void afterConstruct() throws Exception {
//        while(true) {
//            try {
//                this.sendPing();
//            } catch (Exception e) { // Critically important to catch exceptions here
//                logger.error("Publisher caught exception trying to send: ", e);
//            }
//            Thread.sleep(5000);
//        }
//    }

    // TODO: Spring Retry
    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 3)
    )
    public void sendPing(int counter) {
        logger.info("Sending pong " + counter + " to topic...");
        jmsTemplate.send(destination, (Session session) -> session.createTextMessage("pong " + counter));
        logger.info("...sent.");
    }

    @Recover
    public void recoverSendPing(Throwable ex) throws Throwable {
        logger.error("TopicPublisher.recover(): SendPing failed with exception: ", ex);
        throw ex;
    }
}
