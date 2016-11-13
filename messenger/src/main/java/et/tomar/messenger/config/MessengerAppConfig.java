package et.tomar.messenger.config;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by anteneh on 9/6/16.
 */
@Configuration
public class MessengerAppConfig {


    @Bean
    CamelContext camelContext(){

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.setStreamCaching(true);

        return camelContext;
    }
}
