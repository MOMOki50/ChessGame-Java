package SEP.SEP.ChessClub;


import SEP.SEP.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Transactional
public class ChessClubService {

    @Autowired
    private ChessClubRepository chessClubRepository;

    public List<ChessClub> getAllChessClubs() {
        return chessClubRepository.findAll();
    }

    public ChessClub createChessClub(String clubName) {
        ChessClub newClub = new ChessClub();
        newClub.setClubName(clubName);
        return chessClubRepository.save(newClub);
    }

    public void addMemberToClub(User user, ChessClub club) {
        // Überprüfen, ob der Benutzer bereits Mitglied im Club ist
        if (!club.getMembers().contains(user)) {
            // Überprüfen, ob der Benutzer bereits Mitglied in einem anderen Club ist
            ChessClub existingClub = getUserClub(user);
            if (existingClub == null) {
                club.getMembers().add(user);
                chessClubRepository.save(club);
            } else {
                throw new IllegalArgumentException("Der Benutzer ist bereits Mitglied in einem anderen Club.");
            }
        }
    }

    public void removeMemberFromClub(User user, ChessClub club) {
        // Überprüfen, ob der Benutzer Mitglied im Club ist
        if (club.getMembers().contains(user)) {
            System.out.println("Removing member from club: " + club.getClubName());
            club.removeMember(user);
            chessClubRepository.save(club);
            System.out.println("Member removed successfully.");
        } else {
            throw new IllegalArgumentException("Der Benutzer ist kein Mitglied dieses Clubs.");
        }
    }

    public ChessClub getUserClub(User user) {
        return chessClubRepository.findByMembersContains(user);
    }

    public ChessClub getChessClubById(long clubId) {
        return chessClubRepository.findById(clubId).orElse(null);
    }

}
