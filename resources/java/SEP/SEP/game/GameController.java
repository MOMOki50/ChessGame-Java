package SEP.SEP.game;

import SEP.SEP.FinishedGame.FinishedGame;
import SEP.SEP.FinishedGame.FinishedGameRepository;
import SEP.SEP.Notifications.NotificationsRepository;
import ch.astorm.jchess.core.Color;
import ch.astorm.jchess.core.Moveable;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.entities.Bishop;
import ch.astorm.jchess.core.entities.Knight;
import ch.astorm.jchess.core.entities.Queen;
import ch.astorm.jchess.core.entities.Rook;
import com.github.bhlangonijr.chesslib.*;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import SEP.SEP.user.UserService;
import com.github.bhlangonijr.chesslib.move.Move;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.Piece;
import ch.astorm.jchess.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.bhlangonijr.chesslib.Piece.*;


@Controller
    public class GameController {

        @Autowired
        private UserService userService;

        @Autowired
        private GameService gameService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private GameRepository gameRepository;

        @Autowired
        private NotificationsRepository notificationsRepository;

        @Autowired
        private FinishedGameRepository finishedGameRepository;

        @PostMapping("/save-gameinvite")
        public String startGame(Game game, Model model, jakarta.servlet.http.HttpSession session,
                                @RequestParam(value = "spielerSchwarz", required = true) String selectedSpielerEmail, @RequestParam("partiename") String partiename,
                                @RequestParam("timer") int timer) {

            String loggedInEmail = (String) session.getAttribute("loggedInEmail");
            List<Game> gamename=gameRepository.findByPartiename(partiename);
            if(userService.getByEmail(selectedSpielerEmail)==null){
                return "redirect:/create-game";
                //im 3. Zyklus auch prüfen, ob in savedgames vorhanden
            }
            if(gamename.isEmpty()==false) {
                return "redirect:/create-game";
            }
            if(finishedGameRepository.findByPartiename(partiename).isEmpty()==false){//Spielname schon in FinishedGame vorhanden
                return "redirect:/create-game";
            }
            game.setSpielerWeiß(loggedInEmail);
            game.setTimerSchwarz((long) timer*60000);
            game.setTimerWeiß((long) timer*60000);


            System.out.println("SELECTED SPIELER: " + selectedSpielerEmail);
            gameService.saveGame(game);


            return "redirect:/home";
        }

        @GetMapping(value = "/create-game", produces = "application/json")
        public String createGame(Model model, jakarta.servlet.http.HttpSession session) {
            String loggedInEmail = (String) session.getAttribute("loggedInEmail");
            User userData = userService.getUserData(loggedInEmail);

            if(loggedInEmail==null)
                return "redirect:/login";

            Set<User> friends = userData.getFriends();
            List<User> allUsers = userRepository.findAll();

            allUsers.remove(userData);

            System.out.println("Angemeldeter Benutzer: " + userData);
            System.out.println("Alle Nutzer: " + allUsers);

            model.addAttribute("currentUser", userData);
            model.addAttribute("allUsers", allUsers);
            model.addAttribute("friends", friends);

            return "spiel-starten";
        }

        @GetMapping("/play")
        public String playChess( Model model, jakarta.servlet.http.HttpSession session ) {
            //User userData = userService.getUserById(1L);
            String playerName = (String) session.getAttribute("loggedInEmail");
            if(playerName==null)
                return "redirect:/login";
            User userData = userRepository.getByEmail(playerName);
            model.addAttribute("currentUser", userData);

            Game game=gameService.getGameData(playerName);
            if(game==null){
                    return "redirect:/home";
            }

            if(game.getTimerWeiß()<=0){
               spielende(game.getSpielerSchwarz(),game.getSpielerWeiß(), game);
                gameRepository.delete(game);
            }

            if(game.getTimerSchwarz()<=0){
                spielende(game.getSpielerWeiß(),game.getSpielerSchwarz(), game);
                gameRepository.delete(game);
            }

            if(game.isPartieBeigetreten() && game.getFinishedGame()==null)
                erstellenVonFinishedGame(game);

            model.addAttribute("game", game);
            model.addAttribute("playerName", playerName);

            return "chessboard";
        }

        @GetMapping(value ="/check-game-started", produces = "application/json")
        public String checkGameStarted(Model model, jakarta.servlet.http.HttpSession session) {
            String playerName = (String) session.getAttribute("loggedInEmail");

            if (gameService.partieGestartet(playerName)) {
                List<Game> games = gameRepository.findBySpielerWeißAndPartieGestartetIsTrue(playerName);
                if(games.isEmpty()==false){
                    Game game=games.get(0);
                    if(game.isPartieBeigetreten()==false) {
                        game.setPartieBeigetreten(true);
                        erstellenVonFinishedGame(game);
                    }
                    if(game.getZeitpunkt()==null)
                        game.setZeitpunkt(new Date());
                    gameRepository.save(game);
                }
                return "redirect:/play";
            } else {
                return "redirect:/create-game";
            }
        }

        @GetMapping("/getGameData")
        @ResponseBody
        public Game getGameData(Model model, jakarta.servlet.http.HttpSession session) {
            String playerName = (String) session.getAttribute("loggedInEmail");
            Game game= gameService.getGameData(playerName);
            if(game!=null) {
                zeitAktualisieren(game);
                gameRepository.save(game);
            }
            return game;
        }

        //wenn spieler schwarz die Einladung annimmt soll sich chessboard für spieler weiß öffnen
        //hier wird abgefragt, ob sich die partie öffnen soll
        @GetMapping("/spielBeitreten")
        public ResponseEntity<Boolean> spielBeitreten(Model model, jakarta.servlet.http.HttpSession session) {
            String playerName = (String) session.getAttribute("loggedInEmail");
            boolean starten=gameService.spielBeitreten(playerName);
            Game game= gameService.getGameData(playerName);
            if(starten) {
                if(game.getZeitpunkt()==null) {
                    game.setZeitpunkt(new Date());
                    gameRepository.save(game);
                }
                else{
                    erstellenVonFinishedGame(game);
                }
                gameService.parteStartenTrue(playerName);
            }
            return ResponseEntity.ok(starten);
        }

        @GetMapping("/{col}/{row}/saveClick")
        public ResponseEntity<String> saveClick(@PathVariable int col, @PathVariable int row, jakarta.servlet.http.HttpSession session){
            String playerName = (String) session.getAttribute("loggedInEmail");
            Game game=gameService.getGameData(playerName);
            zeitAktualisieren(game);
            gameRepository.save(game);

            //wenn noch nicht geklickt wurde klick speichern
            if (game.getClickedCol()<0 && game.getClickedRow()<0) {
                game.setClickedCol(col);
                game.setClickedRow(row);
                gameRepository.save(game);
                return ResponseEntity.ok("false");
            }

            //wenn klick auf selbes feld zurücksetzen
            if (game.getClickedCol()==col && game.getClickedRow()==row){
                game.setClickedCol(-4);
                game.setClickedRow(-1);
                gameRepository.save(game);
                return ResponseEntity.ok("false");
            }

            //illegaler Zug, es passiert nichts (Spieler wieder mit selbem Spielbrett dran)
            String isLegalResult=isLegal(game, col, row);
            if(isLegalResult.equals("illegal Move")){
                game.setClickedCol(-3);
                game.setClickedRow(-1);
                gameRepository.save(game);
                return ResponseEntity.ok("false");
            }
            //wenn partie beendet (gewonnen oder unentschieden)
            if(isLegalResult=="matt" || isLegalResult=="unentschieden"){
                game.setWeißAmZug(!game.getWeißAmZug());
                game.setClickedCol(-1);
                game.setClickedRow(-1);
                gameRepository.save(game);
                //hier speicherung in Datenbank für beendete Spiele
                gameRepository.delete(game);
                return ResponseEntity.ok("true");
            }
            game.setCol1(-1);
            game.setCol2(-1);
            game.setRow1(-1);
            game.setRow2(-1);
            if(isLegalResult=="promotion"){
                return ResponseEntity.ok("promotion");
            }
            game.setWeißAmZug(!game.getWeißAmZug());
            game.setClickedCol(-2);
            game.setClickedRow(-1);
            gameRepository.save(game);
            return ResponseEntity.ok("false");

        }

        public String isLegal(Game game, int endCol, int endRow){ //beruht auf Code von com.github.bhlangonijr.chesslib
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
            boolean isWhite=game.getWeißAmZug();
            Move actMove=new Move(from,to);//default (bei nicht-Promotion)
            //bei Promition:
            if(isWhite && startRow==1 && board.getPiece(from)==WHITE_PAWN)
                actMove=new Move(from,to, WHITE_QUEEN);
            if(!isWhite && startRow==6 && board.getPiece(from)==BLACK_PAWN)
                actMove=new Move(from,to, BLACK_QUEEN);
            if(!legalMoves.contains(actMove)){
                return "illegal Move";
            }
            if((isWhite && startRow==1 && board.getPiece(from)==WHITE_PAWN) ||
                    (!isWhite && startRow==6 && board.getPiece(from)==BLACK_PAWN)) {
                game.setPromotionRow(endRow);
                game.setPromotionCol(endCol);
                gameRepository.save(game);
                return "promotion";
            }
            boolean capture=false;
            if(board.getPiece(to)==null)//wie wird leeres feld dargestellt?
                capture=true;
            String san=zugInSan(board, actMove, game);
            boolean erfolgreich=board.doMove(actMove, true); //gibt true bei legalem zug zurück, sonst false
            if(!erfolgreich) //falls stellung nach Zug illegal
                return "illegal Move";
            game.setStellung(board.getFen());
            game.addToHistory(board.getFen());
            FinishedGame finishedGame=game.getFinishedGame();
            finishedGame.addToHistory(board.getFen());
            if(game.getWeißAmZug()) {
                finishedGame.addMove(Integer.toString(game.getZugCount())+".");
                game.zugCountErhöhen();
            }
            finishedGame.addMove(san);
            game.addMove(san);
            //finishedGame.addMove(actMove.toString());
            //finishedGame.addMove(actMove.getSan()); gibt null heraus, da der san in dieser API nicht erzeugt werden kann
            //zugInSan(board, actMove); weiter oben

            finishedGameRepository.save(finishedGame);
            gameRepository.save(game);
            //test auf matt und patt
            if(board.isMated()) {
                if (game.getWeißAmZug()) {
                    spielende(game.getSpielerWeiß(), game.getSpielerSchwarz(), game);
                } else {
                    spielende(game.getSpielerSchwarz(), game.getSpielerWeiß(), game);
                }
                return "matt";
            }
            if(board.isDraw() || board.isStaleMate() || isRepetition(game)) {
                finishedGame.setResult("1/2-1/2");
                finishedGameRepository.save(finishedGame);
                return "unentschieden";
            }

            return "true";
        }

        public boolean isRepetition(Game game){
            String stellung=game.getStellung();
            if(game.getHistory().stream().filter(element -> substringBisLeerzeichen(element).equals(substringBisLeerzeichen(stellung))).collect(Collectors.toList()).size()==3)
                return true;
            return false;
        }

        public String substringBisLeerzeichen(String stellung){
            int firstSpaceIndex = stellung.indexOf(" ");
            return stellung.substring(0,firstSpaceIndex);
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

    public void spielende(String gewinner, String verlierer, Game game){
        User userGewinner=userRepository.findByEmail(gewinner);
        User userVerlierer=userRepository.findByEmail(verlierer);
        userGewinner.setElo(userGewinner.getElo()+10);
        userVerlierer.setElo(userVerlierer.getElo()-10);
        userRepository.save(userGewinner);
        userRepository.save(userVerlierer);
        FinishedGame finishedGame = game.getFinishedGame();
        if (finishedGame == null) {
            // Hier können Sie entweder ein neues FinishedGame-Objekt erstellen oder eine andere Logik einfügen
            // Beispiel: Erstellen eines neuen FinishedGame-Objekts
            finishedGame = new FinishedGame();
            finishedGame.setPartiename(game.getPartiename());
            finishedGame.setSpielerSchwarz(game.getSpielerSchwarz());
            finishedGame.setSpielerWeiß(game.getSpielerWeiß());
            finishedGame.setDate(new Date()); // Setzen Sie hier das aktuelle Datum
            finishedGame.setImported(false); // Falls zutreffend
            // Weitere Initialisierung von finishedGame nach Bedarf
            finishedGameRepository.save(finishedGame);
            game.setFinishedGame(finishedGame);
        }

        if (gewinner.equals(game.getSpielerWeiß())) {
            finishedGame.setResult("1-0");
        } else {
            finishedGame.setResult("0-1");
        }
        finishedGameRepository.save(finishedGame);
    }
    public int inSekunden(Date date){
           return date.getHours()*3600+date.getMinutes()*60+date.getSeconds();
    }

    public void zeitAktualisieren(Game game){
            if(game==null)//sonst fehler bei Verlierer, wenn Mattgesetzt
                return;
            if(game.getZeitpunkt()==null) //Zeit nicht aktualisieren, bis schwarz dem Spiel beitritt
                return;
            Date a=game.getZeitpunkt();
            Date b=new Date();
            long dif=(b.getTime()-a.getTime()); //in Millisekunden
        if (game.getWeißAmZug()) {
            game.setTimerWeiß(game.getTimerWeiß()-dif);
        }
        else{
            game.setTimerSchwarz(game.getTimerSchwarz()-dif);
        }
        game.setZeitpunkt(b);
        gameRepository.save(game);
    }

    @PostMapping("/aufgeben")
    public String aufgeben(jakarta.servlet.http.HttpSession session){
        String playerName = (String) session.getAttribute("loggedInEmail");
        Game game=gameService.getGameData(playerName);
        if(game==null)
            return "redirect:/home";
        if(playerName.equals(game.getSpielerWeiß()))
            spielende(game.getSpielerSchwarz(), playerName, game);
        else
            spielende(game.getSpielerWeiß(), playerName, game);
        //spiel in savedGame speichern
        gameRepository.delete(game);
        return "redirect:/home";
    }

    @PostMapping("/{figur}/promotion")//san für promotion: h8=Q ,endfeld + "=" + Figur als ein Buchstabe, contains zielfeld und figur
    public String promotion(@PathVariable String figur,jakarta.servlet.http.HttpSession session){
        figur = figur.replaceAll("\\{|\\}", ""); //nicht {FIGUR}, sodern FIGUR
        String playerName = (String) session.getAttribute("loggedInEmail");
        Game game=gameService.getGameData(playerName);
        if(game==null)
            return "redirect:/home";
        String promote;
        if(game.getWeißAmZug())
            promote="WHITE_"+figur;
        else
            promote="BLACK_"+figur;

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
            gameRepository.save(game);
            return "redirect:/play";
        }
        String san=promotionZugInSan(board, actMove, game, figur);
        boolean erfolgreich=board.doMove(actMove, true); //gibt true bei legalem zug zurück, sonst false
        game.setStellung(board.getFen());
        game.addToHistory(board.getFen());
        FinishedGame finishedGame=game.getFinishedGame();
        finishedGame.addToHistory(board.getFen());
        if(game.getWeißAmZug()) {
            finishedGame.addMove(Integer.toString(game.getZugCount())+".");
            game.zugCountErhöhen();
        }
        finishedGame.addMove(san);
        game.addMove(san);
        finishedGameRepository.save(finishedGame);
        gameRepository.save(game);
        //test auf matt und patt
        if(board.isMated()) {
            if (game.getWeißAmZug()) {
                spielende(game.getSpielerWeiß(), game.getSpielerSchwarz(), game);
            } else {
                spielende(game.getSpielerSchwarz(), game.getSpielerWeiß(), game);
            }
            //in savegame speichern (3. Zyklus)
            gameRepository.delete(game);
            return "redirect:/home";
        }
        if(board.isDraw() || board.isStaleMate()) {
            //in savegame speichern (3. Zyklus)
            gameRepository.delete(game);
            return "redirect:/home";
        }
        game.setWeißAmZug(!game.getWeißAmZug());
        game.setClickedCol(-6);
        game.setClickedRow(-1);
        game.setPromotionCol(-1);
        game.setPromotionRow(-1);
        gameRepository.save(game);
        return "redirect:/play";
    }

    @GetMapping("/promotion")
    public String showPromotionPage(Model model, jakarta.servlet.http.HttpSession session) {
            String loggedInEmail = (String) session.getAttribute("loggedInEmail");

            Boolean game = gameService.getGameData(loggedInEmail).getWeißAmZug();

            model.addAttribute("game", game);

        return "promotion";
    }

    public void erstellenVonFinishedGame(Game game){
        FinishedGame finishedGame=new FinishedGame();
        finishedGame.setDate(new Date());
        finishedGame.setPartiename(game.getPartiename());
        finishedGame.setSpielerSchwarz(game.getSpielerSchwarz());
        finishedGame.setSpielerWeiß(game.getSpielerWeiß());
        finishedGame.setImported(false);
        finishedGameRepository.save(finishedGame);
        game.setFinishedGame(finishedGame);
        gameRepository.save(game);
    }

    public String promotionZugInSan(Board oldBoard, Move move, Game actGame, String figur){
        Board board=oldBoard.clone();
        JChessGame jGame = JChessGame.newGame();
        List<String> actGameMoves=actGame.getMoves();
        if(!actGameMoves.isEmpty()){//wurden schon moves gespielt
            for(int i=0; i<actGameMoves.size(); i++)
                jGame.play(actGameMoves.get(i));
        }//jGame hat jetzt korrekte Stellung (vor aktuellem Zug)
        List<ch.astorm.jchess.core.Move> possibleMoves= jGame.getAvailableMoves();
        if(possibleMoves.isEmpty())
            return null;
        String fig=welcheFigur(figur);
        boolean iswhite= actGame.getWeißAmZug();
        Color color= Color.BLACK ;
        if(iswhite)
            color=Color.WHITE;//oder farbe, die die methode erwartet
        for(int i=0; i<possibleMoves.size(); i++){
            possibleMoves.get(i).setPromotionNeeded(true);//sind aber nicht alles promotion züge, sondern alle möglichen Züge
            if(figur.equals("QUEEN"))
                possibleMoves.get(i).setPromotion(new Queen(color));
            if(figur.equals("ROOK"))
                possibleMoves.get(i).setPromotion(new Rook(color));
            if(figur.equals("BISHOP"))
                possibleMoves.get(i).setPromotion(new Bishop(color));
            if(figur.equals("KNIGHT"))
                possibleMoves.get(i).setPromotion(new Knight(color));
        }
        ArrayList<ch.astorm.jchess.core.Move> filteredMoves= (ArrayList<ch.astorm.jchess.core.Move>) possibleMoves.stream().filter(element -> element.toString().contains(move.toString().substring(2,4))).collect(Collectors.toList());
        filteredMoves= (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> element.toString().contains(fig)).collect(Collectors.toList());
        if(filteredMoves.size()>1){
            String z = Character.toString(filteredMoves.get(0).toString().charAt(0));//char an zweiterstelle entweder zahl 1-8 oder Buchstabe a-h
            if (z.equals("a") || z.equals("b") || z.equals("c") || z.equals("d") || z.equals("e") || z.equals("f") || z.equals("g") || z.equals("h")) {
                filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(0)).equals(Character.toString(move.toString().charAt(0)))).collect(Collectors.toList());
            } else {//zweites Zeichen ist Zahl 1-8
                filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(0)).equals(Character.toString(move.toString().charAt(1)))).collect(Collectors.toList());
            }
        }
        return filteredMoves.get(0).toString();
    }

    public String welcheFigur(String figur){
        switch(figur) {
            case "QUEEN": return "Q";
            case "ROOK": return "R";
            case "BISHOP": return "B";
            case "KNIGHT": return "N";
        }
        return "";
    }

    public String zugInSan(Board oldBoard, Move move, Game actGame){
            Board board=oldBoard.clone();
            JChessGame jGame = JChessGame.newGame();
            List<String> actGameMoves=actGame.getMoves();
            if(!actGameMoves.isEmpty()){//wurden schon moves gespielt
                for(int i=0; i<actGameMoves.size(); i++)
                    jGame.play(actGameMoves.get(i));
            }//jGame hat jetzt korrekte Stellung (vor aktuellem Zug)
            List<ch.astorm.jchess.core.Move> possibleMoves= jGame.getAvailableMoves();
            if(possibleMoves.isEmpty())
                return null;
            //Rochade prüfen
                if(board.getPiece(move.getFrom())==BLACK_KING || board.getPiece(move.getFrom())==WHITE_KING){
                    if(Character.toString(move.toString().charAt(0)).equals("e")){
                        String zielSpalte=Character.toString(move.toString().charAt(2));
                        if(zielSpalte.equals("a")||zielSpalte.equals("b")||zielSpalte.equals("c"))
                            return "O-O-O"; //lange Rochade
                        if(zielSpalte.equals("g")||zielSpalte.equals("h"))
                            return "O-O"; //kurze Rochade
                    }
                }
            ArrayList<ch.astorm.jchess.core.Move> filteredMoves= (ArrayList<ch.astorm.jchess.core.Move>) possibleMoves.stream().filter(element -> element.toString().contains(move.toString().substring(2))).collect(Collectors.toList());
            if(filteredMoves.isEmpty())
                return null;
            if(filteredMoves.size()>1){//bei mehreren mögliches moves zuerst nach figur (1. Zeichen filtern)
                Piece isPawn=board.getPiece(move.getFrom());
                if(isPawn==BLACK_PAWN || isPawn==WHITE_PAWN) {
                    filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> isNotPawn(Character.toString(element.toString().charAt(0)))).collect(Collectors.toList());
                    if(filteredMoves.size()>1) {//wenn immernoch mehr Möglichkeiten da sind, dann gucken, ob 2. Zeichen gleich reihe oder spalte
                        String z = Character.toString(filteredMoves.get(0).toString().charAt(0));//char an zweiterstelle entweder zahl 1-8 oder Buchstabe a-h
                        if (z.equals("a") || z.equals("b") || z.equals("c") || z.equals("d") || z.equals("e") || z.equals("f") || z.equals("g") || z.equals("h")) {
                            filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(0)).equals(Character.toString(move.toString().charAt(0)))).collect(Collectors.toList());
                        } else {//zweites Zeichen ist Zahl 1-8
                            filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(0)).equals(Character.toString(move.toString().charAt(1)))).collect(Collectors.toList());
                        }
                }
                }
                else{
                    filteredMoves= (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(0)).equals(getFigurenBuchstabe(board.getPiece(move.getFrom())))).collect(Collectors.toList());
                if(filteredMoves.size()>1) {//wenn immernoch mehr Möglichkeiten da sind, dann gucken, ob 2. Zeichen gleich reihe oder spalte
                    String z = Character.toString(filteredMoves.get(0).toString().charAt(1));//char an zweiterstelle entweder zahl 1-8 oder Buchstabe a-h
                    if (z.equals("a") || z.equals("b") || z.equals("c") || z.equals("d") || z.equals("e") || z.equals("f") || z.equals("g") || z.equals("h")) {
                        filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(1)).equals(Character.toString(move.toString().charAt(0)))).collect(Collectors.toList());
                    } else {//zweites Zeichen ist Zahl 1-8
                        filteredMoves = (ArrayList<ch.astorm.jchess.core.Move>) filteredMoves.stream().filter(element -> Character.toString(element.toString().charAt(1)).equals(Character.toString(move.toString().charAt(1)))).collect(Collectors.toList());
                    }
                }
                }
            }
            return filteredMoves.get(0).toString();
    }

    public boolean isNotPawn(String s){//false, wenn andere figur als bauer
            if(s.equals("R")||s.equals("N")||s.equals("B")||s.equals("Q")||s.equals("K"))
                return false;
            return true;
    }

    public String getFigurenBuchstabe(Piece piece){
        switch (piece) {
            case BLACK_ROOK: return "R";
            case BLACK_KNIGHT: return "N";
            case BLACK_BISHOP: return "B";
            case BLACK_QUEEN: return "Q";
            case BLACK_KING: return "K";
            case BLACK_PAWN: return "P";
            case WHITE_ROOK: return "R";
            case WHITE_KNIGHT: return "N";
            case WHITE_BISHOP: return "B";
            case WHITE_QUEEN: return "Q";
            case WHITE_KING: return "K";
            case WHITE_PAWN: return "P";
        }
            return " ";
    }

    public void fenGenerator(FinishedGame game){
        JChessGame jGame = JChessGame.newGame();
        List<String> moves=getMoves(game);
        game.setHistory(new ArrayList<>(Arrays.asList("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")));
        for(String move:moves) {
            jGame.play(move);
            String fen = "";
            Position position = jGame.getPosition();
            for (int row = 7; row >=0; row--) {
                for (int col = 0; col < 8; col++) {
                    Moveable piece = position.get(row, col);
                    if (piece == null) {
                        fen += " ";
                        continue;
                    }
                    if (piece.getColor().equals(Color.WHITE)) {
                        fen += pieceToLetter("white", piece);
                    } else {
                        fen += pieceToLetter("black", piece);
                    }
                }
            }
            game.addToHistory(toFen(fen));
        }
        finishedGameRepository.save(game);
    }

    public String pieceToLetter(String colour, Moveable moveablePiece){
        String piece= moveablePiece.toString();
        if(colour.equals("white")){
            if(piece.contains("Pawn"))
                return "P";
            if(piece.contains("King"))
                return "K";
            if(piece.contains("Rook"))
                return "R";
            if(piece.contains("Knight"))
                return "N";
            if(piece.contains("Bishop"))
                return "B";
            if(piece.contains("Queen"))
                return "Q";
        }
        if(piece.contains("Pawn"))
            return "p";
        if(piece.contains("King"))
            return "k";
        if(piece.contains("Rook"))
            return "r";
        if(piece.contains("Knight"))
            return "n";
        if(piece.contains("Bishop"))
            return "b";
        if(piece.contains("Queen"))
            return "q";
        return null;
    }

    public String toFen(String position){
        String result="";
        int emptyCounter=0;
        for(int row=0; row<8; row++) {
            if(emptyCounter!=0){
                result+=emptyCounter;
                emptyCounter=0;
            }
            if(row!=0)
                result+="/";
            for (int col = 0; col < 8; col++) {
                int numberOfField=row*8+col;
                String figur=Character.toString(position.charAt(numberOfField));
                if(figur.equals(" ")) {
                    emptyCounter++;
                    continue;
                }
                if(emptyCounter!=0){
                    result+=emptyCounter;
                    emptyCounter=0;
                }
                result+=figur;
            }
        }
        if(emptyCounter!=0)
            result+=emptyCounter;
        return result;
    }

    public List<String> getMoves(FinishedGame game){
            String moveString=game.getMoves();
        if(moveString != null && moveString.length() > 1) {
            moveString = moveString.substring(1);
        }
            String[] moves = moveString.split(" ");
            List<String> result=new ArrayList<>();
            for(int i=0; i<moves.length; i++){
                if(i%3==0)//nummerierung der Züge
                    continue;
                if(moves[i].equals("1-0") || moves[i].equals("0-1") || moves[i].equals("1/2-1/2"))
                    continue;
                result.add(moves[i]);
            }
        return result;
    }

    @GetMapping("/{id}/getLiveGameData")
    @ResponseBody
    public Game getliveGameData(@PathVariable Long id) {
        Game game = gameRepository.findById(id).get();

        if(game != null) {
            zeitAktualisieren(game);
            gameRepository.save(game);
        }
        return game;
    }
}