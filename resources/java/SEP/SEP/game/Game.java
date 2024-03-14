package SEP.SEP.game;

import SEP.SEP.FinishedGame.FinishedGame;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spielid")
    private long spielid;

    @Column(name = "partiename")
    private String partiename;

    @Column(name = "timerWeiß") //in Milliekunden
    private long timerWeiß;

    @Column(name = "timerSchwarz") //in Milliekunden
    private long timerSchwarz;

    @Column(name = "spielerSchwarz") //E-Mail
    private String spielerSchwarz;

    @Column(name = "spielerWeiß") //E-Mail
    private String spielerWeiß;

    //2. Zyklus aktuelle Spiel-Position

    @Column(name = "PartieGestartet")
    private boolean partieGestartet = false;

    @Column(name = "partieBeigetreten")//true, wenn schwarz einladung angenommen hat und auch weiß beigetreten ist
    private boolean partieBeigetreten = false;

    @Column(name = "clickedCol")
    private int clickedCol = -1;

    @Column(name = "clickedRow")
    private int clickedRow = -1;

    @Column(name = "weißAmZug")
    private boolean weißAmZug = true;

    @Column(name = "stellung")
    private String stellung = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Column(name = "zeitpunkt")
    private Date zeitpunkt = null;

    @Column(name = "promotionCol")
    private int promotionCol = -1;

    @Column(name = "promotionRow")
    private int promotionRow = -1;

    @Column(name = "history")
    private List<String> history = new ArrayList<>(Arrays.asList("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

    //@Column(name = "finishedGame")
    @JsonIgnore
    @OneToOne
    private FinishedGame finishedGame;

    @Column(name="moves")//Liste an moves speichern
    private List<String> moves=new ArrayList<>();

    @Column(name="zugCount")
    private int zugCount=1;

    @Column(name="col1")
    private int col1 = -1;

    @Column(name="col2")
    private int col2 = -1;

    @Column(name="row1")
    private int row1 = -1;

    @Column(name="row2")
    private int row2 = -1;





    //@Column(name = "board")
    //private String board=new Board();

    public FinishedGame getFinishedGame() {
        return finishedGame;
    }

    public void setFinishedGame(FinishedGame finishedGame) {
        this.finishedGame = finishedGame;
    }

    public long getSpielid() {
        return spielid;
    }

    public void setSpielid(long spielid) {
        this.spielid = spielid;
    }

    public String getPartiename() {
        return partiename;
    }

    public void setPartiename(String partiename) {
        this.partiename = partiename;
    }

    public long getTimerWeiß() {
        return timerWeiß;
    }

    public void setTimerWeiß(long timerWeiß) {
        this.timerWeiß = timerWeiß;
    }

    public long getTimerSchwarz() {
        return timerSchwarz;
    }

    public void setTimerSchwarz(long timerSchwarz) {
        this.timerSchwarz = timerSchwarz;
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

    public boolean isPartieGestartet() {
        return partieGestartet;
    }

    public void setPartieGestartet(boolean partieGestartet) {
        this.partieGestartet = partieGestartet;
    }

    public boolean isPartieBeigetreten() {
        return partieBeigetreten;
    }

    public void setPartieBeigetreten(boolean partieBeigetreten) {
        this.partieBeigetreten = partieBeigetreten;
    }

    public void setClickedCol(int clickedCol) {this.clickedCol=clickedCol;}

    public int getClickedCol() {return clickedCol;}

    public void setClickedRow(int clickedRow) {this.clickedRow=clickedRow;}

    public int getClickedRow() {return clickedRow;}

    public void setWeißAmZug(boolean weißAmZug){this.weißAmZug=weißAmZug;}

    public boolean getWeißAmZug() {return weißAmZug;}

    public String getStellung() {
        return stellung;
    }

    public void setStellung(String stellung) {
        this.stellung = stellung;
    }

    //public Board getBoard(){return new ObjectMapper().readValue(board, Board.class);}

    //public void setBoard(Board board){this.board=board;}

    public Date getZeitpunkt(){
        return zeitpunkt;
    }

    public void setZeitpunkt(Date zeitpunkt){
        this.zeitpunkt=zeitpunkt;
    }

    public void setPromotionCol(int promotionCol) {this.promotionCol=promotionCol;}

    public int getPromotionCol() {return promotionCol;}

    public void setPromotionRow(int promotionRow) {this.promotionRow=promotionRow;}

    public int getPromotionRow() {return promotionRow;}

    public List<String> getHistory() {return history;}

    public void setHistory(List<String> history) {this.history = history;}

    public void addToHistory(String stellung){this.history.add(stellung);}

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public void addMove(String move){
        this.moves.add(move);
    }

    public int getZugCount() {
        return zugCount;
    }

    public void setZugCount(int zugCount) {
        this.zugCount = zugCount;
    }

    public void zugCountErhöhen() {
        this.zugCount++;
    }

    public void setCol1(int col1){this.col1 = col1;}

    public void setCol2(int col2){this.col2 = col2;}

    public void setRow1(int row1){this.row1 = row1;}

    public void setRow2(int row2){this.row2 = row2;}

    public int getCol1(){return col1;}

    public int getCol2(){return col2;}

    public int getRow1(){return row1;}

    public int getRow2(){return row2;}
}