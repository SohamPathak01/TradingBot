package com.example.tradingbot.controller;

//import com.example.tradingbot.service.TradingBotService;
import com.example.tradingbot.service.TradingBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/trading-bot")
public class TradingBotController {

    @Autowired
    private TradingBotService tradingBotService;

    @GetMapping("/run")
    public String runTradingBot() throws IOException, InterruptedException {
        tradingBotService.monitorWallets(tradingBotService.identifySpecificWallets("HERO"));
        return "Trading bot is running...";
    }
}
