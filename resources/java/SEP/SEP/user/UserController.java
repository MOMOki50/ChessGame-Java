package SEP.SEP.user;

import SEP.SEP.ChessClub.ChessClub;
import SEP.SEP.ChessClub.ChessClubService;
import SEP.SEP.Email.VerificationService;
import SEP.SEP.FinishedGame.FinishedGame;
import SEP.SEP.FinishedGame.FinishedGameRepository;
import SEP.SEP.FinishedGame.FinishedGameService;
import SEP.SEP.friends.FriendService;
import jakarta.servlet.http.HttpSession;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private ChessClubService ChessClubService;
    @Autowired
    private FinishedGameRepository finishedGameRepository;

    @GetMapping(value = "/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping(value = "/")
    public String getStart() {
        return "login";
    }

    @GetMapping(value = "/registrierung")
    public String getRegistration() {
        return "Registrierung";
    }

    @GetMapping(value = "/verify")
    public String getVerify() {
        return "2fa";
    }


    @GetMapping(value = "/home")
    public String getStart(Model model, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        model.addAttribute("currentUser", userRepository.findByEmail(loggedInEmail)); //Variablennamen geändert, um Compliance mit navbar.html herzustellen
        return "startpage";
    }

    //Login findet statt
    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session) {
        if (userService.login(email, password)) {
            session.setAttribute("loggedInEmail", email);
            if (verificationService.verification(email)) {
                return "redirect:/verify";
            } else {
                return "redirect:/login";
            }
        } else {
            return "redirect:/login";
        }
    }

    //Profilansicht
    @GetMapping(value = "/profile")
    public String getUser(Model model, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if (loggedInEmail != null) {
            User userData = userService.getUserData(loggedInEmail);
            List<User> allUsers = userRepository.findAll();

            allUsers.remove(userData);

            System.out.println("Angemeldeter Benutzer: " + userData);
            System.out.println("Alle Nutzer: " + allUsers);

            model.addAttribute("currentUser", userData);
            model.addAttribute("allUser", allUsers);
        } else {
            // Redirect zu einer Login-Seite oder einer Fehlerseite
            return "redirect:/login";
        }
        return "profile";
    }

    //Verifizierung des Codes
    @PostMapping(value = "/verifyCode", consumes = "application/x-www-form-urlencoded")
    public String verifyCode(@RequestParam("code") String code, HttpSession session) {

        String email = (String) session.getAttribute("loggedInEmail");

        if (verificationService.verify(email, code)) {
            return "redirect:/home";
        } else {
            return "redirect:/verify";
        }
    }

    //Registrierung
    @PostMapping(value = "/registration", consumes = "multipart/form-data")
    public String registration(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("birthdate") String birthDate,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePic) {
        if (userService.registration(firstname, lastname, birthDate, email, password, profilePic)) {
            return "redirect:/login";
        } else {
            return "redirect:/registrierung";
        }
    }

    //Profilbild fürs frontend über API
    @GetMapping("picture/{id}")
    public ResponseEntity<byte[]> getUserProfilePicture(@PathVariable(value = "id") long id) {
        byte[] profilePicture = userRepository.findById(id).get().getProfilePicture();
        if (profilePicture != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Setze den Content-Type auf das Bildformat
            return new ResponseEntity<>(profilePicture, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Falls kein Bild gefunden wird
        }
    }

    // URL-Endpunkt für die Seite "Spiel-starten"
    @GetMapping(value = "/spiel-starten")
    public String getSpielStarten(Model model) {
        System.out.println("Join Game link: " + generateInvatationLink(generateGameID()));
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUser", allUsers);
        return "spiel-starten";
    }

    // Notifications für Freundschaftsanfragen und Spielanfragen
    @GetMapping(value = "/notifications")
    public String getNotifications(Model model, @NotNull HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if (loggedInEmail != null) {
            User userData = userService.getUserData(loggedInEmail);
            List<User> allUsers = userRepository.findAll();

            allUsers.remove(userData);

            model.addAttribute("currentUser", userData);
            model.addAttribute("allUser", allUsers);
            model.addAttribute("notifications", userData.getNotifications());
        } else {
            // Redirect zu einer Login-Seite oder einer Fehlerseite
            return "redirect:/login";
        }
        return "notifications";
    }

    @GetMapping(value = "/clubs")
    public String getClubs(Model model, @NotNull HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if (loggedInEmail != null) {
            User userData = userService.getUserData(loggedInEmail);

            // Hier rufe die Methode getChessClubs auf, um die Liste der Schachclubs für den Benutzer zu erhalten
            List<ChessClub> userChessClubs = userData.getChessClubs();

            // Liste aller Schachclubs für die allgemeine Anzeige
            List<ChessClub> allChessClubs = ChessClubService.getAllChessClubs();

            // Füge zusätzliche Informationen zu den Clubs hinzu
            for (ChessClub club : userChessClubs) {
                int memberCount = club.getMembers().size();
                model.addAttribute("clubMemberCount_" + club.getId(), memberCount);
            }

            model.addAttribute("currentUser", userData);
            model.addAttribute("userChessClubs", userChessClubs);
            model.addAttribute("allChessClubs", allChessClubs);  // Hinzugefügt

            return "schachclub";
        } else {
            // Redirect zu einer Login-Seite oder einer Fehlerseite
            return "redirect:/login";
        }
    }

    @GetMapping(value = "/friends_profile/{visitedUserId}") //grob von profile übernommen
    public String getVisitedFriends(Model model, HttpSession session, @PathVariable Long visitedUserId) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        String visitedEmail = userRepository.findById(visitedUserId).get().getEmail();
        System.out.println("Besuchter Benutzer: " + visitedUserId);
        if (loggedInEmail != null) {
            User userData = userService.getUserData(loggedInEmail);
            User visitedData = userService.getUserData(visitedEmail);
            System.out.println("Angemeldeter Benutzer: " + userData);
            System.out.println("Besuchter Benutzer: " + visitedData);
            model.addAttribute("currentUser", userData);
            model.addAttribute("visitedUser", visitedData);
        } else {
            return "redirect:/profile";
        }
        return "friends_profile";
    }

    public String generateInvatationLink(String gameID) {
        return "/join-game?gameID=" + gameID;
    }

    public String generateGameID() {
        return UUID.randomUUID().toString();
    }

    @GetMapping(value = "/visible_friends") //API Endpunkt für boolean ob Freundesliste privat sein soll
    public @ResponseBody boolean getPrivateFriends(@RequestParam("currentUserId") Long currentUserId) {
        return userRepository.getVisibleFriendsById(currentUserId); //ruft anhand her aktuellen UserID in der UserRepository ab, ob die Freundesliste privat ist
    }


    @PostMapping(value = "/set_visible_friends") //API Endpunkt um Freundesliste (nicht) privat zu setzen
    public String updateFriendListVisibility(
            @RequestParam boolean visible_friends, //fragt nach Parameter aus Formular in profile.html
            @RequestParam Long currentUserId) { //fragt nach Parameter aus Formular in profile.html
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found")); //findet den richtigen User über die Repository
        user.setVisibleFriends(visible_friends); //überschreibt den Wert für visible_friends
        userRepository.save(user); //speichert den User
        return "redirect:/profile"; //lädt Seite mit neuem Wert neu
    }

    @PostMapping(value = "/remove_friend") //API Endpunkt für Freunde löschen
    public String removeFriend(@RequestParam("currentUserId") Long
                                       currentUserId, @RequestParam("friendUserId") Long friendUserId) {
        friendService.removeFriend(currentUserId, friendUserId);
        return "redirect:/profile";
    }

    @GetMapping(value = "/leaderboard")

    public String getLeaderboard(Model model, @NotNull HttpSession session) {

        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        User userData = userService.getUserData(loggedInEmail);
        List<User> allUsers = userRepository.findAll();
        allUsers.sort(Comparator.comparing(User::getElo).reversed().thenComparing(User::getId)); //sortiert allUsers nach Id ... die ist ja zum Glück nicht überall gleich xD

        model.addAttribute("currentUser", userData);
        model.addAttribute("allUser", allUsers);

        return "leaderboard";

    }
}