package SEP.SEP.game;

import SEP.SEP.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import javax.servlet.http.HttpSession;

@Service
public class GameService {


    @Autowired
    private GameRepository gameRepository;

    public void saveGame(Game game) {
        gameRepository.save(game);
    }


    //Abfrage, hat der spieler eine gestartete Partie
    public boolean partieGestartet(String playerName) {

        //List<Game> games = gameRepository.findBySpielerWeißAndPartieGestartetIsTrueOrSpielerSchwarzAndPartieGestartetIsTrue(playerName, playerName);
        List<Game> gameAsWhite = gameRepository.findBySpielerWeißAndPartieGestartetIsTrue(playerName);
        List<Game> gameAsBlack = gameRepository.findBySpielerSchwarzAndPartieGestartetIsTrue(playerName);
        if (!gameAsWhite.isEmpty())
            return true;
        return !gameAsBlack.isEmpty();
    }

    public Game getGameData(String playerName){
        List<Game> games = gameRepository.findBySpielerWeißAndPartieGestartetIsTrueOrSpielerSchwarzAndPartieGestartetIsTrue(playerName, playerName);
        if(games.isEmpty()){
            return null;
        }
        return games.get(games.size() - 1);
    }

    public boolean spielBeitreten(String playerName){
        return !gameRepository.findBySpielerWeißAndPartieGestartetIsTrueAndPartieBeigetretenIsFalse(playerName).isEmpty();
    }

    public void parteStartenTrue(String playerName){
        Game game=gameRepository.findBySpielerWeißAndPartieGestartetIsTrueAndPartieBeigetretenIsFalse(playerName).get(0);
        game.setPartieBeigetreten(true);
        gameRepository.save(game);
    }

    public void partieGestartetSetTrue(String spielerWeiß, String spielerSchwarz){
        Game game=gameRepository.findBySpielerWeißAndSpielerSchwarz(spielerWeiß, spielerSchwarz).get(0);
        game.setPartieGestartet(true);
        gameRepository.save(game);
    }
}

