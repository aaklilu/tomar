package et.tomar.messenger.service;

import et.tomar.messenger.http.MessageResponse;
import et.tomar.messenger.util.Constants;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anteneh on 10/2/16.
 */
@Service("smsService")
public class SMSService implements MessageService {

    private final SimpMessageSendingOperations messageSendingOperations;
    private final Map<String, String> sessionCache;//Still not working, need to figure out how to send to specific session

    @Value("${smpp.queue.outbox}")
    private String smsOutboxQueue;

    @Value("${smpp.queue.ui}")
    private String uiSMSOutboxQueue;

    @Produce
    private ProducerTemplate producerTemplate;

    @Autowired
    public SMSService(SimpMessageSendingOperations messageSendingOperations) {

        this.messageSendingOperations = messageSendingOperations;
        this.sessionCache = new HashMap<>();
    }

    @Override
    public void send(String from, String to, String body, String sessionId) {

        sessionCache.put(from, sessionId);

        Map<String, Object> header = new HashMap<>();

        header.put(Constants.SMS_ORIGIN, from);
        header.put(Constants.SMS_DESTINATION, to);

        producerTemplate.sendBodyAndHeaders(smsOutboxQueue, body, header);
    }

    @Override
    public void receive(Exchange exchange) {

        /**
         * Handle incoming messages here, but for this one, we'll just relay it to the UI queue and the message will be pushed thru websocket
         */
        producerTemplate.send(uiSMSOutboxQueue, exchange);
    }

    @Override
    public void handleDeliveryReceipt(Exchange exchange) {

    }
}
