package me.matthewe.universal.universalapi.v1.ticketdata;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/tickets")
public class TicketWebPageController {

    @GetMapping(produces = "text/html; charset=UTF-8")
    public String ticketWebPage() throws IOException {
        ClassPathResource htmlFile = new ClassPathResource("static/ticketPage.html");
        return StreamUtils.copyToString(htmlFile.getInputStream(), StandardCharsets.UTF_8);
    }
}
