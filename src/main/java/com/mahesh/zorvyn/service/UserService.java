package com.mahesh.zorvyn.service;

import com.mahesh.zorvyn.model.User;
import com.mahesh.zorvyn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public User updateUserStatus(String id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }
}
