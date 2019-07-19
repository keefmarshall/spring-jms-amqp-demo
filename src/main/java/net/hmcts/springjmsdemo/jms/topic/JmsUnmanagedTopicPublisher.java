package net.hmcts.springjmsdemo.jms.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.jms.*;

/**
 * We're hitting issues with an idle session timeout in Azure Service Bus (ASB) for topic publishers.
 *
 * This class doesn't attempt to use a managed session, it creates a new connection and session
 * for every message publish. It's inefficient but much simpler than a managed option.
 *
 * If you're using a CachingConnectionFactory this shouldn't actually create a brand new connection each time but
 * be aware this might also cache sessions, so not actually solving the AMQP / ASB timeout issues.
 */
@Component
public class JmsUnmanagedTopicPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsUnmanagedTopicPublisher.class.getName());

    @Autowired private ConnectionFactory connectionFactory;

    @Retryable(
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 3)
    )
    public void send(String text, String destination) {
        LOGGER.debug("Sending message to topic...");

        try (Connection connection = connectionFactory.createConnection()) {

            connection.start();

            try (Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)) {

                Destination topic = session.createTopic(destination);

                try (MessageProducer messageProducer = session.createProducer(topic)) {

                    messageProducer.send(session.createTextMessage(text));
                    LOGGER.debug("...sent.");

                }
            }

        } catch (JMSException e) {
            LOGGER.error("JMS send failed", e);
            // Do we push an exception further up? Probably, this is bad.
            throw new RuntimeException("JMS send failed", e);
        }
    }


    @Recover
    public void recoverSend(Throwable ex) throws Throwable {
        LOGGER.error("JmsUnmanagedTopicPublisher.recover(): Send repeatedly failed with exception: ", ex);
        throw ex;
    }

}
