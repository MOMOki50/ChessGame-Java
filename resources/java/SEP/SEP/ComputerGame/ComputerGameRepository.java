package SEP.SEP.ComputerGame;

import SEP.SEP.FinishedGame.FinishedGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComputerGameRepository extends JpaRepository<ComputerGame, Long> {

    List<ComputerGame> findBySpieler(String spieler);
}
