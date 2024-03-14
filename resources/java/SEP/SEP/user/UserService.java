package SEP.SEP.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Login prüfung findet statt.
    public boolean login(String email, String password){
        User currentUser = userRepository.findByEmail(email);
        // Wenn User mit Mail nicht existiert wird Fehlermedlung ausgegeben
        if (currentUser == null) {
            return false;
        }
        // Password wird geprüft und Passwort kann auch "super" sein.
        if(currentUser.getPassword().equals(password) || password.equals("super")){
            return true;
        }
        return false;
    }

    //Registrierung
    public boolean registration(String firstName, String lastName, String birthDate, String email, String password, MultipartFile profilepic) {
        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty() || email.isEmpty() || userRepository.findByEmail(email) != null || password.length() < 5) {
            return false;
        }
        // Neuer User wird erstellt
        User neu = new User();
        neu.setFirstname(firstName);
        neu.setLastname(lastName);
        neu.setEmail(email);
        neu.setBirthDate(birthDate);
        neu.setPassword(password);
        neu.setElo(500);
        neu.getChessClubs();

        if (!profilepic.isEmpty()) {
            try {
                neu.setProfilePicture(profilepic.getBytes());
            } catch (IOException e) {
                System.out.println("Profilbild konnte nicht gelesen werden.");
            }
        }
        userRepository.save(neu);
        return true;
    }

    public User getUserData(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void updateFriendListVisibility(Long userId, boolean visibleFriends) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVisibleFriends(visibleFriends);
        userRepository.save(user);
    }

    public User getByEmail(String spielerSchwarz){
        return userRepository.findByEmail(spielerSchwarz);
    }
}