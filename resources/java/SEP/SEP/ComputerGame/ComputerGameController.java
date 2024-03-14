package SEP.SEP.ComputerGame;

import SEP.SEP.ChessComputer.Stockfish;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import SEP.SEP.user.UserService;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static com.github.bhlangonijr.chesslib.Piece.*;

@Controller
public class  ComputerGameController {

    @Autowired
    private UserService userService;

    @Autowired
    private ComputerGameService computerGameService;

    @Autowired
    private ComputerGameRepository computerGameRepository;

    @Autowired
    private UserRepository userRepository;





    @GetMapping(value = "/computerGame", produces = "application/json")
    public String  createGame(Model model, jakarta.servlet.http.HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if(loggedInEmail==null)
            return "redirect:/login";

        ComputerGame game=computerGameService.getComputerGame(loggedInEmail);
        if(game==null)
            return "redirect:/home";
        zeitAnpassen(session);
        model.addAttribute("playerName", loggedInEmail);



        return  "computerGame";
    }




    @GetMapping("/getComputerGameData")
    @ResponseBody
    public ComputerGame  getGameData(jakarta.servlet.http.HttpSession session) {
        String playerName = (String) session.getAttribute("loggedInEmail");
        ComputerGame game=computerGameService.getComputerGame(playerName);
        return game;
    }

    @GetMapping("/{col}/{row}/computerGameClick")
    public ResponseEntity<String> saveClick(@PathVariable int col, @PathVariable int row, jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        ComputerGame game=computerGameService.getComputerGame(playerName);

        //wenn noch nicht geklickt wurde klick speichern
        if (game.getClickedCol()<0 && game.getClickedRow()<0) {
            game.setClickedCol(col);
            game.setClickedRow(row);
            computerGameRepository.save(game);
            return ResponseEntity.ok("false");
        }

        //wenn klick auf selbes feld zurücksetzen
        if (game.getClickedCol()==col && game.getClickedRow()==row){
            game.setClickedCol(-4);
            game.setClickedRow(-1);
            computerGameRepository.save(game);
            return ResponseEntity.ok("false");
        }

        //illegaler Zug, es passiert nichts (Spieler wieder mit selbem Spielbrett dran)
        String isLegalResult=isLegal(game, col, row);
        if(isLegalResult.equals("illegal Move")){
            game.setClickedCol(-3);
            game.setClickedRow(-1);
            computerGameRepository.save(game);
            return ResponseEntity.ok("false");
        }
        //wenn partie beendet (gewonnen oder unentschieden)
        if(isLegalResult=="unentschieden"){
            computerGameRepository.delete(game);
            return ResponseEntity.ok("true");
        }
        if(isLegalResult=="matt"){
            User user=userRepository.getByEmail(game.getSpieler());
            user.setElo(user.getElo()+10);
            computerGameRepository.delete(game);
            return ResponseEntity.ok("true");
    }
        if(isLegalResult=="promotion"){
        return ResponseEntity.ok("promotion");
    }
        game.setClickedCol(-2);
        game.setClickedRow(-1);
        computerGameRepository.save(game);
        computerzug(game);
        return ResponseEntity.ok("false");
    }

    public String isLegal(ComputerGame game, int endCol, int endRow){ //beruht auf Code von com.github.bhlangonijr.chesslib
        int startCol=game.getClickedCol();
        int startRow=game.getClickedRow();
        Board board=new Board();
        board.loadFromFen(game.getStellung());
        List<Move> legalMoves=board.legalMoves();
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
        if(!legalMoves.contains(actMove)){
            return "illegal Move";
        }
        if(startRow==1 && board.getPiece(from)==WHITE_PAWN) {
            game.setPromotionRow(endRow);
            game.setPromotionCol(endCol);
            computerGameRepository.save(game);
            return "promotion";
        }
        boolean capture=false;
        if(board.getPiece(to)==null)//wie wird leeres feld dargestellt?
            capture=true;
        //String san=zugInSan(board, actMove, game);
        boolean erfolgreich=board.doMove(actMove, true); //gibt true bei legalem zug zurück, sonst false
        if(!erfolgreich) //falls stellung nach Zug illegal
            return "illegal Move";
        game.setStellung(board.getFen());
        computerGameRepository.save(game);
        //test auf matt und patt
        if(board.isMated()) {
            return "matt";
        }
        if(board.isDraw() || board.isStaleMate()) {
            return "unentschieden";
        }

        return "true";
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



    public void  computerzug(ComputerGame game){//ToDo Zug je nach SChwierigkeit spielen
        int schwierigkeit=game.getSchwierigkeit();
        Board board=new Board();
        board.loadFromFen(game.getStellung());
        if(schwierigkeit==2){
            double anteilZufälligeZüge=0.3;
            if(Math.random()<anteilZufälligeZüge)
                schwierigkeit=1;
            else
                schwierigkeit=3;
        }
        if(schwierigkeit==1){
            List<Move> legalMoves=board.legalMoves();
            int index=(int) (Math.random()*legalMoves.size());
            board.doMove(legalMoves.get(index));
            game.setStellung(board.getFen());
        }
        if(schwierigkeit==3){
            System.out.println("Test Stockfish");
            Stockfish stockfish=new Stockfish();
            String zug=stockfish.bestMove(game.getStellung(),"1000", "20");
            System.out.println(zug);
            //String zug="e7e5";



            Rank startRank= Rank.fromValue("RANK_" +zug.charAt(1));
            Rank endRank= Rank.fromValue("RANK_" +zug.charAt(3));
            File startFile=File.fromValue("FILE_" + Character.toString(zug.charAt(0)).toUpperCase());
            File endFile=File.fromValue("FILE_" + Character.toString(zug.charAt(2)).toUpperCase());
            Square from= Square.encode(startRank, startFile);
            Square to= Square.encode(endRank, endFile);
            Move actMove=new Move(from,to);


            if(zug.length()>4)
                actMove=new Move(from,to, BLACK_QUEEN);

            board.doMove(actMove);
            String fen=board.getFen();
            game.setStellung(fen);
            board.loadFromFen(fen);
        }
        computerGameRepository.save(game);
        if(board.isDraw() || board.isStaleMate())
            computerGameRepository.delete(game);
        if(board.isMated()) {
            User user=userRepository.getByEmail(game.getSpieler());
            user.setElo(user.getElo()-10);
            computerGameRepository.delete(game);
        }
    }



    @PostMapping("/{figur}/computerGamePromotion")//san für promotion: h8=Q ,endfeld + "=" + Figur als ein Buchstabe, contains zielfeld und figur
    public String promotion(@PathVariable String figur,jakarta.servlet.http.HttpSession session){
        figur = figur.replaceAll("\\{|\\}", ""); //nicht {FIGUR}, sodern FIGUR
        String playerName = (String) session.getAttribute("loggedInEmail");
        ComputerGame game=computerGameService.getComputerGame(playerName);
        if(game==null)
            return "redirect:/home";
        String promote="WHITE_"+figur;

        //Zug machen
        int startCol=game.getClickedCol();
        int startRow=game.getClickedRow();
        int endRow=game.getPromotionRow();
        int endCol=game.getPromotionCol();
        Board board=new Board();
        board.loadFromFen(game.getStellung());
        List<Move> legalMoves=board.legalMoves();
        Rank startRank= Rank.fromValue("RANK_" +Integer.toString(8-startRow));
        Rank endRank= Rank.fromValue("RANK_" +Integer.toString(8-endRow));
        File startFile=File.fromValue("FILE_" + getBuchstabe(startCol));
        File endFile=File.fromValue("FILE_" + getBuchstabe(endCol));
        Square from= Square.encode(startRank, startFile);
        Square to= Square.encode(endRank, endFile);
        Move actMove=new Move(from,to, Piece.fromValue(promote));
        if(!legalMoves.contains(actMove)){
            game.setClickedCol(-5);
            game.setClickedRow(-1);
            game.setPromotionCol(-1);
            game.setPromotionRow(-1);
            computerGameRepository.save(game);
            return "redirect:/computerGame";
        }
        //String san=promotionZugInSan(board, actMove, game, figur);
        boolean erfolgreich=board.doMove(actMove, true); //gibt true bei legalem zug zurück, sonst false
        game.setStellung(board.getFen());

        computerGameRepository.save(game);
        //test auf matt und patt
        if(board.isMated()) {
            User user=userRepository.getByEmail(game.getSpieler());
            user.setElo(user.getElo()+10);
            computerGameRepository.delete(game);
            return "redirect:/home";
        }
        if(board.isDraw() || board.isStaleMate()) {
            computerGameRepository.delete(game);
            return "redirect:/home";
        }
        game.setClickedCol(-6);
        game.setClickedRow(-1);
        game.setPromotionCol(-1);
        game.setPromotionRow(-1);
        computerzug(game);
        computerGameRepository.save(game);
        return "redirect:/computerGame";
    }

    @GetMapping("/computerGamePromotion")
    public String showPromotionPage(Model model, jakarta.servlet.http.HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        Boolean game = true; //Spieler ist immer weiß

        model.addAttribute("game", game);

        return "computerGamePromotion";
    }


    @GetMapping(value = "/computerspiel", produces = "application/json")//zum testen von fenGenerator, bis richtiger Import möglich ist, Partie mit Id=1 muss vorhanden sein
    public String  test(Model model, jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        if (computerGameRepository.findBySpieler(playerName).isEmpty()) {
            return "difficulty";
        }
        User  userData = userService.getUserData(playerName);
        if(playerName==null)
            return "redirect:/login";
        model.addAttribute("currentUser", userData);

        return "redirect:/computerGame";
    }



    @PostMapping("/computerSpielErstellen")
    public String  showComputerGame(@RequestParam int schwierigkeit, @RequestParam int timer, jakarta.servlet.http.HttpSession session) {
        String  loggedInEmail = (String) session.getAttribute("loggedInEmail");
        ComputerGame game = new ComputerGame();
        game.setSpieler(loggedInEmail);
        game.setSchwierigkeit(schwierigkeit);
        game.setTimer(timer*60000);//in Millisekunden
        computerGameRepository.save(game);

        return "redirect:/computerGame";
    }

    public void zeitAnpassen(jakarta.servlet.http.HttpSession session){
        ComputerGame game=getGameData(session);
        Date newDate= new Date();
        game.setTimer(game.getTimer()-(newDate.getTime()-game.getZeitpunkt().getTime()));
        game.setZeitpunkt(newDate);
        computerGameRepository.save(game);
    }



    @PostMapping ("/timerAbgelaufen")
    public String timerAbgelaufen(jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        User user=userRepository.getByEmail(playerName);
        user.setElo(user.getElo()-10);
        computerGameRepository.deleteAll(computerGameRepository.findBySpieler(playerName));
        return "redirect:/home";
    }


}
