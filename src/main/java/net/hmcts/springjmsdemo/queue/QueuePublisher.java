package net.hmcts.springjmsdemo.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.Session;


@Service
public class QueuePublisher {

    private final Logger logger = LoggerFactory.getLogger(QueuePublisher.class);

    @Value("${amqp.queue}")
    private String destination;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void afterConstruct() {
        sendPing();
    }

    public void sendPing() {
        logger.info("Sending ping to queue");
        jmsTemplate.send(destination, (Session session) -> session.createTextMessage("ping"));
    }
}
