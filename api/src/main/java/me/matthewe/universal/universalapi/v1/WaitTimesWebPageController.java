package me.matthewe.universal.universalapi.v1;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/")
public class WaitTimesWebPageController {

    @GetMapping(produces = "text/html; charset=UTF-8")
    public String ticketWebPage() throws IOException {
        ClassPathResource htmlFile = new ClassPathResource("static/waitTimes.html");
        return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
    }
}
