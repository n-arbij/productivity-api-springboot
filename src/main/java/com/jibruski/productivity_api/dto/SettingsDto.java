package com.jibruski.productivity_api.dto;

public class SettingsDto {

    public record PomodoroUpdateRequest(
        Integer pomodoroFocusMinutes,
        Integer pomodoroShortBreakMinutes,
        Integer pomodoroLongBreakMinutes,
        Integer sessionsBeforeLongBreak
    ) {}

    public record ProfileUpdateRequest(
        String username,
        String email
    ) {}

    public record SettingsResponse(
        String username,
        String email,
        Integer pomodoroWorkMinutes,
        Integer pomodoroShortBreakMinutes,
        Integer pomodoroLongBreakMinutes,
        Integer sessionsBeforeLongBreak
    ) {}

}
