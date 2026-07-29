package com.jibruski.productivity_api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.productivity_api.dto.HabitDto;
import com.jibruski.productivity_api.dto.HabitLogDto;
import com.jibruski.productivity_api.service.HabitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habits")
public class HabitController {
    private final HabitService habitService;

    @GetMapping
    public ResponseEntity<List<HabitDto.Response>> getAll(){
        return ResponseEntity.ok(habitService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitDto.Response> getById(@PathVariable Long id){
        return ResponseEntity.ok(habitService.getById(id));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<HabitLogDto.Response>> log(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        return ResponseEntity.ok(habitService.getLogsForDate(date));
    }

    @GetMapping("/{id}/week-summary")
    public ResponseEntity<HabitLogDto.WeekSummary> getWeekSummary(
        @PathVariable Long id,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        return ResponseEntity.ok(habitService.getWeekSummary(id, date));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody HabitDto.CreateRequest request){
        habitService.createHabit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/logs")
    public ResponseEntity<?> log(
        @PathVariable Long id,
        @Valid @RequestBody HabitLogDto.LogRequest request
    ){
        habitService.log(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitDto.Response> update(
        @PathVariable Long id,
        @Valid @RequestBody HabitDto.UpdateRequest request
    ){
        return ResponseEntity.ok(habitService.updateHabit(id, request));
    }

    @PutMapping("/{id}/remove")
    public ResponseEntity<HabitDto.Response> delete(@PathVariable Long id){
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }
}
