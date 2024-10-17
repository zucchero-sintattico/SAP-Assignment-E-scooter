package management_service.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/api/management/dashboard")
    public ModelAndView dashboard() {
        System.out.println("Dashboard accessed");
        return new ModelAndView("dashboard");
    }
}
