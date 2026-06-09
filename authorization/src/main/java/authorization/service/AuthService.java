package authorization.service;

import authorization.dto.AuthResponse;
import authorization.dto.LoginRequest;
import authorization.dto.RegisterRequest;
import authorization.dto.RegisterResponse;
import authorization.entity.User;
import authorization.exception.ConflictException;
import authorization.exception.UnauthorizedException;
import authorization.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String EMAIL_ALREADY_EXISTS = "Email already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException(EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        return new AuthResponse(jwtService.generateToken(user));
    }
}
