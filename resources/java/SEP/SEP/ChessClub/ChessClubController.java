package SEP.SEP.ChessClub;


import SEP.SEP.user.User;
import SEP.SEP.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clubs")
public class ChessClubController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChessClubService chessClubService;

    @Autowired
    private ChessClubRepository chessClubRepository;



    @PostMapping("/createClub")
    public String createChessClub(@RequestParam String clubName, HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if (loggedInEmail != null) {
            User currentUser = userService.getUserData(loggedInEmail);

            try {
                ChessClub newClub = chessClubService.createChessClub(clubName);
                return "redirect:/clubs";
            } catch (IllegalArgumentException e) {
                // Behandlung der IllegalArgumentException
                model.addAttribute("error", e.getMessage());
                return "redirect:/clubs"; // Passe dies an deine Fehlerseite an
            }
        } else {
            return "redirect:/login";
        }
    }


    @GetMapping("/club/{clubId}")
    public String getClubPage(@PathVariable long clubId, Model model, HttpSession session) {
        ChessClub club = chessClubService.getChessClubById(clubId);
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");


        if (club != null && loggedInEmail != null) {
            User currentUser = userService.getUserData(loggedInEmail);
            User userData = userService.getUserData(loggedInEmail);
            model.addAttribute("clubMembers", club.getMembers());
            model.addAttribute("currentUser", userData);
            if (club.getMembers().contains(currentUser)) {
                model.addAttribute("club", club);
                return "Clubseite";
            } else {
                // Benutzer ist kein Mitglied des Clubs, daher zur Clubliste umleiten
                return "redirect:/clubs";
            }
        } else {
            // Club nicht gefunden oder Benutzer nicht eingeloggt, zur Clubliste umleiten
            return "redirect:/clubs";
        }
    }

    @GetMapping("/club")
    public String getClubListPage(Model model, HttpSession session) {

        return "Clubseite";
    }

    @PostMapping("/join/{clubId}")
    public String joinClub(@PathVariable long clubId, HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if (loggedInEmail != null) {
            User currentUser = userService.getUserData(loggedInEmail);
            ChessClub clubToJoin = chessClubService.getChessClubById(clubId);

            if (clubToJoin != null) {
                try {
                    chessClubService.addMemberToClub(currentUser, clubToJoin);
                    return "redirect:/clubs";
                } catch (IllegalArgumentException e) {
                    // Behandlung der IllegalArgumentException
                    model.addAttribute("error", e.getMessage());
                    return "redirect:/clubs";
                }
            } else {
                return "redirect:/clubs";
            }
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("/leave/{clubId}")
    public String leaveClub(@PathVariable long clubId, HttpSession session, Model model) {
        System.out.println("Leave Club method called for clubId: " + clubId);

        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if (loggedInEmail != null) {
            User currentUser = userService.getUserData(loggedInEmail);
            ChessClub clubToLeave = chessClubService.getChessClubById(clubId);

            if (clubToLeave != null) {
                try {
                    System.out.println("Removing member from club:" + clubToLeave.getClubName());
                    chessClubService.removeMemberFromClub(currentUser, clubToLeave);
                    System.out.println("Member successfully removed from the club.");
                    return "redirect:/clubs";
                } catch (IllegalArgumentException e) {
                    // Behandlung der IllegalArgumentException
                    System.out.println("Error removing member from the club: " + e.getMessage());
                    model.addAttribute("error", e.getMessage());
                    return "redirect:/clubs";
                }
            } else {
                System.out.println("Club not found.");
                return "redirect:/clubs";
            }
        } else {
            System.out.println("User not logged in.");
            return "redirect:/login";
        }
    }

    @GetMapping("/clubmember/{clubId}")
    public ResponseEntity<List<Long>> getMemberIds(@PathVariable Long clubId){
        return ResponseEntity.ok(chessClubRepository.findMemberIdsByClubId(clubId));
    }
}

