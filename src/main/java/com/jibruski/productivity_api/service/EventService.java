package com.jibruski.productivity_api.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jibruski.productivity_api.common.RecurrenceExpander;
import com.jibruski.productivity_api.dto.EventDto;
import com.jibruski.productivity_api.dto.EventDto.OccurrenceResponse;
import com.jibruski.productivity_api.dto.EventReminderDto;
import com.jibruski.productivity_api.model.Event;
import com.jibruski.productivity_api.model.EventReminder;
import com.jibruski.productivity_api.model.EventStatus;
import com.jibruski.productivity_api.repository.EventReminderRepository;
import com.jibruski.productivity_api.repository.EventRepository;
import com.jibruski.productivity_api.security.CurrentUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Recur;

@Service
@RequiredArgsConstructor
public class EventService {
    private final CurrentUserService userService;
    private final EventRepository eventRepository;
    private final EventReminderRepository reminderRepository;
    private final RecurrenceExpander recurrenceExpander;

    public List<EventDto.Response> getAll(){
        Long userId = userService.getCurrentUserId();
        return eventRepository.findByUserId(userId).stream()
            .map(EventDto.Response::fromEntity)
            .toList();
    }

    public EventDto.Response getById(Long id){
        Event event = getEventById(id);
        return EventDto.Response.fromEntity(event);
    }

    public List<OccurrenceResponse> getByDateRange(LocalDate from, LocalDate to) {
        Long userId = userService.getCurrentUserId();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be before to date");
        }

        Instant rangeStart = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant rangeEnd   = to.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Event> candidates = eventRepository.findCandidateEvents(
            userId, rangeEnd, rangeStart
        );
        
        return candidates.stream()
            .flatMap(event -> recurrenceExpander
                .expand(event, from, to, ZoneId.systemDefault()).stream())
            .sorted(Comparator.comparing(OccurrenceResponse::startDateTime))
            .toList();
    }

    public List<EventReminderDto.Response> getRemindersByEvent(Long eventId){
        Event event = getEventById(eventId);
        return reminderRepository.findByEvent(event).stream()
            .map(EventReminderDto.Response::fromEntity)
            .toList();
    }

    @Transactional
    public void createEvent(EventDto.CreateRequest request){

        if (request.recurrenceRule() != null) {
            validateRecurrenceRule(request.recurrenceRule());
        }
        Event event = new Event();
        event.setUserId(userService.getCurrentUserId());
        event.setTitle(request.title());
        event.setDescription(request.description());

        if(request.allDay()){
            event.setStartDateTime(request.startDate().atStartOfDay().toInstant(ZoneOffset.UTC));
            event.setEndDateTime(request.endDate().atStartOfDay().toInstant(ZoneOffset.UTC));
        } else{
            event.setStartDateTime(request.startDateTime());
            event.setEndDateTime(request.endDateTime());
        }
        event.setLocation(request.location());
        event.setRecurrenceRule(request.recurrenceRule());
        event.setColor(request.color() != null ? request.color() : "#CB410B");
        event.setEventStatus(resolveDisplayStatus(event));
        Event saved = eventRepository.save(event);

        List<EventReminder> reminders = request.reminderMinutes() != null ? 
            request.reminderMinutes().stream()
                .map(minutes -> EventReminder.builder()
                        .event(saved)
                        .remindBeforeMinutes(minutes)
                        .notified(false)
                        .build())
                .toList()
            : List.of();
        
        if(!reminders.isEmpty()){
            reminderRepository.saveAll(reminders);
        }
    }

    @Transactional
    public EventDto.Response updateEvent(Long id, EventDto.UpdateRequest request){
        Event event = getEventById(id);

        if(request.title() != null) event.setTitle(request.title());
        if(request.description() != null) event.setDescription(request.description());
        if(request.startDateTime() != null) event.setStartDateTime(request.startDateTime());
        if(request.endDateTime() != null) event.setEndDateTime(request.endDateTime());
       
        if (request.startDateTime() != null || request.endDateTime() != null) {
            Instant newStart = request.startDateTime() != null
                ? request.startDateTime()
                : event.getStartDateTime();
            Instant newEnd = request.endDateTime() != null
                ? request.endDateTime()
                : event.getEndDateTime();

            if (newStart.isAfter(newEnd)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            event.setStartDateTime(newStart);
            event.setEndDateTime(newEnd);
        }

        if (request.allDay() != null) {
            event.setAllDay(request.allDay());

            if (request.allDay()) {
                LocalDate startDate = request.startDate() != null
                    ? request.startDate()
                    : event.getStartDateTime()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                LocalDate endDate = request.endDate() != null
                    ? request.endDate()
                    : event.getEndDateTime()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException(
                        "Start date must be before or equal to end date");
                }

                event.setStartDateTime(startDate.atStartOfDay().toInstant(ZoneOffset.UTC));
                event.setEndDateTime(endDate.atStartOfDay().toInstant(ZoneOffset.UTC));

            } else {
                if (request.startDateTime() == null || request.endDateTime() == null) {
                    throw new IllegalArgumentException(
                        "StartDateTime and EndDateTime are required when converting " +
                        "from an all-day event to a timed event");
                }
                if (request.startDateTime().isAfter(request.endDateTime())) {
                    throw new IllegalArgumentException("Start time must be before end time");
                }
                event.setStartDateTime(request.startDateTime());
                event.setEndDateTime(request.endDateTime());
            }
        }

        if(request.location() != null) event.setLocation(request.location());
        if(request.color() != null) event.setColor(request.color());
        if(request.recurrenceRule() != null){
            validateRecurrenceRule(request.recurrenceRule());
            event.setRecurrenceRule(request.recurrenceRule());
        }else if (request.removeRecurrence() != null && request.removeRecurrence()) {
            event.setRecurrenceRule(null);
        }

        if (request.reminderMinutes() != null) {
        // Replace all existing reminders
            reminderRepository.deleteByEvent(event);
            if (!request.reminderMinutes().isEmpty()) {
                List<EventReminder> reminders = request.reminderMinutes().stream()
                    .map(minutes -> EventReminder.builder()
                        .event(event)
                        .remindBeforeMinutes(minutes)
                        .notified(false)
                        .build())
                    .toList();
                reminderRepository.saveAll(reminders);
            }
        }

        eventRepository.save(event);
        return EventDto.Response.fromEntity(event);
    }

    public void cancelEvent(Long eventId){
        Event event = getEventById(eventId);
        event.setEventStatus(EventStatus.CANCELLED);
        resolveDisplayStatus(event);
        eventRepository.save(event);
    }

    @Transactional
    public void addReminder(Long eventId, int minutes){
        Long userId = userService.getCurrentUserId();
        Event event = eventRepository.findByIdAndUserId(eventId, userId).orElseThrow(
            () -> new RuntimeException("Event not found with id: " + eventId)
        );

        boolean alreadyExists = reminderRepository.existsByEventAndRemindBeforeMinutes(event, minutes);

        if(alreadyExists){
            throw new IllegalArgumentException("A reminder for " + minutes + " minutes already exists for this event");
        }
        EventReminder reminder = EventReminder.builder()
            .event(getEventById(eventId))
            .remindBeforeMinutes(minutes)
            .notified(false)
            .build();
        
        reminderRepository.save(reminder);
    }

    public void removeReminder(Long id){
        EventReminder reminder = reminderRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Event Reminder not found")
        );

        getEventById(reminder.getEvent().getId());
        reminderRepository.delete(reminder);
    }

    private void validateRecurrenceRule(String rule) {
        try {
            new Recur<>(rule);  // ical4j will throw if the string is malformed
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid recurrence rule: " + rule);
        }
    }

    private Event getEventById(Long id){
        Long userId = userService.getCurrentUserId();
        Event event = eventRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Event not found")
        );

        if(!event.getUserId().equals(userId)){
            throw new RuntimeException("Access denied");
        }

        return event;
    }

    private EventStatus resolveDisplayStatus(Event event){
        Instant now = Instant.now();
        if(now.isBefore(event.getStartDateTime())) return EventStatus.UPCOMING;
        if(now.isAfter(event.getEndDateTime())) return EventStatus.PASSED;
        return EventStatus.ONGOING;
    }
}
