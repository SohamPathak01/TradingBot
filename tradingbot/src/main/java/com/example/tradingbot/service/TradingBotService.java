package com.example.tradingbot.service;

import com.example.tradingbot.model.Transaction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradingBotService {
    private static final String TOKEN = "HERO";
    private static final long TIMEOUT = 7200 * 1000; // 2 hours in milliseconds
    private static OkHttpClient client = new OkHttpClient();

    @Value("${blockchain.api.baseurl}")
    private String apiUrl;

    @Value("${blockchain.api.key}")
    private String apiKey;

    public List<String> identifySpecificWallets(String token) throws IOException {
        List<String> specificWallets = new ArrayList<>();
        String holdersUrl = apiUrl + "/getTokenHolders?token=" + token;

        Request request = new Request.Builder()
                .url(holdersUrl)
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JsonArray holdersArray = JsonParser.parseString(responseBody).getAsJsonArray();

            for (JsonElement holder : holdersArray) {
                String wallet = holder.getAsJsonObject().get("wallet").getAsString();
                if (tradesOnlyHeroToken(wallet, token)) {
                    specificWallets.add(wallet);
                }
            }
        }
        return specificWallets;
    }

    private boolean tradesOnlyHeroToken(String wallet, String token) throws IOException {
        String transactionsUrl = apiUrl + "/getWalletTransactions?wallet=" + wallet;

        Request request = new Request.Builder()
                .url(transactionsUrl)
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JsonArray transactionsArray = JsonParser.parseString(responseBody).getAsJsonArray();

            for (JsonElement transaction : transactionsArray) {
                String txnToken = transaction.getAsJsonObject().get("token").getAsString();
                if (!txnToken.equals(token)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String monitorTransactions(String wallet) throws IOException {
        String transactionsUrl = apiUrl + "/getWalletTransactions?wallet=" + wallet;

        Request request = new Request.Builder()
                .url(transactionsUrl)
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JsonArray transactionsArray = JsonParser.parseString(responseBody).getAsJsonArray();

            for (JsonElement transaction : transactionsArray) {
                JsonObject txnObject = transaction.getAsJsonObject();
                String action = txnObject.get("action").getAsString();
                String txnToken = txnObject.get("token").getAsString();

                if ("buy".equals(action) && "SOL".equals(txnToken)) {
                    return "SOL_PURCHASE";
                } else if ("buy".equals(action) && TOKEN.equals(txnToken)) {
                    return "HERO_PURCHASE";
                }
            }
        }
        return null;
    }

    public void monitorWallets(List<String> wallets) throws IOException, InterruptedException {
        for (String wallet : wallets) {
            while (true) {
                String action = monitorTransactions(wallet);
                if ("SOL_PURCHASE".equals(action)) {
                    buyToken(TOKEN);
                    long startTime = System.currentTimeMillis();

                    while (true) {
                        action = monitorTransactions(wallet);
                        if ("HERO_PURCHASE".equals(action)) {
                            sellToken(TOKEN);
                            break;
                        } else if (System.currentTimeMillis() - startTime > TIMEOUT) {
                            sellToken(TOKEN);
                            break;
                        }
                        Thread.sleep(60000); // Wait for a minute before checking again
                    }
                }
                Thread.sleep(60000); // Wait for a minute before checking again
            }
        }
    }

    private void buyToken(String token) {
        // Implement the logic to buy the token using an API call
        System.out.println("Buying " + token);
    }

    private void sellToken(String token) {
        // Implement the logic to sell the token using an API call
        System.out.println("Selling " + token);
    }
}
