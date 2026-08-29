package com.projectui.view.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility facade named `AdminDataStore` to match existing references.
 * This class mirrors the in-memory data used by the application.
 */
public class AdminDataStore {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final AdminDataStore INSTANCE = new AdminDataStore();

    public final List<Port> ports = new ArrayList<>();
    public final List<UserAccount> users = new ArrayList<>();
    public final List<ReportRow> reports = new ArrayList<>();
    public final List<PortRequest> portRequests = new ArrayList<>();

    public String adminName = "Dock Tech Admin";
    public String adminEmail = "";
    public String adminPassword = "admin123";
    public String accountCreated = "01 Jan 2026, 09:00";
    public String language = "English";
    public String dateFormat = "DD/MM/YYYY";
    public boolean notifications = true;
    public String authToken = "";
    public String settingsMessage = "";
    public String reportMessage = "";
    public String lastExportPath = "";

    private AdminDataStore() {
        ports.add(new Port("Port of Mumbai", "Mumbai, Maharashtra", "IN-MUM-01", "Mumbai Port Authority", 18, 12,
                "Operational", "Mixed cargo, containers", "82%", "Clear", now(), 18.9439, 72.8354, "APPROVED"));
        ports.add(new Port("Port of Chennai", "Chennai, Tamil Nadu", "IN-MAA-01", "Chennai Port Authority", 14, 8,
                "Operational", "Automobiles, containers", "76%", "Clear", now(), 13.0827, 80.2707, "APPROVED"));
        ports.add(new Port("Port of Vizag", "Visakhapatnam, Andhra Pradesh", "IN-VTZ-01",
                "Visakhapatnam Port Authority", 12, 5, "Maintenance", "Bulk cargo, minerals", "58%",
                "Crane service in progress", now(), 17.6868, 83.2185, "APPROVED"));
        ports.add(new Port("Port of Kolkata", "Kolkata, West Bengal", "IN-CCU-01",
                "Syama Prasad Mookerjee Port Authority", 10, 9, "Busy", "River cargo, containers", "91%",
                "High congestion watch", now(), 22.5726, 88.3639, "APPROVED"));
        ports.add(new Port("Cochin Port", "Kochi, Kerala", "IN-COK-01",
                "Cochin Port Authority", 16, 10, "Operational", "Crude oil, containers, spices", "78%",
                "Clear", now(), 9.9600, 76.2600, "APPROVED"));

        users.add(new UserAccount("pa_mum_01", "Mumbai Port Authority", "pa_mumbai@docktech.com", "Port Authority",
                "Active", "01 Jan 2026, 09:00", "Just now"));
        users.add(new UserAccount("pa_maa_01", "Chennai Port Authority", "pa_chennai@docktech.com", "Port Authority",
                "Active", "01 Jan 2026, 09:00", "Just now"));
        users.add(new UserAccount("pa_vtz_01", "Visakhapatnam Port Authority", "pa_vizag@docktech.com",
                "Port Authority", "Active", "01 Jan 2026, 09:00", "Just now"));
        users.add(new UserAccount("pa_ccu_01", "Kolkata Port Authority", "pa_kolkata@docktech.com", "Port Authority",
                "Active", "01 Jan 2026, 09:00", "Just now"));
        users.add(new UserAccount("pa_cok_01", "Cochin Port Authority", "pa_cochin@docktech.com", "Port Authority",
                "Active", "01 Jan 2026, 09:00", "Just now"));

        portRequests.add(new PortRequest(
                "NPR-101", "Port of Hazira", "IN-HAZ-01", "Hazira, Gujarat", "Hazira Port Complex, Surat",
                "Industrial container & liquid cargo terminal",
                "Vikram Shah", "vikram@haziraport.com", "+91 98765 11223",
                "Rajesh Varma", "pa_hazira@docktech.com", "PENDING", "15 Aug 2026", 21.1122, 72.6411));

        generateReports("Today", "All Status");
    }

    public static AdminDataStore getInstance() {
        return INSTANCE;
    }

    public void clearSession() {
        adminName = "Dock Tech Admin";
        adminEmail = "";
        language = "English";
        dateFormat = "DD/MM/YYYY";
        notifications = true;
        authToken = "";
        settingsMessage = "";
        reportMessage = "";
        lastExportPath = "";
    }

    public int activePorts() {
        int count = 0;
        for (Port port : ports) {
            if (("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)
                    && "Operational".equalsIgnoreCase(port.status)) {
                count++;
            }
        }
        return count;
    }

    public int totalPorts() {
        int count = 0;
        for (Port port : ports) {
            if ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null) {
                count++;
            }
        }
        return count;
    }

    public int maintenancePorts() {
        int count = 0;
        for (Port port : ports) {
            if (("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)
                    && "Maintenance".equalsIgnoreCase(port.status)) {
                count++;
            }
        }
        return count;
    }

    public int activeVessels() {
        int count = 0;
        for (Port port : ports) {
            if ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null) {
                count += port.vessels;
            }
        }
        return count;
    }

    public int cargoHandled() {
        return activeVessels() * 285;
    }

    public int activeUsers() {
        int count = 0;
        for (UserAccount user : users) {
            if ("Active".equals(user.status))
                count++;
        }
        return count;
    }

    public int pendingUsers() {
        int count = 0;
        for (UserAccount user : users) {
            if ("Pending".equals(user.status))
                count++;
        }
        return count;
    }

    public int pendingPortRequestsCount() {
        int count = 0;
        for (PortRequest req : portRequests) {
            if ("PENDING".equalsIgnoreCase(req.status))
                count++;
        }
        return count;
    }

    public int[] weeklyOperations() {
        int base = totalPorts() * 8;
        return new int[] { base + 6, base + 18, base + 13, base + 30, base + 24, base + activeVessels(), base + 10 };
    }

    public String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    public void generateReports(String filter, String statusFilter) {
        reports.clear();
        String periodLabel = filter == null || filter.isBlank() ? "Today" : filter;
        reports.add(new ReportRow("Port Operations Summary", periodLabel, "Ready", "Aarav Mehta (Admin)",
                "14 Aug 2026, 18:30"));
        reports.add(new ReportRow("Weekly Vessel Traffic & Turnaround", periodLabel, "Ready", "Capt. Rahul Patil",
                "14 Aug 2026, 14:15"));
        reports.add(new ReportRow("Cargo Throughput & Tonnage Analysis", periodLabel, "Processing",
                "JNPT Operations Team", "14 Aug 2026, 11:20"));
        reports.add(new ReportRow("Berth Occupancy & Utilization Index", periodLabel, "Ready",
                "Port Authority (Mumbai)", "13 Aug 2026, 21:00"));
        reports.add(new ReportRow("Customs Clearance & Pending Inspections", periodLabel, "Ready",
                "Alex Kumar (Customs)", "14 Aug 2026, 09:45"));
        reports.add(new ReportRow("Export/Import Manifest Reconciliation", periodLabel, "Ready", "Meera Rao (Exporter)",
                "12 Aug 2026, 16:20"));
        reports.add(new ReportRow("Environmental & Safety Compliance Audit", periodLabel, "Processing",
                "System Automated", "11 Aug 2026, 08:00"));

        if (!"All Status".equalsIgnoreCase(statusFilter) && statusFilter != null && !statusFilter.isBlank()) {
            reports.removeIf(report -> !statusFilter.equalsIgnoreCase(report.status));
        }
        reportMessage = "Reports updated for " + periodLabel;
    }

    public String exportReport(ReportRow report) throws IOException {
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);
        String fileName = report.name.toLowerCase().replace(" ", "-").replace("/", "-").replace("&", "and") + ".csv";
        Path file = exportDir.resolve(fileName);
        String content = "Report Name,Period,Generated By,Status,Generated Date,Exported At\n"
                + "\"" + report.name + "\",\"" + report.period + "\",\"" + report.generatedBy + "\",\""
                + report.status + "\",\"" + report.generatedDate + "\",\"" + now() + "\"\n";
        Files.writeString(file, content);
        lastExportPath = file.toAbsolutePath().toString();
        reportMessage = "Exported " + report.name + " to CSV successfully.";
        return lastExportPath;
    }

    public static class Port {
        public String name;
        public String location;
        public String code;
        public String portAuthority;
        public int berths;
        public int vessels;
        public String status;
        public String cargo;
        public String capacity;
        public String maintenance;
        public String lastUpdated;
        public double lat;
        public double lng;
        public String approvalStatus;

        public Port(String name, String location, int berths, int vessels, String status, String cargo, String capacity,
                String maintenance, String lastUpdated) {
            this(name, location,
                    "IN-" + name.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, name.length()))
                            + "-01",
                    "Port Authority (" + name + ")", berths, vessels, status, cargo, capacity, maintenance, lastUpdated,
                    0.0, 0.0, "APPROVED");
        }

        public Port(String name, String location, String code, String portAuthority, int berths, int vessels,
                String status, String cargo, String capacity, String maintenance, String lastUpdated, double lat,
                double lng, String approvalStatus) {
            this.name = name;
            this.location = location;
            this.code = code != null && !code.isBlank() ? code : "IN-PORT-01";
            this.portAuthority = portAuthority != null && !portAuthority.isBlank() ? portAuthority
                    : "Port Authority (" + name + ")";
            this.berths = berths;
            this.vessels = vessels;
            this.status = status != null ? status : "Operational";
            this.cargo = cargo != null ? cargo : "Containers";
            this.capacity = capacity != null ? capacity : "85%";
            this.maintenance = maintenance != null ? maintenance : "Clear";
            this.lastUpdated = lastUpdated != null ? lastUpdated : "Just now";
            this.lat = lat;
            this.lng = lng;
            this.approvalStatus = approvalStatus != null ? approvalStatus : "APPROVED";
        }

        public int occupiedBerths() {
            return Math.min(berths, vessels);
        }

        public int availableBerths() {
            return Math.max(0, berths - occupiedBerths());
        }
    }

    public static class UserAccount {
        public String uid;
        public String name;
        public String email;
        public String role;
        public String status;
        public String created;
        public String lastLogin;

        public UserAccount(String uid, String name, String email, String role, String status, String created,
                String lastLogin) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.created = created;
            this.lastLogin = lastLogin;
        }

        public UserAccount(String name, String email, String role, String status, String created, String lastLogin) {
            this(null, name, email, role, status, created, lastLogin);
        }
    }

    public static class ReportRow {
        public final String name;
        public final String period;
        public final String status;
        public final String generatedBy;
        public final String generatedDate;

        public ReportRow(String name, String period, String status) {
            this(name, period, status, "Aarav Mehta (Admin)", "14 Aug 2026, 18:30");
        }

        public ReportRow(String name, String period, String status, String generatedBy, String generatedDate) {
            this.name = name;
            this.period = period;
            this.status = status;
            this.generatedBy = generatedBy == null || generatedBy.isBlank() ? "Aarav Mehta (Admin)" : generatedBy;
            this.generatedDate = generatedDate == null || generatedDate.isBlank() ? "14 Aug 2026, 18:30"
                    : generatedDate;
        }
    }

    public static class PortRequest {
        public String requestId;
        public String portName;
        public String portCode;
        public String location;
        public String address;
        public String details;
        public String requesterName;
        public String requesterEmail;
        public String requesterPhone;
        public String proposedPaName;
        public String proposedPaEmail;
        public String status;
        public String requestDate;
        public String rejectionReason;
        public String documentRefs;
        public double lat;
        public double lng;
        public String aadhaarFileName = "Aadhaar_Card.pdf";
        public String aadhaarUrl = "https://res.cloudinary.com/snmxmesm/raw/upload/docktech_port_documents/Aadhaar_Card.pdf";
        public String aadhaarPublicId = "doc_aadhaar_01";
        public String aadhaarResourceType = "pdf";
        public String aadhaarFormat = "pdf";
        public String aadhaarUploadedAt = "15 Aug 2026";

        public String supportingFileName = "Port_Establishment_Proposal.pdf";
        public String supportingUrl = "https://res.cloudinary.com/snmxmesm/raw/upload/docktech_port_documents/Port_Establishment_Proposal.pdf";
        public String supportingPublicId = "doc_proposal_01";
        public String supportingResourceType = "pdf";
        public String supportingFormat = "pdf";
        public String supportingUploadedAt = "15 Aug 2026";

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, 0.0, 0.0);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate, double lat,
                double lng) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, "",
                    "Hazira_Port_Proposal.pdf, EIA_Clearance.pdf", lat, lng);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate,
                String rejectionReason, String documentRefs) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, rejectionReason, documentRefs,
                    0.0, 0.0);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate,
                String rejectionReason, String documentRefs, double lat, double lng) {
            this.requestId = requestId;
            this.portName = portName;
            this.portCode = portCode;
            this.location = location;
            this.address = address;
            this.details = details;
            this.requesterName = requesterName;
            this.requesterEmail = requesterEmail;
            this.requesterPhone = requesterPhone;
            this.proposedPaName = proposedPaName;
            this.proposedPaEmail = proposedPaEmail;
            this.status = status;
            this.requestDate = requestDate;
            this.rejectionReason = rejectionReason != null ? rejectionReason : "";
            this.documentRefs = documentRefs != null ? documentRefs : "Proposal_Document.pdf";
            this.lat = lat;
            this.lng = lng;
        }
    }
}
