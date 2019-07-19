package net.hmcts.springjmsdemo.jms.topic;

import net.hmcts.springjmsdemo.jms.JmsManagedSession;
import net.hmcts.springjmsdemo.jms.JmsManagedSessionEventListener;
import net.hmcts.springjmsdemo.jms.JmsSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.*;

@Component
public class JmsManagedTopicPublisher implements JmsManagedSessionEventListener {
    private final Logger logger = LoggerFactory.getLogger(JmsManagedTopicPublisher.class.getName());

    @Value("${amqp.managedtopic}") private String destination;
    @Autowired private JmsSessionManager sessionManager;

    private JmsManagedSession managedSession;
    private MessageProducer messageProducer;

    public void send(String text) {
        logger.debug("Sending message to topic...");
        try {
            messageProducer.send(managedSession.createTextMessage(text));
        } catch (JMSException e) {
            logger.warn("Initial JMS send failed, reconnecting and trying again..", e);
            try {
                sessionManager.reopenSession(managedSession);
                messageProducer.send(managedSession.createTextMessage(text));
            } catch (JMSException e2) {
                // unrecoverable
                logger.error("JMS session unrecoverable at this time, message publish failed.", e2);
                throw new RuntimeException("JMS session unrecoverable at this time, message publish failed.", e2);
            }
        }
        logger.debug("...sent.");
    }

    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    // LIFECYCLE METHODS BELOW HERE


    @PostConstruct
    public void init() throws JMSException {
        this.managedSession = sessionManager.createManagedSession(this);
    }

    @PreDestroy
    public void destroy() {
        sessionManager.closeSession(managedSession);
    }

    @Override
    public void onSessionOpened(Session session) {
        try {
            logger.info("JMS Managed Session opened, creating MessageProducer..");
            Destination topic = session.createTopic(destination);
            this.messageProducer = session.createProducer(topic);
        } catch (JMSException e) {
            // This really shouldn't happen, the managedSession has just opened. Not easy to see how we can recover.
            logger.error("Exception trying to create MessageProducer! Application will need to be restarted", e);
            throw new RuntimeException(e); // kill the app
        }
    }

    @Override
    public void onSessionClosed(Session session) {
        if (messageProducer != null) {
            try {
                logger.info("JMS Managed Session closed, closing MessageProducer..");
                this.messageProducer.close();
                this.messageProducer = null;
            } catch (JMSException e) {
                // We really don't care too much, but log just for info:
                logger.warn("Error closing MessageProducer", e);
            }
        }
    }
}
