package klu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import klu.model.User;
import klu.repository.RoleRepository;
import klu.repository.UserRepository;
import klu.util.EmailManager;
import klu.util.JwtUtil;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailManager emailManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "401::User E-mail already exists";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "200::User Registered Successfully!";
    }

    public String signIn(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "404::Invalid Credentials";
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "404::Invalid Credentials";
        }
        return "200::" + jwtUtil.generateToken(email);
    }

    public String getPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String message = "Dear " + user.getName() + "\n\nYour password is " + user.getPassword();
            return emailManager.sendEmail(email, "Password Recovery", message);
        }
        return "404::User not found";
    }
}
