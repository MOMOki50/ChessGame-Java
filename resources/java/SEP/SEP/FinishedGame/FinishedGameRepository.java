package SEP.SEP.FinishedGame;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinishedGameRepository extends JpaRepository<FinishedGame, Long> {

    List<FinishedGame> findByPartiename(String name);

    FinishedGame findById(long id);

    List<FinishedGame> findByImportedIsFalseAndSpielerWeißAndResultIsNotNullOrImportedIsFalseAndSpielerSchwarzAndResultIsNotNull(String spielerWeiß, String spielerSchwarz);
}
