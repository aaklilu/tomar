package et.tomar.messenger.service;

import et.tomar.messenger.http.MessageRequest;
import org.apache.camel.Exchange;

/**
 * Created by anteneh on 9/27/16.
 */
public interface MessageService {

    public void send(MessageRequest message, String sessionId);

    public void receive(Exchange exchange);

    public void handleDeliveryReceipt(Exchange exchange);
}
