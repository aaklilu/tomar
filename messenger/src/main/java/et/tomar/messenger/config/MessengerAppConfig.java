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
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by anteneh on 9/6/16.
 */
@Configuration
public class MessengerAppConfig {

    @Autowired
    private Environment environment;

    @Bean
    public PropertiesResolver propertiesResolver(){

        return (context, ignoreMissingLocation, uri) -> {

            Properties answer = new Properties();

            YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
            yamlPropertiesFactoryBean.setSingleton(true);
            yamlPropertiesFactoryBean.setDocumentMatchers(new DefaultProfileDocumentMatcher());

            List<Resource> resources = new ArrayList<>();

            for (String path : uri) {

                resources.add(new ClassPathResource(path));
            }

            yamlPropertiesFactoryBean.setResources(resources.toArray(new Resource[resources.size()]));
            answer.putAll(yamlPropertiesFactoryBean.getObject());

            return answer;
        };
    }

    @Bean
    public PropertiesComponent propertiesComponent(){

        PropertiesComponent propertiesComponent = new PropertiesComponent();
        propertiesComponent.setPropertiesResolver(propertiesResolver());
        List<String> locations = new ArrayList<>();
        locations.add("application.yml");

        for(String profile: environment.getActiveProfiles()){

            locations.add(String.format("application-%s.yml", profile));
        }
        propertiesComponent.setLocations(locations.toArray(new String[locations.size()]));

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
