package et.tomar.messenger.controller;

import et.tomar.messenger.http.MessageRequest;
import et.tomar.messenger.http.MessageResponse;
import et.tomar.messenger.service.MessageService;
import et.tomar.messenger.util.Constants;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by anteneh on 5/22/16.
 */
@Controller
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Value("${smpp.origin}")
    private String origin;

    @Value("${smpp.destination}")
    private String destination;

    @Value("${websocket.queue.sms.outbox}")
    private String websocketSMSOutboxQueue;

    @Value("${smpp.queue.ui}")
    private String uiSMSOutboxQueue;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MessageService smsService;

    @RequestMapping("/")
    public String home() {

        return "home";
    }

    @MessageMapping("/sms/send")
    public void sendSMS(SimpMessageHeaderAccessor messageHeaderAccessor, MessageRequest message) throws Exception {

        smsService.send(message.getFrom(), message.getTo(), message.getBody(), messageHeaderAccessor.getSessionId());
    }

    @Consume(uri = "{{smpp.queue.ui}}")
    public void receiveSMS(@Header(value = "SMSDestinationE164") String from,
                           @Header(value = "SMSOriginE164") String to,
                           @Body String body){

        this.simpMessagingTemplate.convertAndSend(websocketSMSOutboxQueue, new MessageResponse(from, to, body));
    }

    @MessageExceptionHandler
    @SendTo("/sms/errors")
    public  String handleException(Throwable exception){

        logger.warn("An error occurred", exception);
        return exception.getLocalizedMessage();
    }

    @ModelAttribute("origin")
    public String getOrigin(){

        return origin;
    }
    @ModelAttribute("destination")
    public String getDestination(){

        return destination;
    }
}
