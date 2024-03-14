package SEP.SEP.FinishedGame;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Entity
    @Table(name = "FinishedGame")
    public class FinishedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "partiename")
    private String partiename;

    @Column(name = "spielerSchwarz") //E-Mail
    private String spielerSchwarz;

    @Column(name = "spielerWeiß") //E-Mail
    private String spielerWeiß;

    @Column(name = "eloWeiss")
    private int eloWeiss;

    @Column(name = "eloSchwarz")
    private int eloSchwarz;


    @Column(name = "history")//Liste an Stellungen
    private List<String> history = new ArrayList<>(Arrays.asList("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

    @Column(name = "imported")//wurde Partie importiert, oder selbst in dieser Anwendung gespielt
    private boolean imported;

    @Column(name = "date")//wann wurde das Spiel gestartet
    private Date date;

    @Column(name = "result")//1/2-1/2 bei unentschieden, oder 1-0 bzw. 0-1 (je nach dem, welche Farbe gewonnen hat)
    private String result;

    @Column(name= "moves", columnDefinition = "TEXT")
    private String moves="";


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPartiename() {
        return partiename;
    }

    public void setPartiename(String partiename) {
        this.partiename = partiename;
    }

    public String getSpielerSchwarz() {
        return spielerSchwarz;
    }

    public void setSpielerSchwarz(String spielerSchwarz) {
        this.spielerSchwarz = spielerSchwarz;
    }

    public String getSpielerWeiß() {
        return spielerWeiß;
    }

    public void setSpielerWeiß(String spielerWeiß) {
        this.spielerWeiß = spielerWeiß;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }

    public void addToHistory(String stellung){this.history.add(stellung);}

    public String getResult() {
        return result;
    }


    public void setResult(String result) {
        this.result = result;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public void addMove(String move) {
        if(this.moves=="")
            this.moves= move;
        else
            this.moves= this.moves + " " + move;
    }

    public String getSpielerWeiss() {
        return spielerWeiß;
    }

    public int getEloWeiss() {
        return eloWeiss;
    }

    public void setEloWeiss(int eloWeiss) {
        this.eloWeiss = eloWeiss;
    }

    public int getEloSchwarz() {
        return eloSchwarz;
    }

    public void setEloSchwarz(int eloSchwarz) {
        this.eloSchwarz = eloSchwarz;
    }


}

