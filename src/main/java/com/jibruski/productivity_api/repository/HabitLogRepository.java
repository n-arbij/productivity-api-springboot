package com.jibruski.productivity_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jibruski.productivity_api.model.HabitLog;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long>{
    List<HabitLog> findByHabitIdOrderByLogDateDesc(Long habitId);

    Optional<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate logDate);

    @Query("""
        SELECT hl FROM HabitLog hl
        JOIN Habit h ON h.id = hl.habitId
        WHERE h.userId = :userId
        AND hl.logDate = :date
    """)
    List<HabitLog> findByUserIdAndLogDate(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );

    @Query("""
        SELECT hl FROM HabitLog hl
        WHERE hl.habitId = :habitId
        AND hl.logDate BETWEEN :from AND :to
        ORDER BY hl.logDate ASC
    """)
    List<HabitLog> findByHabitIdAndDateRange(
        @Param("habitId") Long habitId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
