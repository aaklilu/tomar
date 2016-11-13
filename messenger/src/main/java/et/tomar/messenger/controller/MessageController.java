package et.tomar.messenger.controller;

import et.tomar.messenger.http.MessageRequest;
import et.tomar.messenger.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by anteneh on 5/22/16.
 */
@Controller
class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Value("${smpp.origin}")
    private String origin;

    @Value("${smpp.destination}")
    private String destination;


    @Autowired
    MessageService smsService;

    @RequestMapping("/")
    public String home() {

        return "home";
    }

    @MessageMapping("/sms/send")
    public void sendSMS(SimpMessageHeaderAccessor messageHeaderAccessor, MessageRequest message) throws Exception {

        smsService.send(message, messageHeaderAccessor.getSessionId());
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
