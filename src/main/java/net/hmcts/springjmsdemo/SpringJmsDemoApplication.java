package net.hmcts.springjmsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringJmsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJmsDemoApplication.class, args);
    }
}
