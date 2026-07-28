package com.jibruski.productivity_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jibruski.productivity_api.dto.JournalDto;
import com.jibruski.productivity_api.service.JournalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/journals")
public class JournalController {
    private final JournalService journalService;

    @GetMapping
    public ResponseEntity<Page<JournalDto.Response>> getAll(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(journalService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalDto.Response> getById(@PathVariable Long id){
        return ResponseEntity.ok(journalService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody JournalDto.Request request){
        return ResponseEntity.status(HttpStatus.CREATED).body(journalService.createJournal(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody JournalDto.Request request){
        return ResponseEntity.ok(journalService.updateJournal(id, request));
    }

    @PutMapping("/{id}/delete")
    public ResponseEntity<?> delete(@PathVariable Long id){
        journalService.deleteJournal(id);
        return ResponseEntity.noContent().build();
    }
}
