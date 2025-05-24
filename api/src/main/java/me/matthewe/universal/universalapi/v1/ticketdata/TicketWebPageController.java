package me.matthewe.universal.universalapi.v1.ticketdata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/tickets")
public class TicketWebPageController {

    @GetMapping
    public RedirectView ticketWebPage() {
        return new RedirectView("/ticketPage.html");
    }
}
