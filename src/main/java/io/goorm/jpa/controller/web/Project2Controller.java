package io.goorm.jpa.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Step 2 전용 프로젝트 문서 컨트롤러
 * 양방향 관계, 비관적 락, QueryDSL, 배치 처리 등 Step 2 관련 문서 제공
 */
@Slf4j
@Controller
@RequestMapping("/project/step2")
public class Project2Controller {

    @GetMapping({"", "/", "/overview"})
    public String overview(Model model) {
        log.info("Step 2 overview page accessed");
        model.addAttribute("pageTitle", "Step 2 개요");
        return "project/step2/overview";
    }

    @GetMapping("/bidirectional")
    public String bidirectional(Model model) {
        log.info("Step 2 bidirectional page accessed");
        model.addAttribute("pageTitle", "양방향 관계와 편의 메서드");
        return "project/step2/bidirectional";
    }

    @GetMapping("/locking")
    public String locking(Model model) {
        log.info("Step 2 locking page accessed");
        model.addAttribute("pageTitle", "비관적 락과 동시성 제어");
        return "project/step2/locking";
    }

    @GetMapping("/querydsl")
    public String querydsl(Model model) {
        log.info("Step 2 querydsl page accessed");
        model.addAttribute("pageTitle", "QueryDSL 고급 기능");
        return "project/step2/querydsl";
    }

    @GetMapping("/batch")
    public String batch(Model model) {
        log.info("Step 2 batch page accessed");
        model.addAttribute("pageTitle", "배치 처리와 성능 최적화");
        return "project/step2/batch";
    }

    @GetMapping("/troubleshooting")
    public String troubleshooting(Model model) {
        log.info("Step 2 troubleshooting page accessed");
        model.addAttribute("pageTitle", "LazyInitializationException");
        return "project/step2/troubleshooting";
    }

    @GetMapping("/assignments")
    public String assignments(Model model) {
        log.info("Step 2 assignments page accessed");
        model.addAttribute("pageTitle", "실습 과제");
        return "project/step2/assignments";
    }

    @GetMapping("/changes")
    public String changes(Model model) {
        log.info("Step 2 changes page accessed");
        model.addAttribute("pageTitle", "Step 2 변경사항");
        return "project/step2/changes";
    }

    @GetMapping("/logging-strategy")
    public String loggingStrategy(Model model) {
        log.info("Step 2 logging strategy page accessed");
        model.addAttribute("pageTitle", "로깅 전략");
        return "project/step2/logging-strategy";
    }

    @GetMapping("/antipatterns")
    public String antipatterns(Model model) {
        log.info("Step 2 antipatterns page accessed");
        model.addAttribute("pageTitle", "안티패턴 개선");
        return "project/step2/antipatterns";
    }

    @GetMapping("/practical-assignments")
    public String practicalAssignments(Model model) {
        log.info("Step 2 practical assignments page accessed");
        model.addAttribute("pageTitle", "실습 과제 (UI 구현)");
        return "project/step2/practical-assignments";
    }

    @GetMapping("/hateoas-theory")
    public String hateoasTheory(Model model) {
        log.info("Step 2 HATEOAS theory page accessed");
        model.addAttribute("pageTitle", "HATEOAS 이론과 실습");
        return "project/step2/hateoas-theory";
    }

    @GetMapping("/hateoas-simple")
    public String hateoasSimple(Model model) {
        log.info("Step 2 HATEOAS simple page accessed");
        model.addAttribute("pageTitle", "HATEOAS 개요");
        return "project/step2/hateoas-simple";
    }

    @GetMapping("/graphql-theory")
    public String graphqlTheory(Model model) {
        log.info("Step 2 GraphQL theory page accessed");
        model.addAttribute("pageTitle", "GraphQL 이론과 실습");
        return "project/step2/graphql-theory";
    }

    @GetMapping("/oauth-theory")
    public String oauthTheory(Model model) {
        log.info("Step 2 OAuth theory page accessed");
        model.addAttribute("pageTitle", "OAuth 2.0 소셜 로그인");
        return "project/step2/oauth-theory";
    }
}
