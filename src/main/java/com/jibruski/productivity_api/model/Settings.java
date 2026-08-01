package com.jibruski.productivity_api.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_settings")
@AllArgsConstructor
@NoArgsConstructor
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer pomodoroFocusMinutes = 25;
 
    @Column(nullable = false)
    private Integer pomodoroShortBreakMinutes = 5;
 
    @Column(nullable = false)
    private Integer pomodoroLongBreakMinutes = 15;
 
    @Column(nullable = false)
    private Integer sessionsBeforeLongBreak = 4;
 
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
 
    @Column(nullable = false)
    private Instant updatedAt;
 
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
 
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
