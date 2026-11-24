package com.bugtracker.repository;

import com.bugtracker.model.Role;
import com.bugtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data writes the SQL for us from the method names.
 * Example: findByUsername  ->  SELECT * FROM users WHERE username = ?
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findAllByOrderByFullNameAsc();

    List<User> findByRoleOrderByFullNameAsc(Role role);
}
