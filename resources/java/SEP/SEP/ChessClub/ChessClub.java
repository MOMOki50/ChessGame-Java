package SEP.SEP.ChessClub;

import SEP.SEP.Chat.Chat;
import SEP.SEP.user.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "chess_club")
public class ChessClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "club_name", unique = true)
    private String clubName;

    @ManyToMany
    @JoinTable(
            name = "club_members",
            joinColumns = @JoinColumn(name = "club_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>(); // Direkte Initialisierung der Liste

    //Club chat
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private Chat chat; // Chat-Attribut hinzugefügt

    public ChessClub() {
        this.members = new ArrayList<>();
        this.chat = new Chat(); // Initialisierung des Chats im Konstruktor
    }

    // Getter und Setter

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public List<User> getMembers() {
        return members;
    }

    // Überprüfung, ob die members-Liste null ist, und ggf. initialisieren
    public void setMembers(List<User> members) {
        this.members = (members != null) ? members : new ArrayList<>();
    }

    public void addMember(User member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
        member.getChessClubs().add(this);
    }

    public void removeMember(User member) {
        if (members != null) {
            members.remove(member);
            member.getChessClubs().remove(this);
        }
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    }
