package com.jibruski.productivity_api.dto;

import com.jibruski.productivity_api.model.EventReminder;

public class EventReminderDto {
     public record Request(
        int minutes
    ) {}

    public record Response(
        Long id,
        Integer reminderBeforeMinutes,
        boolean notified
    ) {
        public static Response fromEntity(EventReminder reminder){
            return new Response(
                reminder.getId(),
                reminder.getRemindBeforeMinutes(),
                reminder.isNotified()
            );
        }
    }
}
