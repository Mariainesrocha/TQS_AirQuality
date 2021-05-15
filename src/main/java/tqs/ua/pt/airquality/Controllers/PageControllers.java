package tqs.ua.pt.airquality.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import tqs.ua.pt.airquality.Services.PlaceService;
import java.net.URISyntaxException;

@Controller
public class PageControllers {

    @Autowired
    PlaceService service;

    @GetMapping(value = "/")
    public ModelAndView home() throws URISyntaxException {
        ModelAndView mav = new ModelAndView("home");
        mav.addObject("place", service.getHere());
        return mav;
    }
}