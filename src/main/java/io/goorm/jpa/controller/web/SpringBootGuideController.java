package io.goorm.jpa.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/springboot")
@Slf4j
public class SpringBootGuideController {

    @GetMapping("/setup")
    public String setup(Model model) {
        log.info("Spring Boot setup page accessed");
        model.addAttribute("pageTitle", "프로젝트 설정");
        return "springboot/setup";
    }

    @GetMapping("/model")
    public String model(Model model) {
        log.info("Spring Boot model page accessed");
        model.addAttribute("pageTitle", "Model/Entity 어노테이션");
        return "springboot/model";
    }

    @GetMapping("/controller")
    public String controller(Model model) {
        log.info("Spring Boot controller page accessed");
        model.addAttribute("pageTitle", "Controller 어노테이션");
        return "springboot/controller";
    }

    @GetMapping("/service")
    public String service(Model model) {
        log.info("Spring Boot service page accessed");
        model.addAttribute("pageTitle", "Service 어노테이션");
        return "springboot/service";
    }

    @GetMapping("/repository")
    public String repository(Model model) {
        log.info("Spring Boot repository page accessed");
        model.addAttribute("pageTitle", "Repository 어노테이션");
        return "springboot/repository";
    }
}
