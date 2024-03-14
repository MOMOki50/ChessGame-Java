package SEP.SEP.ChessComputer;
import SEP.SEP.game.Game;
import SEP.SEP.game.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class ChessComputerService {

    @Autowired
    private GameRepository gameRepository;

    public String doTheBestMove(String fenPosition, Game game) {
        System.out.println("Service Input: fenPosition: " + fenPosition);
        Stockfish stockfish = new Stockfish();
        //System.out.println(stockfish.bestMove(fenPosition, "2000", "25"));
        String bestmove = stockfish.bestMove(fenPosition, "2000", "25");
        //System.out.println("Service Output: bestmove: " + bestmove); //e.g. a6a5
        int col1 = bestmove.charAt(0)-97;//97 ist der ascii code für 'a'
        int row1 = 8 - (bestmove.charAt(1) - 48); //48 ist der ascii code für '0'
        int col2 = bestmove.charAt(2)-97;
        int row2 = 8 - (bestmove.charAt(3) - 48);
        game.setCol1(col1);
        game.setRow1(row1);
        game.setCol2(col2);
        game.setRow2(row2);
        gameRepository.save(game);
        return bestmove;
    }
}
