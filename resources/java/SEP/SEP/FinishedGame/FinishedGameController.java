package SEP.SEP.FinishedGame;

import SEP.SEP.game.GameController;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import SEP.SEP.Notifications.NotificationsRepository;
import SEP.SEP.game.GameRepository;
import SEP.SEP.game.GameService;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import SEP.SEP.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;


@Controller
//@RequestMapping("/finished-game")
public class FinishedGameController {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private FinishedGameRepository finishedGameRepository;

    @Autowired
    private FinishedGameService finishedGameService;

    @Autowired
    private GameController gameController;



    @GetMapping(value = "/gameRepetition", produces = "application/json")
    public String createGame(@RequestParam(name = "id", required = true, defaultValue = "1") long id, Model model, jakarta.servlet.http.HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        User userData = userService.getUserData(loggedInEmail);

        if(loggedInEmail==null)
            return "redirect:/login";


        model.addAttribute("currentUser", userData);
        model.addAttribute("id", id);

        return "gameRepetition";
    }

    @GetMapping("/{id}/getFinishedGameData")
    @ResponseBody
    public FinishedGame getFinishedGameData(@PathVariable long id) {
        FinishedGame finishedGame=finishedGameRepository.findById(id);
        return finishedGame;
    }


    @PostMapping("/import-pgn")
    public String importPgnFile(@RequestParam("pgnFile") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String pgnContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            FinishedGame finishedGame = finishedGameService.createFinishedGameFromPgn(pgnContent);
            gameController.fenGenerator(finishedGame);
            finishedGameRepository.save(finishedGame);
            redirectAttributes.addFlashAttribute("successMessage", "Spiel erfolgreich importiert.");
        } catch (IOException | ParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Importieren des Spiels: " + e.getMessage());
        }
        return "redirect:/deine-spiele";
    }


    @GetMapping("/download-pgn/{id}")
    public ResponseEntity<Resource> downloadPgn(@PathVariable Long id) {
        FinishedGame finishedGame = finishedGameRepository.findById(id).orElse(null);

        if (finishedGame == null) {
            return ResponseEntity.notFound().build();
        }

        String pgnData = finishedGameService.convertToPgn(finishedGame);
        ByteArrayResource resource = new ByteArrayResource(pgnData.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + finishedGame.getPartiename() + ".pgn")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }


    @GetMapping("/deine-spiele")
    public String deineletztenSpiele(Model model, jakarta.servlet.http.HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if(loggedInEmail == null)
            return "redirect:/login";

        User userData = userService.getUserData(loggedInEmail);

        List<FinishedGame> recentFinishedGames = finishedGameService.getRecentFinishedGames(3, session); //Anzahl der Spiele anpassen

        List<FinishedGame> spiele = finishedGameRepository.findAll();
        for(FinishedGame game:recentFinishedGames)
        eloAktualisieren(game);

        model.addAttribute("spiele", spiele);
        model.addAttribute("currentUser", userData);
        model.addAttribute("deineletztenspiele", recentFinishedGames);

        return "gameHistory";
    }

    public void eloAktualisieren(FinishedGame game){
        game.setEloWeiss(userRepository.getByEmail(game.getSpielerWeiss()).getElo());
        game.setEloSchwarz(userRepository.getByEmail(game.getSpielerSchwarz()).getElo());
        finishedGameRepository.save(game);
    }
}
