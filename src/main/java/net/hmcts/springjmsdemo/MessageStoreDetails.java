package net.hmcts.springjmsdemo;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageStoreDetails {

    @Value("${amqp.host}")
    private String host;

    @Value("${amqp.username}")
    private String username;

    @Value("${amqp.password}")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrlString() throws UnsupportedEncodingException {
        return String.format("amqp://%1s?amqp.idleTimeout=3600000", host);
//        return String.format("amqps://%1s?amqp.idleTimeout=3600000", host);
    }
}
