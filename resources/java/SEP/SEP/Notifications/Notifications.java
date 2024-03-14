package SEP.SEP.Notifications;

import SEP.SEP.user.User;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String type;

    @ManyToOne
    private User requester;

    @ManyToOne
    private User receiever;


    public Notifications(){}

    public Notifications(String type, User requester, User receiever) {
        this.type = type;
        this.requester = requester;
        this.receiever = receiever;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getReceiever() {
        return receiever;
    }

    public void setReceiever(User receiever) {
        this.receiever = receiever;
    }

}



