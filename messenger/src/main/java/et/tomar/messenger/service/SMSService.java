package et.tomar.messenger.service;

import et.tomar.messenger.http.MessageRequest;
import et.tomar.messenger.http.MessageResponse;
import et.tomar.messenger.util.Constants;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @Value("${websocket.queue.sms.outbox}")
    private String websocketSMSOutboxQueue;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Produce
    private ProducerTemplate producerTemplate;

    @Autowired
    public SMSService(SimpMessageSendingOperations messageSendingOperations) {

        this.messageSendingOperations = messageSendingOperations;
        this.sessionCache = new HashMap<>();
    }

    public void send(MessageRequest message, String sessionId) {

        sessionCache.put(message.getFrom(), sessionId);

        Map<String, Object> header = new HashMap<>();

        header.put(Constants.SMS_ORIGIN, message.getFrom());
        header.put(Constants.SMS_DESTINATION, message.getTo());

        producerTemplate.sendBodyAndHeaders(smsOutboxQueue, message.getBody(), header);
    }

    public void receive(Exchange exchange) {

        String from = String.valueOf(exchange.getIn().getHeader(Constants.SMS_DESTINATION_E164));
        String to = String.valueOf(exchange.getIn().getHeader(Constants.SMS_ORIGIN_E164));
        Object body = exchange.getIn().getBody();

        this.simpMessagingTemplate.convertAndSend(websocketSMSOutboxQueue, new MessageResponse(from, to, String.valueOf(body)));
    }

    public void handleDeliveryReceipt(Exchange exchange) {

    }
}
