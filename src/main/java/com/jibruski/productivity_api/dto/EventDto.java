package com.jibruski.productivity_api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.jibruski.productivity_api.model.Event;
import com.jibruski.productivity_api.model.EventReminder;
import com.jibruski.productivity_api.model.EventStatus;

import jakarta.validation.constraints.NotBlank;

public class EventDto {
    public record CreateRequest(
       @NotBlank String title,
       String description,
       Instant startDateTime,
       Instant endDateTime,
       LocalDate startDate,
       LocalDate endDate,
       boolean allDay,
       String location,
       String color,
       String recurrenceRule,
       List<Integer> reminderMinutes
    ) {}

    public record UpdateRequest(
        String title,
        String description,
        Instant startDateTime,
        Instant endDateTime,
        LocalDate startDate,
        LocalDate endDate,
        Boolean allDay,
        String location,
        String color,
        String recurrenceRule,          // new rule — replaces existing
        Boolean removeRecurrence,       // true = strip rule, make one-time
        List<Integer> reminderMinutes,  // null = don't touch, empty = remove all
        RecurrenceEditScope editScope   // ALL (only supported for now)
    ) {}

    public record Response(
        Long id,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        boolean allDay,
        String location,
        String color,
        EventStatus eventStatus,
        String recurrenceRule,
        Instant createdAt,
        Instant updatedAt,
        List<Integer> remindMinutes
    ) {
        public static Response fromEntity(Event event){
            return new Response(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                event.isAllDay(),
                event.getLocation(),
                event.getColor(),
                event.getEventStatus(),
                event.getRecurrenceRule(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getReminders().stream()
                .map(EventReminder::getRemindBeforeMinutes)
                .toList() 
            );
        }
    }

    public record OccurrenceResponse(
        Long seriesId,
        String title,
        String description,
        Instant startDateTime,
        Instant endDateTime,
        boolean allDay,
        String location,
        String color,
        boolean isRecurring,
        EventStatus eventStatus
    ) {}
}
