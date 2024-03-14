package SEP.SEP.game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findBySpielerWeißAndPartieGestartetIsTrueOrSpielerSchwarzAndPartieGestartetIsTrue(String spielerWeiß, String spielerSchwarz);

    List<Game> findByPartieGestartetIsTrue();

    List<Game> findBySpielerWeißAndPartieGestartetIsTrue(String playerName);

    List<Game> findBySpielerSchwarzAndPartieGestartetIsTrue(String playerName);

    List<Game> findBySpielerWeißAndPartieGestartetIsTrueAndPartieBeigetretenIsFalse(String playerName);

    List<Game> findBySpielerWeißAndSpielerSchwarz(String spielerWeiß, String spielerSchwarz);

    List<Game> findByPartiename(String partiename);

    Iterable<? extends Game> findBySpielerSchwarzAndPartieGestartetIsFalseAndSpielerWeiß(String schwarz, String weiß);

    Game findById(long id);
}
