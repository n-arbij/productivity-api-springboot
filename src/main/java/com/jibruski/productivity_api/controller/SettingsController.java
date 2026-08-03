package com.jibruski.productivity_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.productivity_api.dto.SettingsDto.PomodoroUpdateRequest;
import com.jibruski.productivity_api.dto.SettingsDto.ProfileUpdateRequest;
import com.jibruski.productivity_api.dto.SettingsDto.SettingsResponse;
import com.jibruski.productivity_api.service.SettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @GetMapping
    public SettingsResponse getSettings() {
        return settingsService.getSettings();
    }
 
    @PutMapping("/profile")
    public SettingsResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return settingsService.updateProfile(request);
    }
 
    @PutMapping("/pomodoro")
    public SettingsResponse updatePomodoro(@Valid @RequestBody PomodoroUpdateRequest request) {
        return settingsService.updatePomodoro(request);
    }
}
