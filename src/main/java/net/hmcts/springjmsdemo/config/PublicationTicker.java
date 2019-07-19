package net.hmcts.springjmsdemo.config;

import net.hmcts.springjmsdemo.topic.TopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PublicationTicker {
    private final Logger logger = LoggerFactory.getLogger(PublicationTicker.class);

    private TopicPublisher publisher;

    @Autowired
    PublicationTicker(TopicPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void afterConstruct() throws Exception {
        new Thread( () -> {
            int c = 0;
            try {
                while (true) {
                    try {
                        publisher.sendPing(c++);
                    } catch (Exception e) { // Critically important to catch exceptions here
                        logger.error("Publisher threw exception trying to send: ", e);
                    }
                    Thread.sleep(1000 * 60 * 11); // 11 minutes
                }
            } catch (InterruptedException iex) {
                logger.warn("Thread interrupted", iex);
            }
        }).start();
    }

}
