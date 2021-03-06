package ru.itis.javalab.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itis.javalab.dto.UserForm;
import ru.itis.javalab.models.Email;
import ru.itis.javalab.models.User;
import ru.itis.javalab.repositories.UserRepository;
import ru.itis.javalab.utils.EmailUtil;
import ru.itis.javalab.utils.MailsGenerator;

import java.util.UUID;

@Service
public class SignUpServiceImpl implements SignUpService {

    private final UserRepository userRepository;
    private final MailsGenerator mailsGenerator;
    private final EmailUtil emailUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignUpServiceImpl(UserRepository userRepository,
                             EmailUtil emailUtil,
                             MailsGenerator mailsGenerator,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailUtil = emailUtil;
        this.mailsGenerator = mailsGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${server.url}")
    private String serverUrl;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean signUp(UserForm form) {
        String hash = passwordEncoder.encode(form.getPassword());
        User newUser = User.builder()
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .email(form.getEmail())
                .hashPassword(hash)
                .confirmCode(UUID.randomUUID())
                .role(User.Role.USER)
                .state(User.State.ACTIVE)
                .build();
        userRepository.save(newUser);
        String confirmMail = mailsGenerator.getMailForConfirm(serverUrl, newUser.getConfirmCode().toString());
        Email email = Email.builder()
                .from(from)
                .to(newUser.getEmail())
                .subject("Регистрация")
                .text(confirmMail)
                .build();
        emailUtil.sendMail(email);
        return true; //TODO
    }
}

