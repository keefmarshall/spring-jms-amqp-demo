package net.hmcts.springjmsdemo.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;


public class JMSErrorHandler implements ErrorHandler {

    private static Logger log = LoggerFactory.getLogger(JMSErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
        log.warn("spring jms custom error handling example");
        log.error(t.getCause().getMessage());
    }
}
