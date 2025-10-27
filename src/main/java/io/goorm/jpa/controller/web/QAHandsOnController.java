package io.goorm.jpa.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * QA Hands-On 가이드 컨트롤러
 * 테스트 자동화 학습 가이드 (Postman, JUnit, nGrinder, JMeter, CI/CD)
 */
@Slf4j
@Controller
@RequestMapping("/qa-hands-on")
public class QAHandsOnController {

    @GetMapping({"", "/"})
    public String index() {
        log.info("QA Hands-On index - redirecting to overview");
        return "redirect:/qa-hands-on/overview";
    }

    @GetMapping("/overview")
    public String overview(Model model) {
        log.info("QA Hands-On overview page accessed");

        model.addAttribute("pageTitle", "QA Hands-On Overview");

        return "qa-hands-on/overview";
    }

    @GetMapping("/postman")
    public String postman(Model model) {
        log.info("Postman guide page accessed");

        model.addAttribute("pageTitle", "Lv.1 Postman - REST API 수동 테스트");

        return "qa-hands-on/postman";
    }

    @GetMapping("/junit")
    public String junit(Model model) {
        log.info("JUnit guide page accessed");

        model.addAttribute("pageTitle", "Lv.2 JUnit - 자동 테스트");

        return "qa-hands-on/junit";
    }

    @GetMapping("/ngrinder")
    public String ngrinder(Model model) {
        log.info("nGrinder guide page accessed");

        model.addAttribute("pageTitle", "Lv.3 nGrinder - 성능 테스트");

        return "qa-hands-on/ngrinder";
    }

    @GetMapping("/jmeter")
    public String jmeter(Model model) {
        log.info("JMeter guide page accessed");

        model.addAttribute("pageTitle", "Lv.4 JMeter - 대기업 표준 성능 테스트");

        return "qa-hands-on/jmeter";
    }

    @GetMapping("/cicd")
    public String cicd(Model model) {
        log.info("CI/CD guide page accessed");

        model.addAttribute("pageTitle", "CI/CD - GitHub Actions 자동화");

        return "qa-hands-on/cicd";
    }

    @GetMapping("/final")
    public String finalMission(Model model) {
        log.info("Final mission page accessed");

        model.addAttribute("pageTitle", "실전 종합 미션");

        return "qa-hands-on/final";
    }
}
