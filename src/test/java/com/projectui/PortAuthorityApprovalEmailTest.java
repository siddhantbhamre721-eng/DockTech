package com.projectui;

import com.projectui.dao.PortRequestDao;
import com.projectui.service.EmailNotificationService;
import com.projectui.view.login.LoginPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PortAuthorityApprovalEmailTest {

    @Test
    public void testEmailRecipientValidationAndRejectionOfFakeAddresses() {
        EmailNotificationService service = new EmailNotificationService();

        // 1. Null, empty, or invalid email format must fail immediately
        Assertions.assertFalse(service.sendApprovalNotification(null, "Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "pa@gmail.com", "Pass#1234").join());
        Assertions.assertFalse(service.sendApprovalNotification("", "Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "pa@gmail.com", "Pass#1234").join());
        Assertions.assertFalse(service.sendApprovalNotification("invalid-email", "Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "pa@gmail.com", "Pass#1234").join());

        // 2. System-generated placeholder email pattern must be rejected
        Assertions.assertFalse(service.sendApprovalNotification("pa_mangaloreinternational@docktech.com", "Port of Mangalore", "IN-MNG-01", "REQ-2", "PA Mangalore", "pa_mangalore@docktech.com", "Pass#1234").join());

        // 3. Valid email format is validated by LoginPage.isEmail
        Assertions.assertTrue(LoginPage.isEmail("siddhantbhamre721@gmail.com"));
        Assertions.assertTrue(LoginPage.isEmail("admin.port@mumbaiport.gov.in"));
        Assertions.assertFalse(LoginPage.isEmail("invalid-email-address"));
    }

    @Test
    public void testRegisteredEmailPreservationInPortRequestDao() {
        Map<String, Object> reqData = new HashMap<>();
        reqData.put("requestId", "REQ-TEST-EMAIL-01");
        reqData.put("portName", "Port of Goa Deepwater");
        reqData.put("portCode", "IN-GOA-01");
        reqData.put("proposedPaEmail", "goa.authority@maritime.gov.in");
        reqData.put("requesterEmail", "director.goa@maritime.gov.in");

        com.projectui.view.admin.AdminDataStore.PortRequest parsed = PortRequestDao.parsePortRequestFromMap(reqData);
        Assertions.assertEquals("goa.authority@maritime.gov.in", parsed.proposedPaEmail);
        Assertions.assertEquals("director.goa@maritime.gov.in", parsed.requesterEmail);
    }

    @Test
    public void testSendPortAuthorityCredentialWebhookValidation() {
        EmailNotificationService service = new EmailNotificationService();
        // Validation tests
        Assertions.assertFalse(service.sendPortAuthorityCredentialWebhook("Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "pa@gmail.com", "", "Pass#123").join());
        Assertions.assertFalse(service.sendPortAuthorityCredentialWebhook("Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "", "pa@gmail.com", "Pass#123").join());
        Assertions.assertFalse(service.sendPortAuthorityCredentialWebhook("Port of Mumbai", "IN-BOM-01", "REQ-1", "PA Mumbai", "pa@gmail.com", "pa@gmail.com", "").join());
    }

    @Test
    public void testSendPortAuthorityCredentialWebhookSuccess() {
        EmailNotificationService service = new EmailNotificationService();
        boolean sent = service.sendPortAuthorityCredentialWebhook(
                "Port of Mumbai",
                "IN-BOM-01",
                "REQ-TEST-001",
                "Captain R. K. Sharma",
                "mumbai.portauthority@docktech.com",
                "siddhantbhamre721@gmail.com",
                "Pass#Test456"
        ).join();
        Assertions.assertTrue(sent, "Zapier Catch Hook must accept the credential payload with HTTP 200");
    }
}
