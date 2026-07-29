package com.jibruski.productivity_api.dto;

import java.time.Instant;

import com.jibruski.productivity_api.model.PomodoroSession;
import com.jibruski.productivity_api.model.SessionStatus;
import com.jibruski.productivity_api.model.SessionType;

import jakarta.validation.constraints.NotNull;

public class PomodoroDto {
     public record CreateRequest(
        @NotNull SessionType sessionType,
        @NotNull Integer plannedDurationMinutes
    ) {}

    public record UpdateRequest(
        @NotNull SessionStatus status
    ) {}

    public record Response(
        Long id,
        SessionType sessionType,
        SessionStatus status,
        Instant startTime,
        Instant endTIme,
        Integer plannedDurationMinutes,
        Integer actualDurationMinutes,
        boolean completed
    ) {
        public static Response fromEntity(PomodoroSession session){
            Integer actual = null;
            if(session.getStartTime() != null && session.getEndTime() != null) {
                actual = (int) java.time.Duration.between(
                    session.getStartTime(),
                    session.getEndTime()
                ).toMinutes();
            }

            return new Response(
                session.getId(),
                session.getSessionType(),
                session.getStatus(),
                session.getStartTime(),
                session.getEndTime(),
                session.getPlannedDurationMinutes(),
                actual,
                session.isCompleted()
            );
        }
    }

    public record SummaryResponse(
        String date,
        int completeSessions,
        int cancelledSessions,
        int totalFocusMinutes,
        int totalBreakMinutes
    ) {}
}
