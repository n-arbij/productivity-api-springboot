package com.jibruski.productivity_api.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.productivity_api.dto.PomodoroDto;
import com.jibruski.productivity_api.service.PomodoroService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pomodoro")
public class PomodoroController {
    private final PomodoroService pomodoroService;

    @PostMapping("/start")
    public ResponseEntity<?> start(
        @Valid @RequestBody PomodoroDto.CreateRequest request
    ) {
        pomodoroService.start(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PomodoroDto.Response> update(
        @PathVariable Long id,
        @Valid @RequestBody PomodoroDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(pomodoroService.update(id, request));
    }

    @GetMapping("/active")
    public ResponseEntity<PomodoroDto.Response> getActive() {
        return ResponseEntity.ok(pomodoroService.getActive());
    }

    @GetMapping("/summary")
    public ResponseEntity<PomodoroDto.SummaryResponse> getSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(pomodoroService.getSummary(date));
    }
}
