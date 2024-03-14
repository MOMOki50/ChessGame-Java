package SEP.SEP.ChessStream;

import SEP.SEP.game.Game;
import SEP.SEP.game.GameRepository;
import SEP.SEP.user.User;
import SEP.SEP.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChessStreamController {
    @Autowired
    public ChessStreamService chessStreamService;
    @Autowired
    public GameRepository gameRepository;
    @Autowired
    public UserService userService;

    @GetMapping(value = "/streaming")
    public String getStreamList(Model model, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        List<Game> allGames = gameRepository.findByPartieGestartetIsTrue();

        model.addAttribute("allGames",allGames);
        model.addAttribute("currentUser", userService.getUserData(loggedInEmail));

        if(loggedInEmail != null){
            return "streaming";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping(value = "/liveStreamingGame", produces = "application/json")
    public String liveStreamingGame(@RequestParam(name = "id", required = true, defaultValue = "1") long id, Model model, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        User userData = userService.getUserData(loggedInEmail);
        long spielid = id;
        if(loggedInEmail==null) {
            return "redirect:/login";
        };

        model.addAttribute("currentUser", userData);
        model.addAttribute("spielid", spielid);

        return "liveStreamingGame";
    }
}