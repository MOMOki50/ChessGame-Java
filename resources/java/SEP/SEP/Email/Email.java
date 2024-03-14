package SEP.SEP.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Email {

    @Autowired
    private JavaMailSender mailSender;

    //Mail wird erstellt und versendet
    public void sendVerificationMail(String to, String code){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("SEPVgruppeB@outlook.de");
        mail.setTo(to);
        mail.setSubject("Verifzierungs-Code");
        mail.setText("Bitte gebe folgenden Code ein: " + code);
        mailSender.send(mail);
    }

    public void FriendRequestMail(String to, String username){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("SEPVgruppeB@outlook.de");
        mail.setTo(to);
        mail.setSubject("Freundschaftsanfrage");
        mail.setText("Hallo " + username + ",\n\n" +"Du hast eine Freundschaftsanfrage bekommen.\n\n" +
                "Um die Anfrage zu akzeptieren, logge dich in deinem Konto ein und gehe zu den Benachrichtigungen.\n\n" +
                "Mit freundlichen Grüßen,\nIhr SEP-Team");
        mailSender.send(mail);
    }
}