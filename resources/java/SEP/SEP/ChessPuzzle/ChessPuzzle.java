package SEP.SEP.ChessPuzzle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ChessPuzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String puzzleId;

    private String spielerMail;

    private String fen;
    private String moves;

    private Integer rating;

    private Integer ratingDeviation;

    private Integer popularity;

    private Integer nbPlays;

    private String themes;

    private String gameUrl;

    private String openingTags;

    private int gegnerStartCol=-1;

    private int gegnerStartRow=-1;

    private int gegnerEndCol=-1;

    private int gegnerEndRow=-1;

    private boolean amZug=false;

    private int clickedCol=-1;

    private int clickedRow=-1;

    public int getClickedRow() {return clickedRow;}

    public void setClickedRow(int clickedRow) {this.clickedRow = clickedRow;}

    public int getClickedCol() {return clickedCol;}

    public void setClickedCol(int clickedCol) {this.clickedCol = clickedCol;}


    public boolean getAmZug() {return amZug;}

    public void setAmZug(boolean amZug) {this.amZug = amZug;}

    public int getGegnerStartCol() {return gegnerStartCol;}

    public int getGegnerStartRow() {return gegnerStartRow;}

    public void setGegnerStartRow(int gegnerStartRow) {this.gegnerStartRow = gegnerStartRow;}

    public int getGegnerEndCol() {return gegnerEndCol;}

    public void setGegnerEndCol(int gegnerEndCol) {this.gegnerEndCol = gegnerEndCol;}

    public int getGegnerEndRow() {return gegnerEndRow;}

    public void setGegnerEndRow(int gegnerEndRow) {this.gegnerEndRow = gegnerEndRow;}

    public void setGegnerStartCol(int gegnerStartCol) {this.gegnerStartCol = gegnerStartCol;}
//  private int clickedCol=-1;


   // private int clickedRow=-1;

    //private String SpielerMail;//mit loggedInEmail initialisieren

    public ChessPuzzle(){

    }

   public String getSpielerMail() { return spielerMail;}


    public void setSpielerMail(String spielerMail) {
       this.spielerMail = spielerMail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(String puzzleId) {
        this.puzzleId = puzzleId;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getRatingDeviation() {
        return ratingDeviation;
    }

    public void setRatingDeviation(Integer ratingDeviation) {
        this.ratingDeviation = ratingDeviation;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Integer getNbPlays() {
        return nbPlays;
    }

    public void setNbPlays(Integer nbPlays) {
        this.nbPlays = nbPlays;
    }

    public String getThemes() {
        return themes;
    }

    public void setThemes(String themes) {
        this.themes = themes;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public String getOpeningTags() {
        return openingTags;
    }

    public void setOpeningTags(String openingTags) {
        this.openingTags = openingTags;
    }




 //   public int getClickedCol() {
  //      return clickedCol;
  //  }

 //   public void setClickedCol(int clickedCol) {
  //      this.clickedCol = clickedCol;
  //  }

 //   public int getClickedRow() {
 //       return clickedRow;
//    }

  //  public void setClickedRow(int clickedRow) {
 //       this.clickedRow = clickedRow;
//    }
}
