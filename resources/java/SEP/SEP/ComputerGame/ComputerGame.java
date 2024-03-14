

package SEP.SEP.ComputerGame;

import SEP.SEP.FinishedGame.FinishedGame;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "computerGame")
public class ComputerGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spielid")
    private long spielid;


    @Column(name = "spieler") //E-Mail
    private String spieler;

    @Column(name = "schwierigkeit") //z.B. 1,2,3
    private int schwierigkeit;

    @Column(name = "clickedCol")
    private int clickedCol = -1;

    @Column(name = "clickedRow")
    private int clickedRow = -1;

    @Column(name = "stellung")
    private String stellung = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Column(name = "promotionCol")
    private int promotionCol = -1;

    @Column(name = "promotionRow")
    private int promotionRow = -1;

    @Column(name = "timer") //in Milliekunden
    private long timer;

    @Column(name = "zeitpunkt")
    private Date zeitpunkt = new Date();


    //@Column(name="moves")//Liste an moves speichern
    //private List<String> moves=new ArrayList<>();


    public long getSpielid() {
        return spielid;
    }

    public void setSpielid(long spielid) {
        this.spielid = spielid;
    }

    public String getSpieler() {
        return spieler;
    }

    public void setSpieler(String spieler) {
        this.spieler = spieler;
    }

    public int getSchwierigkeit() {
        return schwierigkeit;
    }

    public void setSchwierigkeit(int schwierigkeit) {
        this.schwierigkeit = schwierigkeit;
    }

    public int getClickedCol() {
        return clickedCol;
    }

    public void setClickedCol(int clickedCol) {
        this.clickedCol = clickedCol;
    }

    public int getClickedRow() {
        return clickedRow;
    }

    public void setClickedRow(int clickedRow) {
        this.clickedRow = clickedRow;
    }

    public String getStellung() {
        return stellung;
    }

    public void setStellung(String stellung) {
        this.stellung = stellung;
    }

    public int getPromotionCol() {
        return promotionCol;
    }

    public void setPromotionCol(int promotionCol) {
        this.promotionCol = promotionCol;
    }

    public int getPromotionRow() {
        return promotionRow;
    }

    public void setPromotionRow(int promotionRow) {
        this.promotionRow = promotionRow;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public Date getZeitpunkt() {
        return zeitpunkt;
    }

    public void setZeitpunkt(Date zeitpunkt) {
        this.zeitpunkt = zeitpunkt;
    }
}
