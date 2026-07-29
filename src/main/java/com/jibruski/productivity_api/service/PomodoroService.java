package com.jibruski.productivity_api.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jibruski.productivity_api.dto.PomodoroDto;
import com.jibruski.productivity_api.model.PomodoroSession;
import com.jibruski.productivity_api.model.SessionStatus;
import com.jibruski.productivity_api.model.SessionType;
import com.jibruski.productivity_api.repository.PomodoroRepository;
import com.jibruski.productivity_api.security.CurrentUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PomodoroService {
    private final PomodoroRepository pomodoroRepository;
    private final CurrentUserService userService;

    @Transactional
    public void start(PomodoroDto.CreateRequest request){
        Long userId = userService.getCurrentUserId();

        // Cancel any running session before starting a new one
        pomodoroRepository
            .findByUserIdAndStatus(userId, SessionStatus.RUNNING)
            .ifPresent(existing -> {
                existing.setStatus(SessionStatus.CANCELLED);
                existing.setEndTime(Instant.now());
                pomodoroRepository.save(existing);
            });

        PomodoroSession session = new PomodoroSession();
        session.setUserId(userId);
        session.setSessionType(request.sessionType());
        session.setStatus(SessionStatus.RUNNING);
        session.setStartTime(Instant.now());
        session.setPlannedDurationMinutes(request.plannedDurationMinutes());
        pomodoroRepository.save(session);
    }

    @Transactional
    public PomodoroDto.Response update(Long sessionId, PomodoroDto.UpdateRequest request){
        Long userId = userService.getCurrentUserId();

        PomodoroSession session = pomodoroRepository.findById(sessionId).orElseThrow(
            () -> new RuntimeException("Session not found")
        );

        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("You do not own this session");
        }

        session.setStatus(request.status());

        if (request.status() == SessionStatus.COMPLETED) {
            session.setCompleted(true);
            session.setEndTime(Instant.now());
        }

        if (request.status() == SessionStatus.CANCELLED) {
            session.setEndTime(Instant.now());
        }

        return PomodoroDto.Response.fromEntity(pomodoroRepository.save(session));
    }

    public PomodoroDto.Response getActive() {
        Long userId = userService.getCurrentUserId();

        return pomodoroRepository
            .findByUserIdAndStatus(userId, SessionStatus.RUNNING)
            .map(PomodoroDto.Response::fromEntity)
            .orElse(null);
    }

    public PomodoroDto.SummaryResponse getSummary(LocalDate date) {
        Long userId = userService.getCurrentUserId();

        Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to   = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<PomodoroSession> sessions = pomodoroRepository
            .findByUserIdAndStartTimeBetween(userId, from, to);

        int completedSessions = (int) sessions.stream()
            .filter(s -> s.getSessionType() == SessionType.FOCUS && s.isCompleted())
            .count();

        int cancelledSessions = (int) sessions.stream()
            .filter(s -> s.getSessionType() == SessionType.FOCUS &&
                         s.getStatus() == SessionStatus.CANCELLED)
            .count();

        int totalFocusMinutes = sessions.stream()
            .filter(s -> s.getSessionType() == SessionType.FOCUS &&
                         s.isCompleted() &&
                         s.getStartTime() != null &&
                         s.getEndTime() != null)
            .mapToInt(s -> (int) java.time.Duration.between(
                s.getStartTime(), s.getEndTime()).toMinutes())
            .sum();

        int totalBreakMinutes = sessions.stream()
            .filter(s -> (s.getSessionType() == SessionType.SHORT_BREAK ||
                          s.getSessionType() == SessionType.LONG_BREAK) &&
                          s.isCompleted() &&
                          s.getStartTime() != null &&
                          s.getEndTime() != null)
            .mapToInt(s -> (int) java.time.Duration.between(
                s.getStartTime(), s.getEndTime()).toMinutes())
            .sum();

        return new PomodoroDto.SummaryResponse(
            date.toString(),
            completedSessions,
            cancelledSessions,
            totalFocusMinutes,
            totalBreakMinutes
        );
    }
}
