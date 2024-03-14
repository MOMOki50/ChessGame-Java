package SEP.SEP.user;

import SEP.SEP.ChessClub.ChessClub;
import SEP.SEP.Chat.Chat;
import SEP.SEP.Chat.ChatMessage.ChatMessage;
import SEP.SEP.Notifications.Notifications;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "player")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "firstname")
    private String firstname;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "birthDate")
    private String birthDate;

    @Column(name = "elo")
    private int elo = 500;
    @Column(name = "password")
    private String password;
    @Column(name = "loginCode")
    private String loginCode;

    @Column(name = "profilePicture")
    private byte[] profilePicture;

    @Column(name = "visible_friends") //boolean ob Freundeliste sichtbar sein soll, default: sichtbar für andere
    private boolean visible_friends = true;

    @Column(name = "anzahlGeloestePuzzle")
    private int anzahlGeloestePuzzle=0;

    @JsonIgnore
    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> messages;

    @JsonIgnore
    @ManyToMany
    private Set<User> friends = new HashSet<User>();

    @JsonIgnore
    @OneToMany
    private List<Notifications> notifications = new ArrayList<Notifications>();

    @JsonIgnore
    @ManyToMany(mappedBy = "participants")
    private List<Chat> chats = new ArrayList<Chat>();

    @ManyToMany(mappedBy = "members")
    private List<ChessClub> chessClubs;



    public User(String firstName, String lastName, String birthDate, String email, String password, byte[] profilePicture, List<ChessClub> chessClubs) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.password = password;
        this.profilePicture = profilePicture;
        this.chessClubs = chessClubs;
    }

    public User() {

    }


    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean getVisible_friends() {
        return visible_friends;
    }

    public void setVisibleFriends(boolean visible_friends) {
        this.visible_friends = visible_friends;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public List<Notifications> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notifications> notifications) {
        this.notifications = notifications;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public List<ChessClub> getChessClubs() {return chessClubs;}

    public int getAnzahlGeloestePuzzle() {return anzahlGeloestePuzzle;}

    public void setAnzahlGeloestePuzzle(int anzahlGeloestePuzzle) {this.anzahlGeloestePuzzle = anzahlGeloestePuzzle;}
}

