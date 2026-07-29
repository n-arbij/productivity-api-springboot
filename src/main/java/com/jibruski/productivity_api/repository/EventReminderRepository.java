package com.jibruski.productivity_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.productivity_api.model.Event;
import com.jibruski.productivity_api.model.EventReminder;

public interface EventReminderRepository extends JpaRepository<EventReminder, Long>{
    void deleteByEvent(Event event);

    boolean existsByEventAndRemindBeforeMinutes(Event event, int minutes);

    List<EventReminder> findByEvent(Event event);
}
