package SEP.SEP.ChessPuzzle;

import SEP.SEP.game.Game;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.File;
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.bhlangonijr.chesslib.Piece.*;
import static com.github.bhlangonijr.chesslib.Piece.BLACK_PAWN;
import static java.lang.Integer.parseInt;

@Controller
public class ChessPuzzleController {

    @Autowired
    private ChessPuzzleService chessPuzzleService;

    @Autowired
    private ChessPuzzleRepository chessPuzzleRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/import")
    public ResponseEntity<String> importChessPuzzles(@RequestParam("file") MultipartFile file) {
        try {
            String csvData= new String(file.getBytes(), StandardCharsets.UTF_8);
            chessPuzzleService.importPuzzlesFromCsv(csvData);
            return ResponseEntity.ok("Schachpuzzles erfolgreich importiert.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Importieren der Schachpuzzles.");
        }
    }

    @GetMapping("/ChessPuzzle")
    public String showPuzzle(Model model, HttpSession session) {
        model.addAttribute("pageTitle", "Import Chesspuzzle");
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if(!chessPuzzleRepository.findBySpielerMail(null).isEmpty() || !chessPuzzleRepository.findBySpielerMail(loggedInEmail).isEmpty()){
            ChessPuzzle puzzle=null;
            if(chessPuzzleRepository.findBySpielerMail(loggedInEmail).isEmpty())
                puzzle=chessPuzzleRepository.findBySpielerMail(null).get(0);
            else puzzle=getPuzzleData(session);
            puzzle.setSpielerMail(loggedInEmail);
            chessPuzzleRepository.save(puzzle);
            if(!puzzle.getAmZug()){
            puzzle.setAmZug(true);
            chessPuzzleRepository.save(puzzle);
            zugDesGegners(session);
            }
            return "GameChesspuzzle";
        }
        return "ImportChesspuzzle";
    }

    @GetMapping("/getPuzzleData")
    @ResponseBody
    public ChessPuzzle getPuzzleData(jakarta.servlet.http.HttpSession session){
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        ChessPuzzle puzzle= chessPuzzleRepository.findBySpielerMail(loggedInEmail).get(0); //null für loggedInEmail zum testen, sobal spieler eingetragen wird ändern
        //ChessPuzzle puzzle= chessPuzzleRepository.getById(Long.valueOf(1));//test
        return puzzle;
    }

    public void zugDesGegners(jakarta.servlet.http.HttpSession session){
        ChessPuzzle puzzle=getPuzzleData(session);
        String moves=puzzle.getMoves();
        puzzle.setMoves(moves.substring(5));
        int startRow= 8-parseInt(String.valueOf(moves.charAt(1)), 10);
        puzzle.setGegnerStartRow(startRow);
        puzzle.setClickedRow(startRow);
        int endRow=8-parseInt(String.valueOf(moves.charAt(3)), 10);
        puzzle.setGegnerEndRow(endRow);
        int startCol=getPuzzleBuchstabe(String.valueOf(moves.charAt(0)));
        puzzle.setGegnerStartCol(startCol);
        puzzle.setClickedCol(startCol);
        int endCol=getPuzzleBuchstabe(String.valueOf(moves.charAt(2)));
        puzzle.setGegnerEndCol(endCol);
        doMove(puzzle, endCol, endRow);

        puzzle.setClickedCol(-1);
        puzzle.setClickedRow(-1);
        chessPuzzleRepository.save(puzzle);
    }

    public int getPuzzleBuchstabe(String Spalte){
        switch (Spalte) {
            case "a":return 0;
            case "b":return 1;
            case "c":return 2;
            case "d":return 3;
            case "e":return 4;
            case "f":return 5;
            case "g":return 6;
            case "h":return 7;
        }
        return -1;
    }

    public void endPuzzle(jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        chessPuzzleRepository.delete(getPuzzleData(session));
        User user=userRepository.findByEmail(playerName);
        user.setAnzahlGeloestePuzzle(user.getAnzahlGeloestePuzzle()+1);
        userRepository.save(user);
    }

    @GetMapping("/{col}/{row}/savePuzzleClick")
    public ResponseEntity<String> saveClick(@PathVariable int col, @PathVariable int row, jakarta.servlet.http.HttpSession session) {
        String playerName = (String) session.getAttribute("loggedInEmail");
        ChessPuzzle puzzle = getPuzzleData(session);

        //wenn noch nicht geklickt wurde klick speichern
        if (puzzle.getClickedCol()<0 && puzzle.getClickedRow()<0) {
            puzzle.setClickedCol(col);
            puzzle.setClickedRow(row);
            chessPuzzleRepository.save(puzzle);
            return ResponseEntity.ok("false");
        }

        int startCol=puzzle.getClickedCol();
        int startRow=puzzle.getClickedRow();
        int endCol=col;
        int endRow=row;
        String moves=puzzle.getMoves();

        if(!(startCol==getPuzzleBuchstabe(String.valueOf(moves.charAt(0)))))
            return falscherZug(puzzle);
        if(!(endCol==getPuzzleBuchstabe(String.valueOf(moves.charAt(2)))))
            return falscherZug(puzzle);
        if(!(startRow==8-parseInt(String.valueOf(moves.charAt(1)), 10)))
            return falscherZug(puzzle);
        if(!(endRow==8-parseInt(String.valueOf(moves.charAt(3)), 10)))
            return falscherZug(puzzle);

        doMove(puzzle, endCol, endRow);
        moves=moves.substring(4);
        if(moves.length()<1)
            endPuzzle(session);
        else {
            moves=moves.substring(1);
            puzzle.setClickedCol(-1);
            puzzle.setClickedRow(-1);
            puzzle.setMoves(moves);
            puzzle.setAmZug(false);
            chessPuzzleRepository.save(puzzle);
            }

        return ResponseEntity.ok("true");
    }

    public ResponseEntity<String> falscherZug(ChessPuzzle puzzle){
        puzzle.setClickedCol(-1);
        puzzle.setClickedRow(-1);
        chessPuzzleRepository.save(puzzle);
        return ResponseEntity.ok("false");
    }

    public void doMove(ChessPuzzle puzzle, int endCol, int endRow){
        int startCol=puzzle.getClickedCol();
        int startRow=puzzle.getClickedRow();
        Board board=new Board();
        board.loadFromFen(puzzle.getFen());
        Rank startRank= Rank.fromValue("RANK_" +Integer.toString(8-startRow));
        Rank endRank= Rank.fromValue("RANK_" +Integer.toString(8-endRow));
        File startFile=File.fromValue("FILE_" + getBuchstabe(startCol));
        File endFile=File.fromValue("FILE_" + getBuchstabe(endCol));
        Square from= Square.encode(startRank, startFile);
        Square to= Square.encode(endRank, endFile);
        Move actMove=new Move(from,to);//default (bei nicht-Promotion)
        //bei Promition:
        if(startRow==1 && board.getPiece(from)==WHITE_PAWN)
            actMove=new Move(from,to, WHITE_QUEEN);
        if(startRow==6 && board.getPiece(from)==BLACK_PAWN)
            actMove=new Move(from,to, BLACK_QUEEN);
        boolean erfolgreich=board.doMove(actMove, true); //gibt true bei legalem zug zurück, sonst false
        puzzle.setFen(board.getFen());
        chessPuzzleRepository.save(puzzle);
    }

    public String getBuchstabe(int Spalte){
        switch (Spalte) {
            case 0:return "A";
            case 1:return "B";
            case 2:return "C";
            case 3:return "D";
            case 4:return "E";
            case 5:return "F";
            case 6:return "G";
            case 7:return "H";
        }
        return null;
    }

}
