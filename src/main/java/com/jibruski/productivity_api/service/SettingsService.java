package com.jibruski.productivity_api.service;

import org.springframework.stereotype.Service;

import com.jibruski.exceptionstarter.exceptions.ResourceNotFoundException;
import com.jibruski.productivity_api.dto.SettingsDto.PomodoroUpdateRequest;
import com.jibruski.productivity_api.dto.SettingsDto.ProfileUpdateRequest;
import com.jibruski.productivity_api.dto.SettingsDto.SettingsResponse;
import com.jibruski.productivity_api.model.Settings;
import com.jibruski.productivity_api.model.User;
import com.jibruski.productivity_api.repository.SettingsRepository;
import com.jibruski.productivity_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    private static final int MIN_MINUTES = 1;
    private static final int MAX_MINUTES = 180;
    private static final int MIN_SESSIONS = 1;
    private static final int MAX_SESSIONS = 10;

    public SettingsResponse getSettings(Long userId) {
        User user = getUser(userId);
        Settings settings = getOrCreateSettings(userId);
        return toResponse(user, settings);
    }
 
    public SettingsResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUser(userId);
 
        if (request.username() != null && !request.username().isBlank()) {
            user.setUsername(request.username().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email().trim());
        }
        userRepository.save(user);
 
        Settings settings = getOrCreateSettings(userId);
        return toResponse(user, settings);
    }
 
    public SettingsResponse updatePomodoro(Long userId, PomodoroUpdateRequest request) {
        validateMinutes(request.pomodoroFocusMinutes(), "pomodoroFocusMinutes");
        validateMinutes(request.pomodoroShortBreakMinutes(), "pomodoroShortBreakMinutes");
        validateMinutes(request.pomodoroLongBreakMinutes(), "pomodoroLongBreakMinutes");
        validateSessions(request.sessionsBeforeLongBreak());
 
        Settings settings = getOrCreateSettings(userId);
        settings.setPomodoroFocusMinutes(request.pomodoroFocusMinutes());
        settings.setPomodoroShortBreakMinutes(request.pomodoroShortBreakMinutes());
        settings.setPomodoroLongBreakMinutes(request.pomodoroLongBreakMinutes());
        settings.setSessionsBeforeLongBreak(request.sessionsBeforeLongBreak());
        settingsRepository.save(settings);
 
        User user = getUser(userId);
        return toResponse(user, settings);
    }
 
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
 
    private Settings getOrCreateSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Settings settings = new Settings();
                    settings.setUserId(userId);
                    return settingsRepository.save(settings);
                });
    }
 
    private void validateMinutes(Integer value, String field) {
        if (value == null || value < MIN_MINUTES || value > MAX_MINUTES) {
            throw new IllegalArgumentException(field + " must be between " + MIN_MINUTES + " and " + MAX_MINUTES);
        }
    }
 
    private void validateSessions(Integer value) {
        if (value == null || value < MIN_SESSIONS || value > MAX_SESSIONS) {
            throw new IllegalArgumentException("sessionsBeforeLongBreak must be between " + MIN_SESSIONS + " and " + MAX_SESSIONS);
        }
    }
 
    private SettingsResponse toResponse(User user, Settings settings) {
        return new SettingsResponse(
                user.getUsername(),
                user.getEmail(),
                settings.getPomodoroFocusMinutes(),
                settings.getPomodoroShortBreakMinutes(),
                settings.getPomodoroLongBreakMinutes(),
                settings.getSessionsBeforeLongBreak()
        );
    }
}
