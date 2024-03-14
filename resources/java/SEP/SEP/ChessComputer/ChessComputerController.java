package SEP.SEP.ChessComputer;

import SEP.SEP.game.Game;
import SEP.SEP.game.GameService;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/chess")
public class ChessComputerController {
    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    private final ChessComputerService chessService;

    private ChessComputerController(ChessComputerService chessService) {
        this.chessService = chessService;
    }


    @PostMapping("/doMyBestMove")
    public String doTheBestMove(@RequestParam String fenPosition, jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        Game game=gameService.getGameData(playerName);
        User user = userRepository.findByEmail((String)session.getAttribute("loggedInEmail"));
        if(user.getElo()>10){
            user.setElo(user.getElo()-1);
            userRepository.save(user);
            return chessService.doTheBestMove(fenPosition, game);
        }
        else {
            return "400";//gibt bad request zurück
        }
    }
}

