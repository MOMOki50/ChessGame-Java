package SEP.SEP.ChessClub;// ChessClubRepository.java
import SEP.SEP.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChessClubRepository extends JpaRepository<ChessClub, Long> {
    ChessClub findByMembersContains(User user);

    @Query("SELECT u.id FROM ChessClub c JOIN c.members u WHERE c.id = :clubId")
    List<Long> findMemberIdsByClubId(@Param("clubId") Long clubId);

    ChessClub findChessClubByClubName(String clubName);
}