package com.jibruski.productivity_api.dto;

import java.time.LocalDate;

import com.jibruski.productivity_api.model.FrequencyType;
import com.jibruski.productivity_api.model.Habit;
import com.jibruski.productivity_api.model.HabitType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HabitDto {
    public record  CreateRequest(
        @NotBlank String name,
        String description,
        @NotNull HabitType habitType,
        @NotNull FrequencyType frequencyType,
        Double targetValue,
        Integer customDayMask,
        String unit,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        String color
    ) {}

    public record UpdateRequest(
        String name,
        String description,
        FrequencyType frequencyType,
        Integer customDayMask,
        Double targetValue,
        String unit,
        LocalDate endDate,
        Boolean active,
        String color
    ) {}

    public record Response(
        Long id,
        String name,
        String description,
        HabitType habitType,
        FrequencyType frequencyType,
        Integer customDayMask,
        Double targetValue,
        String unit,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        boolean deleted,
        int currentStreak,
        int longestStreak
    ) {
        public static Response fromEntity(Habit habit, int currentStreak, int longestStreak){
            return new Response(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getHabitType(),
                habit.getFrequencyType(),
                habit.getCustomDaysMask(),
                habit.getTargetValue(),
                habit.getUnit(),
                habit.getStartDate(),
                habit.getEndDate(),
                habit.getColor(),
                habit.isDeleted(),
                currentStreak,
                longestStreak
            );
        }
    }
}
