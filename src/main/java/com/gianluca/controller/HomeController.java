package com.gianluca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {
	@GetMapping("/")
	public String home() {
		log.debug("Invocato endpoint / da HomeController");
		log.info("Risposta home pronta per il client");
		return "redirect:/login.html";
	}
}