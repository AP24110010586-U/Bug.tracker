package com.bugtracker.service;

import com.bugtracker.dto.UserResponse;
import com.bugtracker.model.Role;
import com.bugtracker.model.User;
import com.bugtracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Reads the list of users (used for the "assign to developer" drop-downs). */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** @param role if null, all users are returned; otherwise only users with that role */
    public List<UserResponse> listUsers(Role role) {
        List<User> users;
        if (role == null) {
            users = userRepository.findAllByOrderByFullNameAsc();
        } else {
            users = userRepository.findByRoleOrderByFullNameAsc(role);
        }

        List<UserResponse> result = new ArrayList<>();
        for (User user : users) {
            result.add(UserResponse.from(user));
        }
        return result;
    }
}
