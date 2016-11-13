package et.tomar.messenger.engine;


import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static et.tomar.messenger.util.Constants.SEPARATOR_COMMA;

/**
 * Created by anteneh on 9/19/16.
 */
public class Blacklist implements Predicate {

    private Logger logger = LoggerFactory.getLogger(Blacklist.class);

    private Set<String> tokens;
    private String headerName;

    public Blacklist(String blacklistCountries, String headerName) {
        tokens = new HashSet<String>();
        for (String s : blacklistCountries.split(SEPARATOR_COMMA)) {
            tokens.add(s.toLowerCase());
        }
        this.headerName = headerName;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Message message = exchange.getIn();
        if (!message.getHeaders().containsKey(headerName)) {
            return false;
        }
        String value = message.getHeader(headerName, String.class).toLowerCase();
        if (tokens.contains(value)) {
            logger.info("header '{}' value '{}' is blacklisted", headerName, value);
            return true;
        }
        return false;
    }

}
