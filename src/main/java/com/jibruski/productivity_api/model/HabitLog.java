package com.jibruski.productivity_api.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "habit_log",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_habit_log_date",
        columnNames = {"habit_id", "log_date"}
    )
)
public class HabitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long habitId;

    @Column(nullable = false, name = "log_date")
    private LocalDate logDate;

    @Column(nullable = false)
    private boolean completed;

    private Double value;

    private String notes;

    @Column(nullable = false)
    private Instant loggedAt = Instant.now();
}
