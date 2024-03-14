package SEP.SEP.ChessPuzzle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChessPuzzleRepository extends JpaRepository<ChessPuzzle, Long> {

    List<ChessPuzzle> findBySpielerMail(String spielerMail);

    ChessPuzzle getById(Long id);
}
