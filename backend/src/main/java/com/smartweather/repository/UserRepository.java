package com.smartweather.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartweather.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
