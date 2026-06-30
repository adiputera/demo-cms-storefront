package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.service.EventManagementService;
import id.adiputera.demo.cms.dto.EventDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EventManagementController {

    private final EventManagementService eventManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEvents() {
        log.info("GET /api/cms/events");
        List<EventDTO> events = eventManagementService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable("id") Long id) {
        log.info("GET /api/cms/events/{}", id);
        EventDTO event = eventManagementService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@Valid @RequestBody EventDTO request) {
        log.info("POST /api/cms/events - Creating event: {}", request.getTitle());
        EventDTO createdEvent = eventManagementService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", createdEvent));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody EventDTO request) {
        log.info("PUT /api/cms/events/{}", id);
        EventDTO updatedEvent = eventManagementService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable("id") Long id) {
        log.info("DELETE /api/cms/events/{}", id);
        eventManagementService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }
}
