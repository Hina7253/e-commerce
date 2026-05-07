package com.ecommerce.service;

import com.ecommerce.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DeliveryService {

    @Value("${delivery.api.url}")
    private String deliveryApiUrl;

    @Value("${delivery.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> createShipment(Order order) {
        Map<String, Object> shipmentRequest = new HashMap<>();
        shipmentRequest.put("orderId", order.getId());
        shipmentRequest.put("customerName", order.getShippingAddress().getFullName());
        shipmentRequest.put("customerPhone", order.getShippingAddress().getPhoneNumber());
        shipmentRequest.put("address", order.getShippingAddress().getAddressLine1());
        shipmentRequest.put("city", order.getShippingAddress().getCity());
        shipmentRequest.put("postalCode", order.getShippingAddress().getPostalCode());
        shipmentRequest.put("weight", calculateOrderWeight(order));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(shipmentRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    deliveryApiUrl + "/shipments",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> result = new HashMap<>();
            result.put("partner", "ShipRocket");
            result.put("trackingId", UUID.randomUUID().toString());
            result.put("trackingUrl", "https://track.shiprocket.in/" + result.get("trackingId"));
            return result;
        } catch (Exception e) {
            // Fallback: generate mock tracking
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("partner", "MockDelivery");
            fallback.put("trackingId", "MOCK-" + UUID.randomUUID().toString());
            fallback.put("trackingUrl", "https://mocktracking.com/" + fallback.get("trackingId"));
            return fallback;
        }
    }

    public Map<String, Object> getTrackingStatus(String trackingId) {
        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("status", "IN_TRANSIT");
        trackingInfo.put("currentLocation", "Sorting Facility");
        trackingInfo.put("estimatedDelivery", "In 3-5 days");
        return trackingInfo;
    }

    private double calculateOrderWeight(Order order) {
        return order.getItems().size() * 0.5; // 0.5kg per item approx
    }
}
