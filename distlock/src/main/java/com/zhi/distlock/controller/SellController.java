package com.zhi.distlock.controller;

import com.zhi.distlock.service.DistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellController {

    @Autowired
    private DistService distService;

    @GetMapping("/sell/{goodName}")
    public String sellGoodsByName(@PathVariable("goodName") String goodName) throws Exception {
        return distService.sellGoods(goodName);
    }

    @GetMapping("/add/{goodName}")
    public String addGoodInventory(@PathVariable("goodName") String goodName) {
        distService.addInventory(goodName, 100);
        return "ok";
    }
}
