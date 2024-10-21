package management_service.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import management_service.EScooterService;
import management_service.entities.EScooter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Collections;
import java.util.List;

@RestController
public class EScooterController {

    private final EScooterService eScooterService;
    private final CircuitBreaker circuitBreaker;
    private final Logger logger = LoggerFactory.getLogger(EScooterController.class);

    public EScooterController(EScooterService eScooterService, CircuitBreaker circuitBreaker) {
        this.eScooterService = eScooterService;
        this.circuitBreaker = circuitBreaker;
    }

    @PostMapping("/api/management/create_escooter")
    public ResponseEntity<JsonNode> createScooter(@RequestBody EScooter scooter) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return circuitBreaker.executeSupplier(() -> {
                if (scooter.getName() == null || scooter.getLocation() == null) {
                    JsonNode errorResponse = objectMapper.createObjectNode()
                            .put("status", "error")
                            .put("message", "Name and location must not be null");
                    return ResponseEntity.badRequest().body(errorResponse);
                } else {
                    this.eScooterService.createEScooter(scooter);
                    JsonNode successResponse = objectMapper.createObjectNode()
                            .put("status", "success")
                            .put("message", "E-Scooter created successfully!");
                    return ResponseEntity.ok(successResponse);
                }
            });
        } catch (Exception e) {
            JsonNode errorResponse = objectMapper.createObjectNode()
                    .put("status", "error")
                    .put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/api/management/all_scooters")
    public List<EScooter> getAllScooters() {
        return circuitBreaker.executeSupplier(eScooterService::getEScooters);
    }

    @GetMapping("/api/management/get_scooter_state/{scooterId}")
    public ResponseEntity<JsonNode> getScooterState(@PathVariable String scooterId) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return circuitBreaker.executeSupplier(() -> {
                EScooter scooter = eScooterService.getScooterState(scooterId);
                if (scooter != null) {
                    String state = scooter.getState() != null ? scooter.getState() : "N/A";
                    JsonNode responseJson = objectMapper.createObjectNode()
                            .put("state", state);
                    return ResponseEntity.ok(responseJson);
                } else {
                    throw new RuntimeException("Scooter not found");
                    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body(objectMapper.createObjectNode().put("error", "Scooter not found"));
                }
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.createObjectNode().put("error", "An error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/api/management/available_scooters")
    private ResponseEntity<List<EScooter>> getAvailableScooters() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                List<EScooter> scooters = eScooterService.getAvailableScooters();
                return ResponseEntity.ok(scooters);
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PutMapping("/api/management/set_scooter_state/{scooterId}")
    public ResponseEntity<String> setScooterState(@PathVariable String scooterId, @RequestBody EScooter updatedScooter) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                EScooter scooter = eScooterService.setScooterState(scooterId, updatedScooter);
                if (scooter != null) {
                    return ResponseEntity.ok("Scooter state updated successfully");
                } else {
                    throw new RuntimeException("Scooter not found");
                    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Scooter not found");
                }
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PutMapping("/api/management/use_scooter/{scooterId}")
    public ResponseEntity<String> useScooter(@PathVariable String scooterId) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                EScooter scooter = eScooterService.useScooter(scooterId);
                if (scooter != null) {
                    logger.info("Scooter with id {} is now in use", scooterId);
                    return ResponseEntity.ok("Scooter is now in use");
                } else {
                    logger.warn("Scooter with id {} is not available or not found", scooterId);
                    throw new RuntimeException("Scooter not found or is not available" );
                    //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Scooter is not available or not found");
                }
            });
        } catch (HttpClientErrorException e) {
            logger.error("An error occurred while trying to use scooter with id {}: {}", scooterId, e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while trying to use scooter with id {}: {}", scooterId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
