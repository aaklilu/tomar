package et.tomar.messenger.engine.smpp.processor;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.smpp.SmppMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.smpp.SmppConstants.DEST_ADDR;
import static org.apache.camel.component.smpp.SmppConstants.DEST_ADDR_TON;
import static org.apache.camel.component.smpp.SmppConstants.MESSAGE_TYPE;
import static org.apache.camel.component.smpp.SmppConstants.SOURCE_ADDR;
import static org.apache.camel.component.smpp.SmppConstants.SOURCE_ADDR_TON;

/**
 * Handles camel specific addressing. Converts several headers into camel headers
 * eg. smsOriginE164 will be changed to {@link org.apache.camel.component.smpp.SmppConstants#SOURCE_ADDR}
 *
 * Created by anteneh on 9/19/16.
 */
public class SmppAddressing implements Processor {

    private static final int SMPP_TON_E164 = 1;
    private static final int SMPP_TON_NATIONAL = 2;

    private Logger logger = LoggerFactory.getLogger(SmppAddressing.class);

    private String smscCountry;
    private String originHeaderName;
    private String destinationHeaderName;
    private PhoneNumberUtil pnu;

    public SmppAddressing(String smscCountry, String originHeaderName, String destinationHeaderName) {
        this.smscCountry = smscCountry;
        this.originHeaderName = originHeaderName;
        this.destinationHeaderName = destinationHeaderName;
        pnu = PhoneNumberUtil.getInstance();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();

        String messageType = message.getHeader(MESSAGE_TYPE, String.class);
        if (messageType != null) {
            if (messageType.equals(SmppMessageType.DeliverSm.toString())) {
                handleDeliverSM(exchange);
            }
        } else {
            handleOutgoingMessage(exchange);
        }
    }

    private void handleOutgoingMessage(Exchange exchange) throws CamelExchangeException {
        Message message = exchange.getIn();

        String e164Origin = message.getHeader(originHeaderName, String.class);
        if (e164Origin == null) {
            String error = "Missing origin header: " + originHeaderName;
            logger.debug(error);
            throw new CamelExchangeException(error, exchange);
        }

        String e164Destination = message.getHeader(destinationHeaderName, String.class);
        if (e164Destination == null) {
            String error = "Missing destination header: " + destinationHeaderName;
            logger.debug(error);
            throw new CamelExchangeException(error, exchange);
        }

        // Specify that number type is E.164
        message.setHeader(SOURCE_ADDR_TON, SMPP_TON_E164);
        message.setHeader(DEST_ADDR_TON, SMPP_TON_E164);

        // Remove the leading '+' from the E.164 numbers
        message.setHeader(SOURCE_ADDR, e164Origin.substring(1));
        message.setHeader(DEST_ADDR, e164Destination.substring(1));
    }

    private void handleDeliverSM(Exchange exchange) throws CamelExchangeException {

        handleSmscAddress(exchange,SOURCE_ADDR, SOURCE_ADDR_TON, originHeaderName);

        handleSmscAddress(exchange, DEST_ADDR, DEST_ADDR_TON, destinationHeaderName);
    }

    private void handleSmscAddress(Exchange exchange,
                                   String headerAddr, String headerTon, String headerE164) throws CamelExchangeException {

        Message message = exchange.getIn();

        if (!message.getHeaders().containsKey(headerTon)) {
            String error = "Missing header: " + headerTon;
            logger.warn(error);
            throw new CamelExchangeException(error, exchange);
        }
        int typeOfNumber = message.getHeader(headerTon, Integer.class);

        if (!message.getHeaders().containsKey(headerAddr)) {
            String error = "Missing header: " + headerAddr;
            logger.warn(error);
            throw new CamelExchangeException(error, exchange);
        }
        String smppAddress = message.getHeader(headerAddr, String.class);

        String e164Address = null;
        switch (typeOfNumber) {
            case SMPP_TON_E164:
                e164Address = "+" + smppAddress;
                break;
            case SMPP_TON_NATIONAL:
                PhoneNumber phoneNumber = null;
                try {
                    phoneNumber = pnu.parse(smppAddress, smscCountry);
                } catch (NumberParseException ex) {
                    logger.warn("Failed to parse national number: {}", smppAddress);
                }
                if (phoneNumber != null) {
                    e164Address = pnu.format(phoneNumber, PhoneNumberFormat.E164);
                }
                break;
            default:
                logger.warn("Unhandled type of number: " + typeOfNumber);
        }
        if (e164Address != null) {
            message.setHeader(headerE164, e164Address);
        }
    }

}
