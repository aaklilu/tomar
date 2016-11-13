package et.tomar.messenger.engine.smpp.processor;

import et.tomar.messenger.engine.smpp.route.SMPPRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static et.tomar.messenger.util.Constants.SEPARATOR_COLON;
import static et.tomar.messenger.util.Constants.SEPARATOR_COMMA;

/**
 * Optionally overrides source number with the number provided.
 * eg. US,UK:+0123, all destinations that are identified of US or UK numbers, the source number will be changed to +0123
 *
 * Created by anteneh on 9/19/16.
 */
public class SourceOverride implements Processor {

    private Logger logger = LoggerFactory.getLogger(SMPPRouteBuilder.class);

    private Map<String, String> overrideSources;
    private String countryHeader;
    private String sourceHeader;

    public SourceOverride(String overrides, String countryHeader, String sourceHeader) {

        logger.debug("Parsing source overrides configuration string: {}", overrides);
        overrideSources = new HashMap();

        for (String group : overrides.split(SEPARATOR_COMMA)) {

            String[] parts = group.split(SEPARATOR_COLON);
            if (parts.length < 2) {
                String error = "Colon missing from overrides";
                logger.warn(error);
                throw new IllegalArgumentException(error);
            }
            if (parts.length > 2) {
                String error = "Too many colons in overrides group";
                logger.warn(error);
                throw new IllegalArgumentException(error);
            }

            String[] countries = parts[0].split(",");
            String source = parts[1];
            for (String country : countries) {
                String countryUpperCase = country.toUpperCase();
                logger.info("Country '{}' will use source: {}",
                        countryUpperCase, source);
                overrideSources.put(countryUpperCase, source);
            }
        }
        this.countryHeader = countryHeader;
        this.sourceHeader = sourceHeader;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();

        if (!message.getHeaders().containsKey(countryHeader)) {
            logger.debug("country header {} not present", countryHeader);
            return;
        }

        String destCountry = message.getHeader(countryHeader, String.class)
                .toUpperCase();

        String override = overrideSources.get(destCountry);
        if (override != null) {
            logger.debug("Overriding with source number {}", override);
            message.setHeader(sourceHeader, override);
        }
    }

}
