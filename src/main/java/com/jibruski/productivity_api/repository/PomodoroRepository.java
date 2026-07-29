package com.jibruski.productivity_api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jibruski.productivity_api.model.PomodoroSession;
import com.jibruski.productivity_api.model.SessionStatus;

public interface PomodoroRepository extends JpaRepository<PomodoroSession, Long>{
    Optional<PomodoroSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    @Query("""
        SELECT p FROM PomodoroSession p
        WHERE p.userId = :userId
        AND p.startTime >= :from
        AND p.startTime < :to
    """)
    List<PomodoroSession> findByUserIdAndStartTimeBetween(
        @Param("userId") Long userId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        SELECT p FROM PomodoroSession p
        WHERE p.userId = :userId
        AND p.startTime >= :from
        AND p.startTime < :to
        AND p.sessionType = 'FOCUS'
    """)
    List<PomodoroSession> findFocusSessionsForPeriod(
        @Param("userId") Long userId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}
