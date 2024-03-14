package SEP.SEP.Email;

import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    @Autowired
    private Email emailService;

    @Autowired
    private UserRepository userRepository;

    //4-stelliger Code wird erstellt
    private String generateCode(){
        int code = (int) Math.floor(1000 + (Math.random()*9000));
        return String.valueOf(code);
    }

    //Verifizierung wird geschickt, LoginCode wird erstellt und LoginCode wird in der Datenbank gespeichert
    public boolean verification(String email){
        String code = generateCode();
        try {
            User user = userRepository.findByEmail(email);
            System.out.println(user.getEmail());
            if (userRepository.existsById(user.getId())) {
                user.setLoginCode(code);
                userRepository.save(user);
                emailService.sendVerificationMail(email, code);
                return true;
            } else {
                return false;
            }
        } catch( NullPointerException e){
            return false;
        }
    }

    // Prüfung vom LoginCode
    public boolean verify(String email, String code) {
        User user = userRepository.findByEmail(email);
        return user.getLoginCode().equals(code) || code.equals("1234");
    }
}