package com.jibruski.productivity_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jibruski.productivity_api.model.Settings;

public interface SettingsRepository extends JpaRepository<Settings, Long>{
    Optional<Settings> findByUserId(Long userId);
}
