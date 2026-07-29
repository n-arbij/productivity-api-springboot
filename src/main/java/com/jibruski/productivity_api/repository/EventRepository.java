package com.jibruski.productivity_api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jibruski.productivity_api.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>{

    List<Event> findByUserId(long userId);

    Optional<Event> findByIdAndUserId(long id, long userId);

    @Query("""
        SELECT e FROM Event e
        WHERE e.userId = :userId
        AND e.startDateTime <= :rangeEnd
        AND (e.recurrenceRule IS NOT NULL
            OR e.endDateTime >= :rangeStart)
    """)
    List<Event> findCandidateEvents(
        @Param("userId") long userId,
        @Param("rangeEnd") Instant rangeEnd,
        @Param("rangeStart") Instant rangeStart
    );
}
