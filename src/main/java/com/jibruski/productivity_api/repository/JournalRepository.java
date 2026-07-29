package com.jibruski.productivity_api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.productivity_api.model.Journal;

public interface JournalRepository extends JpaRepository<Journal, Long>{
    Page<Journal> findAllByUserIdAndDeletedFalse(Long userId, Pageable pageable);
    Optional<Journal> findByIdAndUserId(Long id, Long userId);
}
