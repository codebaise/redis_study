package com.zhi.redis.controller;

import com.zhi.redis.service.UAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UAController {

    @Autowired
    private UAService uaService;

    @RequestMapping("/hello")
    public String hello(@RequestParam(value = "word", required = false, defaultValue = "helloworld") String word){
        return uaService.uaDemo(word);
    }

    @GetMapping("/{user}/signIn/{day}")
    public boolean signIn(@PathVariable("user") String username, @PathVariable("day") int day) {
        return uaService.signIn(username, day);
    }

    @GetMapping("/checkSignIn/{username}")
    public long checkSignInRecord(@PathVariable("username") String username) {
        return uaService.checkSignInRecordByUsername(username);
    }

}
