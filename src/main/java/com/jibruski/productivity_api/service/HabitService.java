package com.jibruski.productivity_api.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jibruski.exceptionstarter.exceptions.BusinessRuleException;
import com.jibruski.exceptionstarter.exceptions.ResourceNotFoundException;
import com.jibruski.productivity_api.dto.HabitDto;
import com.jibruski.productivity_api.dto.HabitLogDto;
import com.jibruski.productivity_api.model.FrequencyType;
import com.jibruski.productivity_api.model.Habit;
import com.jibruski.productivity_api.model.HabitLog;
import com.jibruski.productivity_api.model.HabitType;
import com.jibruski.productivity_api.repository.HabitLogRepository;
import com.jibruski.productivity_api.repository.HabitRepository;
import com.jibruski.productivity_api.security.CurrentUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitLogRepository logRepository;
    private final CurrentUserService userService;

    public List<HabitDto.Response> getAll(){
        Long userId = userService.getCurrentUserId();
        return habitRepository.findByUserIdAndDeletedFalse(userId).stream()
        .map(habit -> {
            StreakResult streak = calculateStreak(habit);
            return HabitDto.Response.fromEntity(habit, streak.current(), streak.longest());
        }).toList();
    }

    public HabitDto.Response getById(Long id){
        Long userId = userService.getCurrentUserId();
        Habit habit = findOwnedHabit(id, userId);
        StreakResult streak = calculateStreak(habit);
        return HabitDto.Response.fromEntity(habit, streak.current(), streak.longest());
    }

    @Transactional
    public void createHabit(HabitDto.CreateRequest request){
        validate(request);

        Habit habit = new Habit();
        habit.setUserId(userService.getCurrentUserId());
        habit.setName(request.name());
        habit.setDescription(request.description());
        habit.setHabitType(request.habitType());
        habit.setFrequencyType(request.frequencyType());
        habit.setCustomDaysMask(request.customDayMask() != null ? request.customDayMask(): null);
        habit.setTargetValue(request.targetValue() != null ? request.targetValue() : null);
        habit.setUnit(request.unit() != null ? request.unit(): null);
        habit.setStartDate(request.startDate());
        habit.setEndDate(request.endDate() != null ? request.endDate() : null);
        habit.setColor(request.color() != null ? request.color() : "#6366f1");

        habitRepository.save(habit);
    }

    @Transactional
    public HabitDto.Response updateHabit(Long id, HabitDto.UpdateRequest request){
        Habit habit = findOwnedHabit(id, userService.getCurrentUserId());

        if(request.name() != null) habit.setName(request.name());
        if(request.description() != null) habit.setDescription(request.description());
        if(request.frequencyType() != null) habit.setFrequencyType(request.frequencyType());
        if(request.customDayMask() != null) habit.setCustomDaysMask(request.customDayMask());
        if(request.targetValue() != null) habit.setTargetValue(request.targetValue());
        if(request.unit() != null) habit.setUnit(request.unit());
        if(request.color() != null) habit.setColor(request.color());
        Habit saved = habitRepository.save(habit);

        StreakResult streak = calculateStreak(saved);
        return HabitDto.Response.fromEntity(habit, streak.current, streak.longest);
    }

    @Transactional
    public void deleteHabit(Long id){
        Habit habit = findOwnedHabit(id, userService.getCurrentUserId());
        habit.setDeleted(true);
        habitRepository.save(habit);
    }

    @Transactional
    public void log(Long habitId, HabitLogDto.LogRequest request){
        Habit habit = findOwnedHabit(habitId, userService.getCurrentUserId());

        if(habit.getHabitType() == HabitType.QUANTITATIVE && request.value() == null){
            throw new BusinessRuleException("Value is required for quantitative habits");
        }

        HabitLog log = logRepository.findByHabitIdAndLogDate(habitId, request.logDate())
                    .orElse(new HabitLog());

        log.setHabitId(habitId);
        log.setLogDate(request.logDate());
        log.setCompleted(resolveCompletion(habit, request));
        log.setValue(request.value());
        log.setNotes(request.notes());
        log.setLoggedAt(Instant.now());
        logRepository.save(log);
    }

    public List<HabitLogDto.Response> getLogsForDate(LocalDate date){
        return logRepository.findByUserIdAndLogDate(userService.getCurrentUserId(), date)
                .stream()
                .map(HabitLogDto.Response::fromEntity)
                .toList();
    }

    public HabitLogDto.WeekSummary getWeekSummary(Long habitId, LocalDate weekStart){
        Habit habit = findOwnedHabit(habitId, userService.getCurrentUserId());
        LocalDate weekEnd = weekStart.plusDays(7);

        List<HabitLog> logs = logRepository.findByHabitIdAndDateRange(habitId, weekStart, weekEnd);

        Map<LocalDate, HabitLog> logsByDate = logs.stream()
            .collect(Collectors.toMap(HabitLog::getLogDate, l -> l));

        List<HabitLogDto.DaySummary> days = weekStart.datesUntil(weekEnd.plusDays(1))
                .map(date -> {
                    boolean scheduled = isScheduledForDate(habit, date);
                    HabitLog log = logsByDate.get(date);
                    return new HabitLogDto.DaySummary(
                        date,
                        scheduled,
                        log != null && log.isCompleted(),
                        log != null ? log.getValue() : null
                    );
                })
                .toList();
        
        return new HabitLogDto.WeekSummary(weekStart, weekEnd, days);
    }

    private StreakResult calculateStreak(Habit habit){
        List<HabitLog> logs = logRepository.findByHabitIdOrderByLogDateDesc(habit.getId());  

        List<LocalDate> completedDates = logs.stream()
            .filter(HabitLog::isCompleted)
            .filter(log -> isScheduledForDate(habit, log.getLogDate()))
            .map(HabitLog::getLogDate)
            .sorted(Comparator.reverseOrder())
            .toList();

        if(completedDates.isEmpty()) return new StreakResult(0, 0);

        int currentStreak = computeCurrentStreak(habit, completedDates);
        int longestStreak = computeLongestStreak(habit, completedDates);

        return new StreakResult(currentStreak, longestStreak);
    }

    private int computeCurrentStreak(Habit habit, List<LocalDate> completedDates){
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        if(isScheduledForDate(habit, checkDate)) {
            checkDate = getPreviousScheduledDate(habit, checkDate);
        }

        if(!completedDates.contains(checkDate)){
            LocalDate previous = getPreviousScheduledDate(habit, checkDate);
            if (previous == null || !completedDates.contains(previous)) return 0;
            checkDate = previous;
        }

        int streak = 0;
        while(checkDate != null && completedDates.contains(checkDate)){
            streak++;
            checkDate = getPreviousScheduledDate(habit, checkDate);
        }
        return streak;
    }

    private int computeLongestStreak(Habit habit, List<LocalDate> completedDates){
        List<LocalDate> ascending = completedDates.stream()
            .sorted()
            .toList();
        
        int longest = 0;
        int current = 0;
        LocalDate previous = null;

        for(LocalDate date : ascending){
            if(previous == null){
                current = 1;
            } else {
                LocalDate expectedNext = getNextScheduledDate(habit, previous);
                current = date.equals(expectedNext) ? current + 1: 1;
            }
            longest = Math.max(longest, current);
            previous = date;
        }

        return longest;
    }

    private boolean isScheduledForDate(Habit habit, LocalDate date) {
        if (date.isBefore(habit.getStartDate())) return false;
        if (habit.getEndDate() != null && date.isAfter(habit.getEndDate())) return false;

        return switch (habit.getFrequencyType()) {
            case DAILY -> true;
            case MON_TO_FRI -> {
                DayOfWeek day = date.getDayOfWeek();
                yield day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            }
            case WEEKLY, CUSTOM_DAYS -> {
                if (habit.getCustomDaysMask() == null) yield false;
                yield (habit.getCustomDaysMask() & getDayBit(date.getDayOfWeek())) != 0;
            }
        };
    }

    private LocalDate getPreviousScheduledDate(Habit habit, LocalDate from){
        LocalDate candidate = from.minusDays(1);
        for(int i=0; i<7; i++){
            if(isScheduledForDate(habit, candidate)) return candidate;
            candidate = candidate.minusDays(1);
        }
        return null;
    }

    private LocalDate getNextScheduledDate(Habit habit, LocalDate from){
        LocalDate candidate = from.plusDays(1);
        for(int i = 0; i < 7; i++){
            if(isScheduledForDate(habit, candidate)) return candidate;
            candidate = candidate.plusDays(1);
        }
        return null;
    }

    private int getDayBit(DayOfWeek day) {
        return switch (day) {
            case MONDAY    -> 1;
            case TUESDAY   -> 2;
            case WEDNESDAY -> 4;
            case THURSDAY  -> 8;
            case FRIDAY    -> 16;
            case SATURDAY  -> 32;
            case SUNDAY    -> 64;
        };
    }

    private boolean resolveCompletion(Habit habit, HabitLogDto.LogRequest request) {
        return switch (habit.getHabitType()) {
            case BOOLEAN -> request.completed();
            case QUANTITATIVE -> {
                if (request.value() != null && habit.getTargetValue() != null) {
                    yield request.value() >= habit.getTargetValue();
                }
                yield request.completed();
            }
        };
    }

    private void validate(HabitDto.CreateRequest request){
        if(request.habitType() == HabitType.QUANTITATIVE && request.targetValue() == null){
            throw new BusinessRuleException("Target Value is required for quantitative habits");
        }
        if((request.frequencyType() == FrequencyType.WEEKLY || 
                request.frequencyType() == FrequencyType.CUSTOM_DAYS && request.customDayMask() == null)
        ){
            throw new BusinessRuleException("customDaysMask is required for the weekly habits");
        }
    }

    private Habit findOwnedHabit(Long id, Long userId){
        return habitRepository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(
            () -> new ResourceNotFoundException("Habit not found")
        );
    }

    private record StreakResult(int current, int longest) {}
}
