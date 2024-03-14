package SEP.SEP.ChessStream;

import SEP.SEP.game.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChessStreamService {
    @Autowired
    public GameRepository gameRepository;
}
