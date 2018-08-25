package com.fleemer;

import com.fleemer.repository.TestConfigForMail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@Import(TestConfigForMail.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class FleemerApplicationTests {
    @Test
    public void contextLoads() {
    }

}
