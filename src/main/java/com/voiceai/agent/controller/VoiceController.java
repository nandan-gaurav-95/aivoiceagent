package com.voiceai.agent.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

@RestController
public class VoiceController {

    @Value("${server.ngrok-url}")
    private String ngrokUrl;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhone;

    @Autowired
    private BulkCallService bulkCallService;

    // Existing - Twilio webhook
    @PostMapping(value = "/voice-invitation", produces = "application/xml")
    public String weddingInvitation(@RequestParam(defaultValue = "Guest") String name) {
        String wsUrl = ngrokUrl.replace("https://", "wss://") + "/twilio-stream";
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Connect>
                    <Stream url="%s">
                        <Parameter name="guestName" value="%s"/>
                    </Stream>
                </Connect>
            </Response>
            """.formatted(wsUrl, name);
    }

    @PostMapping(value = "/voice", produces = "application/xml")
    public String incomingCall() {
        String wsUrl = ngrokUrl.replace("https://", "wss://") + "/twilio-stream";
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Connect>
                    <Stream url="%s"/>
                    </Connect>
            </Response>
            """.formatted(wsUrl);
    }

    // Single test call
    @GetMapping("/make-call")
    public String makeCall() throws Exception {
        Twilio.init(accountSid, authToken);
        Call call = Call.creator(
            new PhoneNumber("+917843069930"),
            new PhoneNumber(twilioPhone),
            new URI(ngrokUrl + "/voice")
        ).create();
        return "Call started: " + call.getSid();
    }

    // Bulk call - JSON body
    @PostMapping("/bulk-call")
    public List<BulkCallService.CallResult> bulkCall(
            @RequestBody List<Map<String, String>> guests) {

        List<BulkCallService.GuestContact> contacts = guests.stream()
            .map(g -> new BulkCallService.GuestContact(
                g.get("name"), g.get("phone")))
            .toList();

        return bulkCallService.makeCallsToGuests(contacts);
    }

    // Bulk call - CSV upload
    @PostMapping("/bulk-call/csv")
    public List<BulkCallService.CallResult> bulkCallCsv(
            @RequestParam("file") MultipartFile file) throws Exception {

        List<BulkCallService.GuestContact> contacts = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream()));

        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) { firstLine = false; continue; } // header skip
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                contacts.add(new BulkCallService.GuestContact(
                    parts[0].trim(), parts[1].trim()));
            }
        }

        return bulkCallService.makeCallsToGuests(contacts);
    }
}