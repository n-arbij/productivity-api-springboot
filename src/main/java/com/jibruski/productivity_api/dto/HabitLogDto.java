package com.jibruski.productivity_api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.jibruski.productivity_api.model.HabitLog;

import jakarta.validation.constraints.NotNull;

public class HabitLogDto {

    public record  LogRequest(
        @NotNull LocalDate logDate,
        Boolean completed,
        Double value,
        String notes
    ) {}

    public record  Response(
        Long id,
        Long habitId,
        LocalDate logDate,
        boolean completed,
        Double value,
        String notes,
        Instant loggedAt
    ) {
        public static Response fromEntity(HabitLog habitLog){
            return new Response(
                habitLog.getId(),
                habitLog.getHabitId(),
                habitLog.getLogDate(),
                habitLog.isCompleted(),
                habitLog.getValue(),
                habitLog.getNotes(),
                habitLog.getLoggedAt()
            );
        }
    }

    public record WeekSummary(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<DaySummary> days
    ){}

    public record DaySummary(
        LocalDate date,
        boolean scheduled,
        boolean completed,
        Double value
    ) {}
}
