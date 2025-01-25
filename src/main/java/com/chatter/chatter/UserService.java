package com.chatter.chatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public static void saveUser(User user) {
        userRepository.save(user);
    }
}