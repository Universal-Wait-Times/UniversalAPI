package me.matthewe.universal.universalapi;

import me.matthewe.universal.universalapi.v1.ResortRegion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UniversalParkRegionTester {
    // Replace this with the actual API base URL if different
    private static final String API_BASE = "https://services.universalorlando.com/api/venues?city=%s&pageSize=All";
//    private static final String API_BASE = "https://assets.universalparks.com/%s/wait-time/wait-time-attraction-list.json";

    // List of region codes to test
    private static final String[] REGION_CODES = {
        "USH"
    };

    public static void main(String[] args) {
        for (ResortRegion code : ResortRegion.values()) {
            String region = code.toString().toLowerCase();

            String url = String.format(API_BASE,region);

            System.out.println("Testing region: " + region);
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
//                        System.out.println("Response for " + region + ":\n" + content);
                        System.out.println(content);
                    }
                } else {
                    System.out.println("No data or invalid region.");
                }
                conn.disconnect();
                System.out.println("--------");
            } catch (Exception e) {
                System.out.println("Error testing region '" + region + "': " + e.getMessage());
            }
        }
    }
}