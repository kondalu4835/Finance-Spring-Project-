package com.finance.service;

import com.finance.dto.UserDto;
import com.finance.entity.User;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public UserDto.PageResponse list(int page, int size) {
        Page<User> pg = userRepository.findAll(
            PageRequest.of(page, size, Sort.by("id").ascending())
        );
        return UserDto.PageResponse.builder()
                .content(pg.getContent().stream().map(this::mapToDto).toList())
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();
    }

    public UserDto.Response getById(Long id) {
        return mapToDto(findOrThrow(id));
    }

    public UserDto.Response create(UserDto.CreateRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }
        User user = userRepository.save(User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build());
        return mapToDto(user);
    }

    @Transactional
    public UserDto.Response update(Long id, UserDto.UpdateRequest req) {
        User user = findOrThrow(id);
        if (req.getName()   != null) user.setName(req.getName());
        if (req.getRole()   != null) user.setRole(req.getRole());
        if (req.getActive() != null) user.setActive(req.getActive());
        return mapToDto(userRepository.save(user));
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserDto.Response mapToDto(User u) {
        return UserDto.Response.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}