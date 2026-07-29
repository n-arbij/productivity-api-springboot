package com.jibruski.productivity_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.productivity_api.model.Habit;

public interface HabitRepository extends JpaRepository<Habit, Long>{

    List<Habit> findByUserIdAndDeletedFalse(Long userId);

    Optional<Habit> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
}
