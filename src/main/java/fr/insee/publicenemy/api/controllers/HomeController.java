package fr.insee.publicenemy.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/")
    public String test() {
        log.info("test");
        return "ok";
    }
}
