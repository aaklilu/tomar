package et.tomar.web;

import et.tomar.messenger.MessengerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MessengerApplication.class)
@WebAppConfiguration
public class MessengerApplicationTests {

    @Test
    public void contextLoads() {
    }

}
