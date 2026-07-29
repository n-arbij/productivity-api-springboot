package com.jibruski.productivity_api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.productivity_api.dto.EventDto;
import com.jibruski.productivity_api.dto.EventDto.OccurrenceResponse;
import com.jibruski.productivity_api.dto.EventReminderDto;
import com.jibruski.productivity_api.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto.Response>> getAll(){
        return ResponseEntity.ok(eventService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto.Response> getById(@PathVariable Long id){
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping("/range")
    public ResponseEntity<List<OccurrenceResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(eventService.getByDateRange(from, to));
    }

    @GetMapping("/reminders/{eventId}")
    public ResponseEntity<List<EventReminderDto.Response>> getRemindersByEvent(@PathVariable Long eventId){
        return ResponseEntity.ok(eventService.getRemindersByEvent(eventId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EventDto.CreateRequest request){
        eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addReminder(@PathVariable Long id, @RequestParam int minutes) {
        eventService.addReminder(id, minutes);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto.Response> update(@PathVariable Long id, @Valid @RequestBody EventDto.UpdateRequest request){
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id){
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reminders/{id}")
    public ResponseEntity<EventReminderDto.Response> removeReminder(@PathVariable Long id){
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }
}
