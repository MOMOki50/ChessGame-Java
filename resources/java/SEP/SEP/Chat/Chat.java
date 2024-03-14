package SEP.SEP.Chat;

import SEP.SEP.Chat.ChatMessage.ChatMessage;
import SEP.SEP.user.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ChatMessage> messages = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "chat_players",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    private List<User> participants = new ArrayList<User>();

    @Column
    private String chessClubName;


    public Chat() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public String getChessClubName() {
        return chessClubName;
    }

    public void setChessClubName(String chessClubName) {
        this.chessClubName = chessClubName;
    }
}