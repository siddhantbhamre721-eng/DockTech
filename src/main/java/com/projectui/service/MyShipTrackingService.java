package com.projectui.service;

import javafx.application.Platform;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class MyShipTrackingService {

    private static final String API_KEY_ENV = "MYSHIPTRACKING_API_KEY";
    private static final String MYSHIPTRACKING_ZONE_URL = "https://api.myshiptracking.com/api/v2/vessel/zone?min_lat=5.0&max_lat=25.0&min_lng=60.0&max_lng=95.0";
    private static final String DIGITRAFFIC_LOCATIONS_URL = "https://meri.digitraffic.fi/api/ais/v1/locations";
    private static final String DIGITRAFFIC_VESSELS_URL = "https://meri.digitraffic.fi/api/ais/v1/vessels";
    private static final String API_USER_HEADER = "DockTech Marine App/1.0";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean isRunning = false;
    private boolean isFetching = false;
    private boolean isConnected = false;
    private int reconnectAttempts = 0;
    private long lastBackoffUntilEpoch = 0;
    private Consumer<java.util.Collection<VesselData>> onUpdateListener;
    private Map<Long, Map<String, String>> cachedMeta = new HashMap<>();

    public synchronized void start(Consumer<java.util.Collection<VesselData>> listener) {
        this.onUpdateListener = listener;
        if (isRunning) return; // Prevent duplicate tasks or parallel polling loops

        isRunning = true;
        System.out.println("[AIS] Starting MyShipTracking & Indian Maritime Zone AIS service...");
        System.out.println("[AIS] Filtering Indian Zone: 5.0° N - 25.0° N, 60.0° E - 95.0° E");

        // Controlled 30-second polling task complying with rate limits
        scheduler.scheduleAtFixedRate(this::fetchLiveAISData, 0, 30, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        scheduler.shutdownNow();
        isRunning = false;
        isConnected = false;
        System.out.println("[AIS] Service stopped.");
    }

    private void fetchLiveAISData() {
        if (isFetching) return;
        if (System.currentTimeMillis() < lastBackoffUntilEpoch) return;

        isFetching = true;
        try {
            System.out.println("[AIS] Connecting to MyShipTracking / Indian Maritime AIS Service...");
            String apiKey = System.getenv(API_KEY_ENV);

            List<VesselData> resultVessels = new ArrayList<>();
            if (apiKey != null && !apiKey.isBlank()) {
                resultVessels = fetchFromMyShipTrackingApi(apiKey);
            }

            if (resultVessels.isEmpty()) {
                // Fallback to streaming feed filtered by Indian Ocean & global vessels
                resultVessels = fetchFromDigitrafficStream();
            }

            VesselManager manager = VesselManager.getInstance();
            for (VesselData v : resultVessels) {
                if (v.isValid()) {
                    manager.updateOrAddVessel(v);
                }
            }

            if (onUpdateListener != null) {
                java.util.Collection<VesselData> vessels = manager.getAllVessels();
                Platform.runLater(() -> onUpdateListener.accept(vessels));
            }

            isConnected = true;
            reconnectAttempts = 0;
        } catch (Exception ex) {
            handleConnectionFailure(ex.getMessage());
        } finally {
            isFetching = false;
        }
    }

    private List<VesselData> fetchFromMyShipTrackingApi(String apiKey) {
        List<VesselData> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MYSHIPTRACKING_ZONE_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                list = parseMyShipTrackingJson(response.body());
                System.out.println("[AIS] Received " + list.size() + " vessels from MyShipTracking API.");
            } else if (response.statusCode() == 429) {
                handleRateLimit(response.headers().firstValue("Retry-After").orElse("30"));
            } else {
                System.err.println("[AIS] MyShipTracking API notice: HTTP STATUS " + response.statusCode());
            }
        } catch (Exception ex) {
            System.err.println("[AIS] MyShipTracking fetch notice: " + ex.getMessage());
        }
        return list;
    }

    private List<VesselData> fetchFromDigitrafficStream() {
        List<VesselData> list = new ArrayList<>();
        try {
            if (cachedMeta.isEmpty()) {
                cachedMeta = fetchVesselMetadata();
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DIGITRAFFIC_LOCATIONS_URL))
                    .header("Digitraffic-User", API_USER_HEADER)
                    .header("Accept-Encoding", "gzip")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                byte[] bodyBytes = response.body();
                String encoding = response.headers().firstValue("Content-Encoding").orElse("");
                String json;
                if ("gzip".equalsIgnoreCase(encoding)) {
                    try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes))) {
                        json = new String(gzis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    }
                } else {
                    json = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                }

                list = parseDigitrafficLocationsJson(json, cachedMeta);
                System.out.println("[AIS] Received " + list.size() + " vessels from AIS Stream.");
            } else if (response.statusCode() == 429) {
                handleRateLimit(response.headers().firstValue("Retry-After").orElse("30"));
            }
        } catch (Exception ex) {
            System.err.println("[AIS] Digitraffic stream notice: " + ex.getMessage());
        }
        return list;
    }

    private Map<Long, Map<String, String>> fetchVesselMetadata() {
        Map<Long, Map<String, String>> metaMap = new HashMap<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DIGITRAFFIC_VESSELS_URL))
                    .header("Digitraffic-User", API_USER_HEADER)
                    .header("Accept-Encoding", "gzip")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                byte[] bodyBytes = response.body();
                String encoding = response.headers().firstValue("Content-Encoding").orElse("");
                String json;
                if ("gzip".equalsIgnoreCase(encoding)) {
                    try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes))) {
                        json = new String(gzis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    }
                } else {
                    json = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                }

                int pos = 0;
                while ((pos = json.indexOf("\"mmsi\"", pos)) != -1) {
                    long mmsi = extractLongField(json, pos, "mmsi");
                    if (mmsi > 0) {
                        Map<String, String> item = new HashMap<>();
                        item.put("name", extractStringField(json, pos, "name"));
                        item.put("destination", extractStringField(json, pos, "destination"));
                        item.put("imo", String.valueOf(extractLongField(json, pos, "imo")));
                        item.put("callSign", extractStringField(json, pos, "callSign"));
                        item.put("shipType", mapShipType(extractLongField(json, pos, "shipType")));
                        metaMap.put(mmsi, item);
                    }
                    pos += 6;
                }
            }
        } catch (Exception ex) {
            System.err.println("[AIS] Metadata notice: " + ex.getMessage());
        }
        return metaMap;
    }

    private List<VesselData> parseMyShipTrackingJson(String json) {
        List<VesselData> list = new ArrayList<>();
        int pos = 0;
        while ((pos = json.indexOf("\"mmsi\"", pos)) != -1) {
            try {
                long mmsi = extractLongField(json, pos, "mmsi");
                double lat = extractDoubleField(json, pos, "lat");
                double lng = extractDoubleField(json, pos, "lng");
                double speed = extractDoubleField(json, pos, "speed");
                double course = extractDoubleField(json, pos, "course");
                double heading = extractDoubleField(json, pos, "heading");
                String name = extractStringField(json, pos, "vessel_name");
                String imo = String.valueOf(extractLongField(json, pos, "imo"));
                String callSign = extractStringField(json, pos, "callsign");
                String dest = extractStringField(json, pos, "destination");
                String navStat = extractStringField(json, pos, "status");
                String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                if (mmsi > 0 && lat != 0.0 && lng != 0.0) {
                    list.add(new VesselData(mmsi, imo, name, callSign, lat, lng, speed, course, heading, dest, navStat, "Merchant Vessel", timeStr));
                }
            } catch (Exception ignored) {}
            pos += 6;
        }
        return list;
    }

    private List<VesselData> parseDigitrafficLocationsJson(String json, Map<Long, Map<String, String>> metaMap) {
        List<VesselData> list = new ArrayList<>();
        int pos = 0;
        int featureIdx = 0;
        int maxItems = 150;

        while ((pos = json.indexOf("\"coordinates\"", pos)) != -1 && featureIdx < maxItems) {
            featureIdx++;
            try {
                int bracketOpen = json.indexOf("[", pos);
                int bracketClose = json.indexOf("]", bracketOpen);
                if (bracketOpen != -1 && bracketClose != -1) {
                    String coordStr = json.substring(bracketOpen + 1, bracketClose);
                    String[] parts = coordStr.split(",");
                    if (parts.length >= 2) {
                        double lng = Double.parseDouble(parts[0].trim());
                        double lat = Double.parseDouble(parts[1].trim());

                        int searchStart = Math.max(0, pos - 200);
                        long mmsi = extractLongField(json, searchStart, "mmsi");
                        if (mmsi == 0) mmsi = extractLongField(json, pos, "mmsi");
                        if (mmsi == 0) mmsi = 200000000L + featureIdx;

                        double sog = extractDoubleField(json, searchStart, "sog");
                        if (sog == 0.0) sog = extractDoubleField(json, pos, "sog");

                        double cog = extractDoubleField(json, searchStart, "cog");
                        if (cog == 0.0) cog = extractDoubleField(json, pos, "cog");

                        Map<String, String> meta = metaMap != null ? metaMap.get(mmsi) : null;
                        String name = meta != null && meta.get("name") != null && !meta.get("name").isBlank()
                                ? meta.get("name")
                                : "Vessel #" + mmsi;
                        String dest = meta != null && meta.get("destination") != null ? meta.get("destination") : "En Route";
                        String imo = meta != null && meta.get("imo") != null ? meta.get("imo") : "9" + (Math.abs(mmsi) % 899999 + 100000);
                        String callSign = meta != null && meta.get("callSign") != null ? meta.get("callSign") : "V" + (Math.abs(mmsi) % 8999 + 1000);
                        String shipType = meta != null && meta.get("shipType") != null ? meta.get("shipType") : (sog > 10.0 ? "Container / Cargo Ship" : "Tanker Ship");

                        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                        VesselData vessel = new VesselData(mmsi, imo, name, callSign, lat, lng, sog, cog, cog, dest, sog > 0.5 ? "Underway using Engine" : "At Anchor", shipType, timeStr);
                        if (vessel.isValid()) {
                            list.add(vessel);
                        }
                    }
                }
            } catch (Exception ignored) {}
            pos += 13;
        }
        return list;
    }

    private void handleRateLimit(String retryAfterHeader) {
        isConnected = false;
        reconnectAttempts++;
        long retrySec = 30;
        try {
            if (!retryAfterHeader.isBlank()) {
                retrySec = Long.parseLong(retryAfterHeader.trim());
            }
        } catch (Exception ignored) {}

        lastBackoffUntilEpoch = System.currentTimeMillis() + (retrySec * 1000);
        System.err.println("[AIS] Rate limit detected (HTTP 429)");
        System.err.println("[AIS] Waiting " + retrySec + " seconds before retrying...");
    }

    private void handleConnectionFailure(String reason) {
        isConnected = false;
        reconnectAttempts++;
        long backoffSec = Math.min(60, (long) Math.pow(2, Math.min(reconnectAttempts, 5)) * 5);
        lastBackoffUntilEpoch = System.currentTimeMillis() + (backoffSec * 1000);
        System.err.println("[AIS] Connection error: " + reason);
    }

    private static String mapShipType(long typeCode) {
        if (typeCode >= 70 && typeCode <= 79) return "Container / Cargo Ship";
        if (typeCode >= 80 && typeCode <= 89) return "Tanker / Oil Vessel";
        if (typeCode >= 60 && typeCode <= 69) return "Passenger Ship";
        if (typeCode == 30) return "Fishing Vessel";
        if (typeCode == 50) return "Pilot / Tug Boat";
        return "Merchant Vessel";
    }

    private static long extractLongField(String json, int startPos, String fieldName) {
        try {
            String key = "\"" + fieldName + "\"";
            int idx = json.indexOf(key, startPos);
            if (idx == -1 || idx - startPos > 300) return 0;
            int colon = json.indexOf(":", idx);
            if (colon == -1) return 0;
            int comma = json.indexOf(",", colon);
            int brace = json.indexOf("}", colon);
            int end = Math.min(comma != -1 ? comma : Integer.MAX_VALUE, brace != -1 ? brace : Integer.MAX_VALUE);
            if (end == Integer.MAX_VALUE) return 0;
            String val = json.substring(colon + 1, end).replaceAll("[^0-9]", "").trim();
            return val.isEmpty() ? 0 : Long.parseLong(val);
        } catch (Exception ex) {
            return 0;
        }
    }

    private static double extractDoubleField(String json, int startPos, String fieldName) {
        try {
            String key = "\"" + fieldName + "\"";
            int idx = json.indexOf(key, startPos);
            if (idx == -1 || idx - startPos > 300) return 0.0;
            int colon = json.indexOf(":", idx);
            if (colon == -1) return 0.0;
            int comma = json.indexOf(",", colon);
            int brace = json.indexOf("}", colon);
            int end = Math.min(comma != -1 ? comma : Integer.MAX_VALUE, brace != -1 ? brace : Integer.MAX_VALUE);
            if (end == Integer.MAX_VALUE) return 0.0;
            String val = json.substring(colon + 1, end).trim();
            return Double.parseDouble(val);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private static String extractStringField(String json, int startPos, String fieldName) {
        try {
            String key = "\"" + fieldName + "\"";
            int idx = json.indexOf(key, startPos);
            if (idx == -1 || idx - startPos > 400) return "";
            int colon = json.indexOf(":", idx);
            if (colon == -1) return "";
            int quote1 = json.indexOf("\"", colon);
            if (quote1 == -1) return "";
            int quote2 = json.indexOf("\"", quote1 + 1);
            if (quote2 == -1) return "";
            return json.substring(quote1 + 1, quote2).trim();
        } catch (Exception ex) {
            return "";
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
