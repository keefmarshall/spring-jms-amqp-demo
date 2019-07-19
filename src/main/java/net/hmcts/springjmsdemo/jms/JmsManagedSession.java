package net.hmcts.springjmsdemo.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.function.Function;

public class JmsManagedSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsManagedSession.class.getName());


    private Connection connection;
    private Session session;

    private JmsManagedSessionEventListener eventListener;

    private boolean initialised = false;

    public JmsManagedSession(JmsManagedSessionEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Use this to safely execute an operation requiring a session.
     *
     * e.g.:
     *
     * MessageProducer producer = managedSession.use((session) -> session.createProducer(dest))
     *
     * @param operation a function that takes a JMS session as argument and returns a typed result
     * @param <R> The type of the operation result
     * @return the result of the operation
     */
    public synchronized <R> R use(Function<Session, R> operation) {
        checkInitialised();
        return operation.apply(session);
    }

    public TextMessage createTextMessage(String text) {
        checkInitialised();
        TextMessage message = null;
        try {
            message = session.createTextMessage(text);
        } catch (JMSException e) {
            // I'm going to assume this can pretty much never happen, it's just building a wrapper
            // object in memoory there's no I/O
            LOGGER.warn("Can't build text message, JMS library insane", e);
        }

        return message;
    }

    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////
    // LIFECYCLE METHODS
    // - these are package private, should only be called by JmsSessionManager

    synchronized void open(ConnectionFactory connectionFactory) throws JMSException {
        if (initialised) {
            return;
        }

        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        initialised = true;

        if (eventListener != null) {
            try {
                eventListener.onSessionOpened(session);
            } catch (Exception e) {
                LOGGER.error("Error in event listener during session open", e);
            }
        }
    }

    synchronized void close() {

        if (eventListener != null) {
            try {
                eventListener.onSessionClosed(session);
            } catch (Exception e) {
                // session listener threw exception, but we still need to close
                LOGGER.error("Error in event listener during close", e);
            }
        }

        initialised = false;

        if (session != null) {
            try {
                session.close();
            } catch (JMSException ex) {
                // ignore, we just want to close
            } finally {
                session = null;
            }
        }

        if (connection != null) {
            try {
                connection.stop();
                connection.close();
            } catch (JMSException ex) {
                // ignore, we just want to close
            } finally {
                connection = null;
            }
        }
    }

    /**
     * This is not just a convenience method, it ensures that the close/open is done inside a single
     * synchronized block, so is effectively an atomic action
     *
     * @param connectionFactory
     * @throws JMSException
     */
    synchronized void reopen(ConnectionFactory connectionFactory) throws JMSException {
        this.close();
        this.open(connectionFactory);
    }

    synchronized boolean isAlive() {
        boolean alive = false;
        if (initialised && session != null) {
            try {
                session.getTransacted(); // will throw exception if session no longer usable in ASB
                alive = true;
            } catch (JMSException e) {
                alive = false;
            }
        }

        return alive;
    }

    private void checkInitialised() {
        if (!initialised) {
            throw new IllegalStateException("Managed JMS session not yet initialised!");
        }
    }

}
