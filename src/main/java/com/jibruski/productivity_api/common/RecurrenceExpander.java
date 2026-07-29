package com.jibruski.productivity_api.common;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jibruski.productivity_api.dto.EventDto;
import com.jibruski.productivity_api.model.Event;
import com.jibruski.productivity_api.model.EventStatus;

import net.fortuna.ical4j.model.Recur;

@Component
public class RecurrenceExpander {

    public List<EventDto.OccurrenceResponse> expand(Event event, LocalDate from, LocalDate to,
            ZoneId userZone) {

        // Non-recurring event — just check if it falls in range
        if (event.getRecurrenceRule() == null || event.getRecurrenceRule().isBlank()) {
            LocalDate eventDate = event.getStartDateTime()
                .atZone(userZone)
                .toLocalDate();
            if (!eventDate.isBefore(from) && !eventDate.isAfter(to)) {
                return List.of(toOccurrence(event, event.getStartDateTime(),
                    event.getEndDateTime()));
            }
            return List.of();
        }

        // Recurring event — expand via ical4j
        return expandRecurring(event, from, to, userZone);
    }

    private List<EventDto.OccurrenceResponse> expandRecurring(Event event, LocalDate from,
            LocalDate to, ZoneId userZone) {

        try {
            // Build the recurrence rule
            Recur<ZonedDateTime> recur = new Recur<>(event.getRecurrenceRule());

            // Convert event start to ZonedDateTime in user's timezone
            ZonedDateTime eventStart = event.getStartDateTime().atZone(userZone);

            // Define expansion window
            ZonedDateTime windowStart = from.atStartOfDay(userZone);
            ZonedDateTime windowEnd   = to.atTime(23, 59, 59).atZone(userZone);

            // Get all occurrence start times within the window
            List<ZonedDateTime> occurrenceDates = recur.getDates(
                eventStart,
                windowStart,
                windowEnd
            );

            // Calculate event duration to apply to each occurrence
            long durationSeconds = Duration.between(
                event.getStartDateTime(),
                event.getEndDateTime()
            ).getSeconds();

            return occurrenceDates.stream()
                .map(occurrenceStart -> {
                    Instant start = occurrenceStart.toInstant();
                    Instant end   = start.plusSeconds(durationSeconds);
                    return toOccurrence(event, start, end);
                })
                .toList();

        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid recurrence rule: " + event.getRecurrenceRule(), e);
        }
    }

    private EventDto.OccurrenceResponse toOccurrence(Event event, Instant start, Instant end) {
        return new EventDto.OccurrenceResponse(
            event.getId(),        // seriesId — links back to parent event
            event.getTitle(),
            event.getDescription(),
            start,
            end,
            event.isAllDay(),
            event.getLocation(),
            event.getColor(),
            event.getRecurrenceRule() != null,
            resolveDisplayStatus(event, start, end)
        );
    }

    private EventStatus resolveDisplayStatus(Event event, Instant start, Instant end) {
        Instant now = Instant.now();
        if (now.isBefore(start)) return EventStatus.UPCOMING;
        if (now.isAfter(end))    return EventStatus.PASSED;
        return EventStatus.ONGOING;
    }
}