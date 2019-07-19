package net.hmcts.springjmsdemo.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * When using AMQP against Azure Service Bus, we need to do additional 'keepalive' work
 * as the ASB 'link' times out if left idle for more than 10 minutes - but doesn't kill
 * the connection, which would otherwise be handled by the Spring layer.
 *
 * We try to reuse as much of the Spring/QPID layer as we can though, to reduce code
 */
@Component
public class JmsSessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsSessionManager.class.getName());
    private static final long HEARTBEAT_DELAY_SECONDS = 30; // TODO: should we externalise this?

    @Autowired private ConnectionFactory connectionFactory;

    private Set<JmsManagedSession> sessions = new HashSet<>();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> heartbeat;

    public synchronized JmsManagedSession createManagedSession(JmsManagedSessionEventListener eventListener) throws JMSException {
        JmsManagedSession session = new JmsManagedSession(eventListener);
        session.open(connectionFactory);
        sessions.add(session);
        return session;
    }

    public void reopenSession(JmsManagedSession session) throws JMSException {
        session.reopen(connectionFactory);
    }

    public synchronized void closeSession(JmsManagedSession session) {
        sessions.remove(session);
        session.close();
    }

    @PostConstruct
    public void open() {
        startHeartbeat();
    }

    @PreDestroy
    public synchronized void close() {
        stopHeartbeat();
        sessions.forEach(this::closeSession);
    }

    /////////////////////////////////////////
    // HEARTBEAT MANAGEMENT

    protected void startHeartbeat() {
        LOGGER.info("Starting managed session heartbeat...");
        heartbeat = executorService.scheduleWithFixedDelay(
                 this::doHeartbeat, HEARTBEAT_DELAY_SECONDS, HEARTBEAT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    protected void stopHeartbeat() {
        heartbeat.cancel(true);
        LOGGER.info("Stopped managed session heartbeat.");
    }

    protected synchronized void doHeartbeat() {
        LOGGER.debug("Heartbeat:");
        sessions.forEach(session -> {
            if (!session.isAlive()) {
                LOGGER.debug("Heartbeat: Found dead session, reopening...");
                try {
                    session.reopen(connectionFactory);
                    LOGGER.debug("Heartbeat: JMS session successfully reopened.");
                } catch (JMSException e) {
                    // This is a major failure, we can log but this probably breaks the app
                    // It'll get retried next heartbeat, so maybe that's enough?
                    LOGGER.error(
                            "Heartbeat: JMS connection session is dead, reopen failed. " +
                            "Will try again next heartbeat.", e);
                }
            }
        });
    }
}
