package com.microtimemanagement.apiservice.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * STUB {@link EmailSender}: logs the message instead of sending it — no email
 * actually leaves the app.
 *
 * <p>Enabling real email later (see README "Email reminders"):
 * <pre>
 *   1. add dependency: org.springframework.boot:spring-boot-starter-mail
 *   2. set env: SPRING_MAIL_HOST / SPRING_MAIL_PORT / SPRING_MAIL_USERNAME /
 *      SPRING_MAIL_PASSWORD (+ spring.mail.properties.mail.smtp.* as needed)
 *   3. add an @Primary @Component implementing EmailSender backed by
 *      JavaMailSender — it overrides this stub with no other code changes.
 * </pre>
 */
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL STUB — not actually sent] to='{}' subject='{}' body='{}'", to, subject, body);
    }
}
