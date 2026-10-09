package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Boat;
import com.example.demo.model.SlipType;
import com.example.demo.model.WaitlistEntry;
import com.example.demo.repository.BoatRepository;
import com.example.demo.repository.SlipRepository;
import com.example.demo.repository.SlipTypeRepository;
import com.example.demo.repository.WaitlistRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class WaitlistController {

    private final WaitlistRepository waitlistRepository;
    private final BoatRepository boatRepository;
    private final SlipTypeRepository slipTypeRepository;
    private final SlipRepository slipRepository;

    public WaitlistController(
            WaitlistRepository waitlistRepository,
            BoatRepository boatRepository,
            SlipTypeRepository slipTypeRepository,
            SlipRepository slipRepository) {

        this.waitlistRepository = waitlistRepository;
        this.boatRepository = boatRepository;
        this.slipTypeRepository = slipTypeRepository;
        this.slipRepository = slipRepository;
    }
    
    @GetMapping("/waitlist")
    public ResponseEntity<?> getWaitlist(HttpSession session) {

        Object customerIdObject = session.getAttribute("customer_id");

        Long customerId = customerIdObject instanceof Number number
                ? number.longValue()
                : null;

        List<Map<String, Object>> summary = new ArrayList<>();

        for (SlipType slipType : slipTypeRepository.findAll()) {

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("slipTypeId", slipType.getSlipTypeId());
            row.put("sizeFt", slipType.getSizeFt());
            row.put(
                    "availableCount",
                    slipRepository.countAvailable(slipType.getSlipTypeId())
            );
            row.put(
                    "waitingCount",
                    waitlistRepository.countWaiting(slipType.getSlipTypeId())
            );

            summary.add(row);
        }

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("ok", true);
        body.put("loggedIn", customerId != null);
        body.put("slipTypes", summary);

        if (customerId != null) {
        	
        	Boat customerBoat =
        	        boatRepository.findFirstByCustomerId(customerId);

        	if (customerBoat != null) {
        	    Map<String, Object> boatInfo = new LinkedHashMap<>();

        	    boatInfo.put("boatId", customerBoat.getBoatId());
        	    boatInfo.put("boatName", customerBoat.getBoatName());
        	    boatInfo.put("boatLengthFt", customerBoat.getBoatLengthFt());

        	    body.put("boat", boatInfo);
        	}

            List<Map<String, Object>> entries = new ArrayList<>();

            for (WaitlistEntry entry :
                    waitlistRepository.findByCustomerId(customerId)) {

                Boat boat = boatRepository.findForCustomer(
                        entry.getBoatId(),
                        customerId
                );

                SlipType slipType = slipTypeRepository.findById(
                        entry.getSlipTypeId()
                );

                Map<String, Object> row =
                        waitlistMap(entry, boat, slipType);

                row.put(
                        "position",
                        waitlistRepository.getPosition(entry)
                );

                entries.add(row);
            }

            body.put("entries", entries);
        }

        return ResponseEntity.ok(body);
    }
    
    @PostMapping("/waitlist")
    public ResponseEntity<?> joinWaitlist(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        Object customerIdObject = session.getAttribute("customer_id");

        if (!(customerIdObject instanceof Number number)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "ok", false,
                            "message", "Please log in before joining the wait list."
                    ));
        }

        long customerId = number.longValue();

        Object boatIdObject = request.get("boatId");

        if (boatIdObject == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "A boat is required."
                    ));
        }

        long boatId;

        try {
            boatId = Long.parseLong(boatIdObject.toString());
        } catch (NumberFormatException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Invalid boat."
                    ));
        }

        Boat boat = boatRepository.findForCustomer(boatId, customerId);

        if (boat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "ok", false,
                            "message", "Boat not found."
                    ));
        }

        BigDecimal boatLength = boat.getBoatLengthFt();

        if (boatLength == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Boat length is required."
                    ));
        }

        SlipType slipType =
                slipTypeRepository.findRequiredForBoatLength(boatLength);

        if (slipType == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message",
                            "No slip type can accommodate this boat."
                    ));
        }

        int available =
                slipRepository.countAvailable(slipType.getSlipTypeId());

        if (available > 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message",
                            "A suitable slip is currently available. "
                            + "Please make a reservation instead."
                    ));
        }

        if (waitlistRepository.hasActiveEntry(
                customerId,
                boatId,
                slipType.getSlipTypeId())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message",
                            "This boat is already on the wait list "
                            + "for the required slip size."
                    ));
        }

        WaitlistEntry entry = new WaitlistEntry();
        entry.setCustomerId(customerId);
        entry.setBoatId(boatId);
        entry.setSlipTypeId(slipType.getSlipTypeId());
        entry.setStatus("WAITING");

        waitlistRepository.insert(entry);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", "You have been added to the wait list.");
        body.put("waitlistId", entry.getWaitlistId());
        body.put("slipTypeId", slipType.getSlipTypeId());
        body.put("sizeFt", slipType.getSizeFt());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    
    private Map<String, Object> waitlistMap(
            WaitlistEntry entry,
            Boat boat,
            SlipType slipType) {

        Map<String, Object> row = new LinkedHashMap<>();

        row.put("waitlistId", entry.getWaitlistId());
        row.put("customerId", entry.getCustomerId());
        row.put("boatId", entry.getBoatId());
        row.put("slipTypeId", entry.getSlipTypeId());
        row.put("joinedAt", entry.getJoinedAt());
        row.put("status", entry.getStatus());

        if (boat != null) {
            row.put("boatName", boat.getBoatName());
            row.put("boatLengthFt", boat.getBoatLengthFt());
            row.put("boatType", boat.getBoatType());
            row.put("registrationNumber", boat.getRegistrationNumber());
        }

        if (slipType != null) {
            row.put("sizeFt", slipType.getSizeFt());
            row.put("ratePerFoot", slipType.getRatePerFoot());
            row.put("electricFee", slipType.getElectricFee());
        }

        return row;
    }
}