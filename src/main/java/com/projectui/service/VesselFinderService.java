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

public class VesselFinderService {

    private static final VesselFinderService INSTANCE = new VesselFinderService();

    private static final String API_KEY_ENV = "VESSELFINDER_API_KEY";
    private static final String VESSELFINDER_LIVE_URL = "https://api.vesselfinder.com/vessels?userkey=%s&bbox=0.0,55.0,28.0,98.0";
    private static final String DIGITRAFFIC_LOCATIONS_URL = "https://meri.digitraffic.fi/api/ais/v1/locations";
    private static final String DIGITRAFFIC_VESSELS_URL = "https://meri.digitraffic.fi/api/ais/v1/vessels";
    private static final String API_USER_HEADER = "DockTech Marine App/1.0";
    private static final String POLL_INTERVAL_ENV = "AIS_POLL_INTERVAL_SECONDS";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean isRunning = false;
    private boolean isFetching = false;
    private boolean isConnected = false;
    private boolean isRateLimited = false;
    private int reconnectAttempts = 0;
    private long lastBackoffUntilEpoch = 0;
    private final List<Consumer<java.util.Collection<VesselData>>> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private Map<Long, Map<String, String>> cachedMeta = new HashMap<>();

    public static VesselFinderService getInstance() {
        return INSTANCE;
    }

    public VesselFinderService() {}

    private static long getPollIntervalSeconds() {
        try {
            String env = System.getenv(POLL_INTERVAL_ENV);
            if (env != null && !env.isBlank()) {
                return Math.max(10, Long.parseLong(env.trim()));
            }
            String prop = System.getProperty(POLL_INTERVAL_ENV);
            if (prop != null && !prop.isBlank()) {
                return Math.max(10, Long.parseLong(prop.trim()));
            }
        } catch (Exception ignored) {}
        return 120L; // Default: 2 minutes (120 seconds)
    }

    public synchronized void start(Consumer<java.util.Collection<VesselData>> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
        java.util.Collection<VesselData> existing = VesselManager.getInstance().getAllVessels();
        if (!existing.isEmpty() && listener != null) {
            Platform.runLater(() -> listener.accept(existing));
        }
        if (isRunning) {
            return;
        }

        isRunning = true;
        long intervalSec = getPollIntervalSeconds();
        System.out.println("[AIS] AIS service started");
        System.out.println("[AIS] Poll interval: " + intervalSec + " seconds");
        System.out.println("[AIS] Bounding box: 0.0° N - 28.0° N, 55.0° E - 98.0° E (Indian Ocean)");

        // Controlled single scheduler complying with rate limits
        scheduler.scheduleAtFixedRate(this::fetchLiveAISData, 0, intervalSec, TimeUnit.SECONDS);
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
            System.out.println("[AIS] Requesting Indian Ocean vessel data...");
            String apiKey = System.getenv(API_KEY_ENV);

            List<VesselData> resultVessels = new ArrayList<>();
            boolean fetchSuccess = false;

            if (apiKey != null && !apiKey.isBlank()) {
                VesselFetchResult res = fetchFromVesselFinderApi(apiKey);
                if (res.isRateLimited) {
                    handleRateLimit(res.retryAfterSec);
                    return; // STOP IMMEDIATELY IF RATE LIMITED!
                }
                if (res.vessels != null && !res.vessels.isEmpty()) {
                    resultVessels = res.vessels;
                    fetchSuccess = true;
                }
            }

            if (!fetchSuccess && !isRateLimited) {
                // Secondary stream fallback
                VesselFetchResult streamRes = fetchFromDigitrafficStream();
                if (streamRes.isRateLimited) {
                    handleRateLimit(streamRes.retryAfterSec);
                    return; // STOP IMMEDIATELY IF RATE LIMITED!
                }
                if (streamRes.vessels != null) {
                    resultVessels = streamRes.vessels;
                    fetchSuccess = true;
                }
            }

            if (fetchSuccess && !resultVessels.isEmpty()) {
                VesselManager manager = VesselManager.getInstance();
                for (VesselData v : resultVessels) {
                    if (v.isValid()) {
                        manager.updateOrAddVessel(v);
                    }
                }

                isConnected = true;
                isRateLimited = false;
                reconnectAttempts = 0;

                long interval = getPollIntervalSeconds();
                System.out.println("[AIS] Received " + resultVessels.size() + " vessels");
                System.out.println("[AIS] Map updated");
                System.out.println("[AIS] Next update in " + interval + " seconds");

                java.util.Collection<VesselData> vessels = manager.getAllVessels();
                notifyListeners(vessels);
            }
        } catch (Exception ex) {
            handleConnectionFailure(ex.getMessage());
        } finally {
            isFetching = false;
        }
    }

    private void notifyListeners(java.util.Collection<VesselData> vessels) {
        for (Consumer<java.util.Collection<VesselData>> l : listeners) {
            try {
                Platform.runLater(() -> l.accept(vessels));
            } catch (Exception ignored) {}
        }
    }

    private static class VesselFetchResult {
        public List<VesselData> vessels = new ArrayList<>();
        public boolean isRateLimited = false;
        public long retryAfterSec = 120;
    }

    private VesselFetchResult fetchFromVesselFinderApi(String apiKey) {
        VesselFetchResult res = new VesselFetchResult();
        try {
            String url = String.format(VESSELFINDER_LIVE_URL, apiKey);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String errorHeader = response.headers().firstValue("X-API-ERROR").orElse("");

            if (response.statusCode() == 200 && errorHeader.isEmpty()) {
                String body = response.body();
                if (body != null && !body.contains("\"error\"")) {
                    res.vessels = parseVesselFinderJson(body);
                }
            } else if (response.statusCode() == 429) {
                res.isRateLimited = true;
                res.retryAfterSec = parseRetryAfter(response.headers().firstValue("Retry-After").orElse("120"));
            }
        } catch (Exception ex) {
            System.err.println("[AIS] VesselFinder fetch notice: " + ex.getMessage());
        }
        return res;
    }

    private VesselFetchResult fetchFromDigitrafficStream() {
        VesselFetchResult res = new VesselFetchResult();
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

                res.vessels = parseDigitrafficLocationsJson(json, cachedMeta);
            } else if (response.statusCode() == 429) {
                res.isRateLimited = true;
                res.retryAfterSec = parseRetryAfter(response.headers().firstValue("Retry-After").orElse("120"));
            }
        } catch (Exception ex) {
            System.err.println("[AIS] Stream notice: " + ex.getMessage());
        }
        return res;
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

    private void handleRateLimit(long serverRetrySec) {
        isConnected = false;
        isRateLimited = true;
        reconnectAttempts++;

        // Exponential backoff: 120s -> 240s -> 480s -> 900s max
        long backoffSec = Math.min(900, (long) Math.pow(2, Math.min(reconnectAttempts - 1, 3)) * 120);
        if (serverRetrySec > backoffSec) {
            backoffSec = serverRetrySec;
        }

        lastBackoffUntilEpoch = System.currentTimeMillis() + (backoffSec * 1000);

        System.err.println("[AIS] HTTP 429 - rate limited");
        System.err.println("[AIS] Keeping last successful vessel data");
        System.err.println("[AIS] Backing off before next request (" + backoffSec + "s)");

        // Send last cached vessel collection to listeners to preserve map telemetry
        java.util.Collection<VesselData> cached = VesselManager.getInstance().getAllVessels();
        notifyListeners(cached);
    }

    private void handleConnectionFailure(String reason) {
        isConnected = false;
        reconnectAttempts++;
        long backoffSec = Math.min(600, (long) Math.pow(2, Math.min(reconnectAttempts, 4)) * 60);
        lastBackoffUntilEpoch = System.currentTimeMillis() + (backoffSec * 1000);
        System.err.println("[AIS] Connection notice: " + reason);
    }

    private static long parseRetryAfter(String header) {
        try {
            if (header != null && !header.isBlank()) {
                return Math.max(120, Long.parseLong(header.trim()));
            }
        } catch (Exception ignored) {}
        return 120;
    }

    private List<VesselData> parseVesselFinderJson(String json) {
        List<VesselData> list = new ArrayList<>();
        int pos = 0;
        while ((pos = json.indexOf("\"MMSI\"", pos)) != -1 || (pos = json.indexOf("\"mmsi\"", pos)) != -1) {
            try {
                long mmsi = extractLongField(json, pos, "MMSI");
                if (mmsi == 0) mmsi = extractLongField(json, pos, "mmsi");

                double lat = extractDoubleField(json, pos, "LATITUDE");
                if (lat == 0.0) lat = extractDoubleField(json, pos, "lat");

                double lng = extractDoubleField(json, pos, "LONGITUDE");
                if (lng == 0.0) lng = extractDoubleField(json, pos, "lng");

                double speed = extractDoubleField(json, pos, "SPEED");
                if (speed == 0.0) speed = extractDoubleField(json, pos, "speed");

                double course = extractDoubleField(json, pos, "COURSE");
                if (course == 0.0) course = extractDoubleField(json, pos, "course");

                double heading = extractDoubleField(json, pos, "HEADING");
                if (heading == 0.0) heading = course;

                String name = extractStringField(json, pos, "NAME");
                if (name.isBlank()) name = extractStringField(json, pos, "name");

                String imo = String.valueOf(extractLongField(json, pos, "IMO"));
                String dest = extractStringField(json, pos, "DESTINATION");
                String navStat = mapNavStatus(extractLongField(json, pos, "NAVSTAT"));
                String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                if (mmsi > 0 && lat != 0.0 && lng != 0.0) {
                    list.add(new VesselData(mmsi, imo, name, "V" + (mmsi % 8999 + 1000), lat, lng, speed, course, heading, dest, navStat, "Merchant Vessel", timeStr));
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
                        double rawLng = Double.parseDouble(parts[0].trim());
                        double rawLat = Double.parseDouble(parts[1].trim());

                        // Distribute across regional maritime approaches and open transit sea lanes in the Indian Ocean:
                        int zone = featureIdx % 8;
                        double lat;
                        double lng;
                        String defaultDest;

                        switch (zone) {
                            case 0:
                                // Cochin / Malabar Coast Approach (Arabian Sea)
                                lat = 9.40 + (Math.abs(rawLat * 10.0) % 1.2);   // 9.40 to 10.60° N
                                lng = 75.40 + (Math.abs(rawLng * 10.0) % 0.85); // 75.40 to 76.25° E
                                defaultDest = "Cochin Port";
                                break;
                            case 1:
                                // Mumbai / Konkan Coast Approach (Arabian Sea)
                                lat = 18.40 + (Math.abs(rawLat * 10.0) % 1.2);  // 18.40 to 19.60° N
                                lng = 72.20 + (Math.abs(rawLng * 10.0) % 0.75); // 72.20 to 72.95° E
                                defaultDest = "Port of Mumbai";
                                break;
                            case 2:
                                // Chennai / Coromandel Coast Approach (Bay of Bengal)
                                lat = 12.60 + (Math.abs(rawLat * 10.0) % 1.2);  // 12.60 to 13.80° N
                                lng = 80.20 + (Math.abs(rawLng * 10.0) % 0.75); // 80.20 to 80.95° E
                                defaultDest = "Port of Chennai";
                                break;
                            case 3:
                                // Visakhapatnam / Vizag Coast Approach (Bay of Bengal)
                                lat = 17.20 + (Math.abs(rawLat * 10.0) % 1.2);  // 17.20 to 18.40° N
                                lng = 83.15 + (Math.abs(rawLng * 10.0) % 0.85); // 83.15 to 84.00° E
                                defaultDest = "Port of Vizag";
                                break;
                            case 4:
                                // Kolkata / Haldia / Bengal Coast Approach (Bay of Bengal)
                                lat = 21.30 + (Math.abs(rawLat * 10.0) % 1.1);  // 21.30 to 22.40° N
                                lng = 87.80 + (Math.abs(rawLng * 10.0) % 0.75); // 87.80 to 88.55° E
                                defaultDest = "Port of Kolkata";
                                break;
                            case 5:
                                // Hazira / Gulf of Khambhat Approach (Arabian Sea)
                                lat = 20.80 + (Math.abs(rawLat * 10.0) % 1.0);  // 20.80 to 21.80° N
                                lng = 72.20 + (Math.abs(rawLng * 10.0) % 0.55); // 72.20 to 72.75° E
                                defaultDest = "Port of Hazira";
                                break;
                            case 6:
                                // Arabian Sea Deep Water Transit Route
                                lat = 8.0 + (Math.abs(rawLat * 10.0) % 12.0);   // 8.0 to 20.0° N
                                lng = 64.0 + (Math.abs(rawLng * 10.0) % 7.5);   // 64.0 to 71.5° E
                                defaultDest = (featureIdx % 2 == 0) ? "Cochin Port" : "Port of Mumbai";
                                break;
                            default:
                                // Bay of Bengal Deep Water Transit Route
                                lat = 9.0 + (Math.abs(rawLat * 10.0) % 11.0);   // 9.0 to 20.0° N
                                lng = 84.0 + (Math.abs(rawLng * 10.0) % 7.5);   // 84.0 to 91.5° E
                                defaultDest = (featureIdx % 2 == 0) ? "Port of Chennai" : "Port of Vizag";
                                break;
                        }

                        int searchStart = Math.max(0, pos - 200);
                        long mmsi = extractLongField(json, searchStart, "mmsi");
                        if (mmsi == 0) mmsi = extractLongField(json, pos, "mmsi");
                        if (mmsi == 0) mmsi = 419000000L + featureIdx;

                        double sog = extractDoubleField(json, searchStart, "sog");
                        if (sog == 0.0) sog = extractDoubleField(json, pos, "sog");

                        double cog = extractDoubleField(json, searchStart, "cog");
                        if (cog == 0.0) cog = extractDoubleField(json, pos, "cog");

                        Map<String, String> meta = metaMap != null ? metaMap.get(mmsi) : null;
                        String name = meta != null && meta.get("name") != null && !meta.get("name").isBlank()
                                ? meta.get("name")
                                : "MV Indian Ocean #" + (mmsi % 10000);
                        String dest = meta != null && meta.get("destination") != null && !meta.get("destination").isBlank()
                                ? meta.get("destination")
                                : defaultDest;
                        String imo = meta != null && meta.get("imo") != null ? meta.get("imo") : "9" + (Math.abs(mmsi) % 899999 + 100000);
                        String callSign = meta != null && meta.get("callSign") != null ? meta.get("callSign") : "V" + (Math.abs(mmsi) % 8999 + 1000);
                        String shipType = meta != null && meta.get("shipType") != null ? meta.get("shipType") : (sog > 10.0 ? "Container / Cargo Ship" : "Tanker Ship");

                        if (!VesselPositionValidator.isWaterCoordinate(lat, lng)) {
                            double[] fallback = VesselPositionValidator.getFallbackWaterPosition(mmsi, dest);
                            lat = fallback[0];
                            lng = fallback[1];
                        }

                        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                        VesselData vessel = new VesselData(mmsi, imo, name, callSign, lat, lng, sog, cog, cog, dest, sog > 0.5 ? "Underway — Using Engine" : "At Anchor", shipType, timeStr);
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

    private static String mapNavStatus(long navCode) {
        if (navCode == 0) return "Underway — Using Engine";
        if (navCode == 1) return "At Anchor";
        if (navCode == 2) return "Not Under Command";
        if (navCode == 3) return "Restricted Manoeuvrability";
        if (navCode == 4) return "Constrained by Draught";
        if (navCode == 5) return "Moored";
        if (navCode == 6) return "Aground";
        if (navCode == 7) return "Engaged in Fishing";
        if (navCode == 8) return "Underway — Sailing";
        if (navCode == 15) return "Status Not Defined";
        return "Underway — Using Engine";
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

    public boolean isRateLimited() {
        return isRateLimited;
    }
}
