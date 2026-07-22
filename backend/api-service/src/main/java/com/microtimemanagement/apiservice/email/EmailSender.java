package com.microtimemanagement.apiservice.email;

/**
 * Outbound email abstraction. The default implementation ({@link LoggingEmailSender})
 * is a STUB that only logs — no email actually leaves the app.
 *
 * <p>To send real email (before or after deploy), see the setup notes in the
 * README ("Email reminders"): add {@code spring-boot-starter-mail}, configure
 * {@code spring.mail.*} from env, and provide a {@code JavaMailSender}-backed
 * implementation of this interface (it will be picked up in place of the stub).
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
