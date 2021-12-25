package com.example.topology_end;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/hello")
public class HelloWorldController {
    @RequestMapping(value="/say", method = RequestMethod.POST)
    public String say() {
        return "Hello World";
    }

    @RequestMapping(value="/shout")
    public String shout() {
        return "H!E!L!L!L W!O!R!L!D!";
    }
}

