package com.example.mediators;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

public class OrderValidationMediator extends AbstractMediator {

    @Override
    public boolean mediate(MessageContext context) {
        try {
            // Retrieve the payload from a specific property or transport property
            String payload = (String) context.getProperty("REST_FULL_REQUEST");

            // Check if payload is missing or empty
            if (payload == null || payload.isEmpty()) {
                context.setProperty("orderValidationSuccess", false);  // Set failure property
                log.error("Payload is missing or empty");
                return true;  // Continue processing the sequence
            }

            // Basic field validation using string checks
            if (!payload.contains("orderID") || !payload.contains("customerID") ||
                !payload.contains("productID") || !payload.contains("quantity") ||
                !payload.contains("location")) {
                context.setProperty("orderValidationSuccess", false);  // Set failure property
                log.error("Missing required fields in payload");
                return true;  // Continue processing the sequence
            }

            // Extract the value of "quantity" from the payload using simple parsing
            int quantity = extractQuantity(payload);
            if (quantity <= 0) {
                context.setProperty("orderValidationSuccess", false);  // Set failure property
                log.error("Invalid quantity. Quantity must be a positive integer.");
                return true;  // Continue processing the sequence
            }

            // Validation successful
            context.setProperty("orderValidationSuccess", true);  // Set success property
            log.info("Order payload validation successful: " + payload);
            return true;  // Continue processing the sequence

        } catch (Exception e) {
            // Handle unexpected errors
            context.setProperty("orderValidationSuccess", false);  // Set failure property
            log.error("Error during payload validation: " + e.getMessage());
            return true;  // Continue processing the sequence (but with failure)
        }
    }

    private int extractQuantity(String payload) throws NumberFormatException {
        String key = "\"quantity\":";
        int startIndex = payload.indexOf(key);
        if (startIndex == -1) {
            throw new NumberFormatException("Quantity field not found in payload");
        }

        startIndex += key.length();
        int endIndex = payload.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = payload.indexOf("}", startIndex); // Handle last field in JSON
        }

        String quantityStr = payload.substring(startIndex, endIndex).trim();
        return Integer.parseInt(quantityStr);
    }
}
