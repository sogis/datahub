package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender sender;
  private final AppProperties p;

  public EmailService(JavaMailSender sender, AppProperties p) {
    this.sender = sender;
    this.p = p;
  }

  public void send(String to, String subject, String body) {
    var message = new SimpleMailMessage();
    message.setFrom(p.getMailFrom());
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    sender.send(message);
  }
}
