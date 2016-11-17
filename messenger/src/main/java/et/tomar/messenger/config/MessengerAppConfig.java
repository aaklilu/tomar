package et.tomar.messenger.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesResolver;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.yaml.DefaultProfileDocumentMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

/**
 * Created by anteneh on 9/6/16.
 */
@Configuration
public class MessengerAppConfig {

    @Bean
    public PropertiesResolver propertiesResolver(){

        return (context, ignoreMissingLocation, uri) -> {

            Properties answer = new Properties();

            for (String path : uri) {

                YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
                yamlPropertiesFactoryBean.setSingleton(true);
                yamlPropertiesFactoryBean.setDocumentMatchers(new DefaultProfileDocumentMatcher());
                yamlPropertiesFactoryBean.setResources(new ClassPathResource(path)/*, new ClassPathResource("application-${spring.profiles.active:default}.yml")*/);

                answer.putAll(yamlPropertiesFactoryBean.getObject());
            }

            return answer;
        };
    }

    @Bean
    public PropertiesComponent propertiesComponent(){

        PropertiesComponent propertiesComponent = new PropertiesComponent();
        propertiesComponent.setPropertiesResolver(propertiesResolver());
        propertiesComponent.setLocation("application.yml");

        return propertiesComponent;
    }

    @Bean
    public CamelContext camelContext(){

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addComponent("properties", propertiesComponent());
        camelContext.setStreamCaching(true);

        return camelContext;
    }
}
