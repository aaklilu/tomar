package et.tomar.messenger.engine.smpp.route;

import et.tomar.messenger.engine.Blacklist;
import et.tomar.messenger.engine.smpp.processor.CountryClassifier;
import et.tomar.messenger.engine.smpp.processor.SourceOverride;
import et.tomar.messenger.engine.smpp.processor.SmppAddressing;
import et.tomar.messenger.service.MessageService;
import org.apache.camel.Predicate;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static et.tomar.messenger.util.Constants.SMS_DESTINATION_COUNTRY_ISO2;
import static et.tomar.messenger.util.Constants.SMS_ORIGIN;
import static et.tomar.messenger.util.Constants.SMS_DESTINATION;
import static et.tomar.messenger.util.Constants.SMS_ORIGIN_E164;

/**
 * Created by anteneh on 9/6/16.
 */
@Component
public class SMPPRouteBuilder extends SpringRouteBuilder {

    private Logger logger = LoggerFactory.getLogger(SMPPRouteBuilder.class);

    @Value("${smpp.throttle.timePeriodMillis}")
    long throttleTimePeriodMillis;

    @Value("${smpp.throttle.maximumRequestsPerPeriod}")
    int throttleRequestsPerPeriod;

    @Value("${smpp.local.country}")
    private String localCountry;

    @Value("${smpp.smsc.country}")
    private String smscCountry;

    @Value("${smpp.blacklist.countries}")
    private String blacklistCountries;

    @Value("${smpp.source-overrides}")
    private String overrides;

    @Value("${smpp.system-type}")
    private String systemType;

    @Value("${smpp.username}")
    private String username;

    @Value("${smpp.host}")
    private String host;

    @Value("${smpp.password}")
    private String password;

    @Value("${smpp.address-range}")
    private String addressRange;

    @Value("${smpp.port}")
    private Integer port;

    @Value("${smpp.link-timer}")
    private Integer linkTimer;

    @Value("${smpp.queue.inbox}")
    private String smsInboxQueue;

    @Value("${smpp.queue.outbox}")
    private String smsOutboxQueue;

    @Value("${smpp.queue.dead-letter}")
    private String smsDeadLetterQueue;

    @Autowired
    private MessageService smsService;

    @Override
    public void configure() throws Exception {

        /**
         * Log some information about the configuration.
         */
        logger.info("Parsing locally supplied numbers using context country: {}", localCountry);
        logger.info("Parsing SMSC supplied numbers using context country: {}", smscCountry);
        logger.info("Throttling allows {} request(s) per {}ms", throttleRequestsPerPeriod, throttleTimePeriodMillis);

        /**
         * Create some Processor instances that will be used in the routes.
         */
        CountryClassifier origin = new CountryClassifier(localCountry, SMS_ORIGIN);
        CountryClassifier destination = new CountryClassifier(localCountry, SMS_DESTINATION);
        Predicate blacklistedDestination = new Blacklist(blacklistCountries, SMS_DESTINATION_COUNTRY_ISO2);
        SourceOverride sourceOverride = new SourceOverride(overrides, destination.getCountryHeaderName(), origin.getParsedHeaderName());
        SmppAddressing smppAddressing = new SmppAddressing(smscCountry,origin.getParsedHeaderName(), destination.getParsedHeaderName());

        /**
         * Create some strings that will be used in the Camel routes
         */
        String log = "log:et.tomar.messenger.camel?level=INFO";
        String logWarn = "log:et.tomar.messenger.camel?level=WARN";

        String smppUriTemplate =
                String.format("smpp://%s@%s:%d"
                        + "?password=%s"
                        + "&enquireLinkTimer=%d"
                        + "&typeOfNumber=1"
                        + "&numberingPlanIndicator=1", username, host, port, password, linkTimer);

        String smppUriProducer = String.format("%s&registeredDelivery=1&systemType=producer" ,smppUriTemplate);
        String smppUriConsumer = String.format("%s&addressRange=%s&systemType=consumer" ,smppUriTemplate, addressRange);

        /**
         * This Camel route handles messages going out to the SMS world
         */
        from(smsOutboxQueue)

                .errorHandler(deadLetterChannel(smsDeadLetterQueue))
                .onException(Exception.class)
                .logExhaustedMessageHistory(true)
                .logHandled(true)
                .maximumRedeliveries(2)
                .redeliveryDelay(5000)
                .end()
                .removeHeaders("CamelSmpp*")//In case it started as SMS elsewhere
                .process(origin)
                .process(destination)
                .choice()
                .when(blacklistedDestination)
                .to(smsDeadLetterQueue)
                .otherwise()
                .process(sourceOverride)
                .process(smppAddressing)
                .throttle(throttleRequestsPerPeriod)
                .timePeriodMillis(throttleTimePeriodMillis)
                .to(smppUriProducer)
                .setBody(simple("The SMSC accepted the message"
                        + " for ${header.CamelSmppDestAddr}"
                        + " and assigned SMPP ID: ${header.CamelSmppId}"))
                .to(logWarn);

        /**
         * This Camel route handles messages coming to us from the SMS world
         */
        from(smppUriConsumer)
                .threads(1, 1)
                .choice()
                .when(simple("${header.CamelSmppMessageType} == 'DeliveryReceipt'"))
                .setBody(simple("Message delivery receipt"
                        + " for SMPP ID ${header.CamelSmppId}"))
                .to(log)
                .when(simple("${header.CamelSmppMessageType} == 'DeliverSm'"))
                .process(smppAddressing)
                .setHeader(SMS_ORIGIN, header(SMS_ORIGIN_E164))
                .removeHeaders("Camel*")
                .to(smsInboxQueue)
                .setBody(simple("Message from ${header.SMSOriginE164}"
                        + " to ${header.SMSDestinationE164}: ${body}"))
                .to(log)
                .otherwise()
                .setBody(simple("Unhandled event type: ${header.CamelSmppMessageType}"))
                .to(logWarn);

        /**
         * Send to messageService
         */
        from(smsInboxQueue)
                .threads(1, 1)
                .bean(smsService, "receive")
                .to(log);
    }
}
