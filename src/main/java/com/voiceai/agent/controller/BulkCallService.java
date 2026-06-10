package com.voiceai.agent.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;


@Service
public class BulkCallService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhone;

    @Value("${server.ngrok-url}")
    private String ngrokUrl;

    public record CallResult(String phone, String name, String status, String callSid) {}

    public List<CallResult> makeCallsToGuests(List<GuestContact> guests) {
        Twilio.init(accountSid, authToken);
        List<CallResult> results = new ArrayList<>();

        for (GuestContact guest : guests) {
            try {
                // 2 second gap between calls
                Thread.sleep(2000);

                Call call = Call.creator(
                    new PhoneNumber(guest.phone()),
                    new PhoneNumber(twilioPhone),
                    new URI(ngrokUrl + "/voice-invitation?name=" + 
                            java.net.URLEncoder.encode(guest.name(), "UTF-8"))
                ).create();

                System.out.println("Call initiated to: " + guest.name() + 
                                   " (" + guest.phone() + ") SID: " + call.getSid());
                results.add(new CallResult(guest.phone(), guest.name(), 
                                          "INITIATED", call.getSid()));

            } catch (Exception e) {
                System.err.println("Call failed for: " + guest.phone() + 
                                   " Error: " + e.getMessage());
                results.add(new CallResult(guest.phone(), guest.name(), 
                                          "FAILED", null));
            }
        }
        return results;
    }

    public record GuestContact(String name, String phone) {}
}