package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.CatalogRepository;
import id.adiputera.demo.cms.admin.repository.EventRepository;
import id.adiputera.demo.cms.dto.EventDTO;
import id.adiputera.demo.cms.entity.Catalog;
import id.adiputera.demo.cms.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Management Service class.
 *
 * @author Yusuf F. Adiputera
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventManagementService {

    private final EventRepository eventRepository;
    private final CatalogRepository catalogRepository;
    private final CatalogSyncService catalogSyncService;

    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("eventCatalog", id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
                .orElseGet(() -> catalogRepository.save(Catalog.builder()
                        .catalogId("eventCatalog")
                        .version(id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
                        .build()));
    }

    public List<EventDTO> getAllEvents() {
        Catalog stagedCatalog = getStagedCatalog();
        List<Event> events = eventRepository.findAllByCatalog(stagedCatalog, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        java.util.Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(events, Event.class, "eventCatalog");
        
        return events.stream().map(event -> {
            EventDTO dto = mapToDTO(event);
            dto.setSyncStatus(syncStatusMap.getOrDefault(event.getSyncKey(), "UNKNOWN"));
            return dto;
        }).collect(Collectors.toList());
    }

    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToDTO(event);
    }

    public EventDTO createEvent(EventDTO request) {
        Catalog catalog = getStagedCatalog();
        
        Event event = Event.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();
        event.setCatalog(catalog);
        
        Event savedEvent = eventRepository.save(event);
        return mapToDTO(savedEvent);
    }

    public EventDTO updateEvent(Long id, EventDTO request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
                
        event.setTitle(request.getTitle());
        event.setSlug(request.getSlug());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        
        Event updatedEvent = eventRepository.save(event);
        return mapToDTO(updatedEvent);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        eventRepository.delete(event);
    }

    private EventDTO mapToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .slug(event.getSlug())
                .description(event.getDescription())
                .location(event.getLocation())
                .build();
    }
}
