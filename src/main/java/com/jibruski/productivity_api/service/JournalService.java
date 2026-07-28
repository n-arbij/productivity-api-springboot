package com.jibruski.productivity_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jibruski.productivity_api.dto.JournalDto;
import com.jibruski.productivity_api.model.Journal;
import com.jibruski.productivity_api.repository.JournalRepository;
import com.jibruski.productivity_api.security.CurrentUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JournalService {
    private final CurrentUserService userService;
    private final JournalRepository journalRepository;

    public Page<JournalDto.Response> getAll(Pageable pageable){
        Long userId = userService.getCurrentUserId();
        return journalRepository.findAllByUserIdAndDeletedFalse(userId, pageable)
                    .map(JournalDto.Response::fromEntity);
    }

    public JournalDto.Response getById(Long id){
        Long userId = userService.getCurrentUserId();
        Journal entry = journalRepository.findByIdAndUserId(id, userId).orElseThrow(
            () -> new RuntimeException("Entry not found")
        );
        return JournalDto.Response.fromEntity(entry);
    }

    @Transactional
    public JournalDto.Response createJournal(JournalDto.Request request){
        Long userId = userService.getCurrentUserId();
        Journal entry = new Journal();
        entry.setContent(request.content());
        entry.setUserId(userId);
        journalRepository.save(entry);

        return JournalDto.Response.fromEntity(entry);
    }

    @Transactional
    public JournalDto.Response updateJournal(Long id, JournalDto.Request request){
        Long userId = userService.getCurrentUserId();
        Journal entry = journalRepository.findByIdAndUserId(id, userId).orElseThrow(
            () -> new RuntimeException("Entry not found")
        );

        if(request.content() != null) entry.setContent(request.content());

        return JournalDto.Response.fromEntity(entry);
    }

    @Transactional
    public JournalDto.Response deleteJournal(Long id){
        Long userId = userService.getCurrentUserId();
        Journal entry = journalRepository.findByIdAndUserId(id, userId).orElseThrow(
            () -> new RuntimeException("Entry not found")
        );
        entry.setDeleted(true);
        journalRepository.save(entry);

        return JournalDto.Response.fromEntity(entry);
    }
}
