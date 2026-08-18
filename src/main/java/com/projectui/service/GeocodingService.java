package com.projectui.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeocodingService {

    private static final GeocodingService INSTANCE = new GeocodingService();

    public static GeocodingService getInstance() {
        return INSTANCE;
    }

    public static class GeocodedLocation {
        public String displayName;
        public double lat;
        public double lng;
        public String address;
        public String type;
        public String category;

        public GeocodedLocation(String displayName, double lat, double lng, String address, String type, String category) {
            this.displayName = displayName;
            this.lat = lat;
            this.lng = lng;
            this.address = address;
            this.type = type;
            this.category = category;
        }

        @Override
        public String toString() {
            return displayName + " (Lat: " + String.format("%.4f", lat) + ", Lng: " + String.format("%.4f", lng) + ")";
        }
    }

    public CompletableFuture<List<GeocodedLocation>> searchPortLocation(String portName) {
        CompletableFuture<List<GeocodedLocation>> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                if (portName == null || portName.isBlank()) {
                    future.complete(new ArrayList<>());
                    return;
                }

                String query = portName.trim();
                List<GeocodedLocation> list = performNominatimSearch(query);

                // Fallback attempt: append "port" if not present and no results found
                if (list.isEmpty() && !query.toLowerCase().contains("port") && !query.toLowerCase().contains("harbor") && !query.toLowerCase().contains("terminal")) {
                    list = performNominatimSearch(query + " port");
                }

                future.complete(list);
            } catch (Exception ex) {
                System.err.println("[GEOCODING ERROR] Failed to geocode '" + portName + "': " + ex.getMessage());
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    private List<GeocodedLocation> performNominatimSearch(String query) {
        List<GeocodedLocation> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlStr = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&addressdetails=1&limit=5";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "DockTech-MaritimeApplication/1.0 (maritime@docktech.com)");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    results = parseNominatimJson(sb.toString());
                }
            } else {
                System.err.println("[GEOCODING API] Returned HTTP " + conn.getResponseCode() + " for query: " + query);
            }
        } catch (Exception ex) {
            System.err.println("[GEOCODING SEARCH ERROR] " + ex.getMessage());
        }
        return results;
    }

    private List<GeocodedLocation> parseNominatimJson(String json) {
        List<GeocodedLocation> list = new ArrayList<>();
        if (json == null || !json.trim().startsWith("[")) return list;

        String raw = json.trim();
        if (raw.equals("[]")) return list;

        // Split into individual JSON objects
        List<String> objects = splitJsonArray(raw);
        for (String item : objects) {
            try {
                String latStr = extractJsonValue(item, "lat");
                String lonStr = extractJsonValue(item, "lon");
                String displayName = extractJsonValue(item, "display_name");
                String type = extractJsonValue(item, "type");
                String category = extractJsonValue(item, "class");

                if (latStr != null && lonStr != null && displayName != null) {
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    list.add(new GeocodedLocation(displayName, lat, lon, displayName, type, category));
                }
            } catch (Exception ignored) {}
        }
        return list;
    }

    private List<String> splitJsonArray(String json) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    list.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return list;
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1) return null;

        int start = idx + searchKey.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }

        int end = start;
        while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }

        if (start < end) {
            return json.substring(start, end);
        }
        return null;
    }
}
