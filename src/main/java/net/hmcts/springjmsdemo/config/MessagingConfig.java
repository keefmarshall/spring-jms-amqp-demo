package net.hmcts.springjmsdemo.config;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * http://ramblingstechnical.blogspot.com/p/using-azure-service-bus-with-spring-jms.html
 */
@Configuration
public class MessagingConfig {
    @Value("${spring.application.name}")
    private String clientId;

    /**
     * DO NOT USE THIS IN PRODUCTION! ONLY FOR TESTING!
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @deprecated
     */
    @Bean
    @Deprecated
    public SSLContext jmsSslContext() throws NoSuchAlgorithmException, KeyManagementException {

        // https://stackoverflow.com/a/2893932
        // DO NOT USE THIS IN PRODUCTION!
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        return sc;
    }

    @Bean
    public ConnectionFactory jmsConnectionFactory(
            MessageStoreDetails details,
            SSLContext jmsSslContext)
            throws UnsupportedEncodingException {
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(details.getUrlString());
        jmsConnectionFactory.setUsername(details.getUsername());
        jmsConnectionFactory.setPassword(details.getPassword());
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setReceiveLocalOnly(true);

        if (details.isTrustAllCerts()) {
            jmsConnectionFactory.setSslContext(jmsSslContext);
        }

        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        JmsTemplate returnValue = new JmsTemplate();
        returnValue.setConnectionFactory(jmsConnectionFactory);
        return returnValue;
    }

    /**
     * Simple factory, used for queues - see below for example for topics
     * @param connectionFactory
     * @return
     */
    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory returnValue = new DefaultJmsListenerContainerFactory();
        returnValue.setConnectionFactory(connectionFactory);
        return returnValue;
    }

    /**
     * Specific config required for topics in Azure Service Bus -
     * need setSubscriptionDurable(true) -
     * see: http://ramblingstechnical.blogspot.com/p/using-azure-service-bus-with-spring-jms.html
     * @param connectionFactory
     * @return
     */
    @Bean
    public JmsListenerContainerFactory topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory returnValue = new DefaultJmsListenerContainerFactory();
        returnValue.setConnectionFactory(connectionFactory);
        returnValue.setSubscriptionDurable(Boolean.TRUE);
        return returnValue;
    }
}
