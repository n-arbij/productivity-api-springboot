package com.jibruski.productivity_api.dto;

import java.time.Instant;

import com.jibruski.productivity_api.model.Journal;

import jakarta.validation.constraints.NotBlank;

public class JournalDto {
    public record Request(
        @NotBlank String content
    ) {}

    public record Response (
        Long id,
        String content,
        Instant createdAt,
        Instant updatedAt
    ){
        public static Response fromEntity(Journal journal){
            return new Response(
                journal.getId(),
                journal.getContent(),
                journal.getCreatedAt(),
                journal.getUpdatedAt()
            );
        }
    }
}
