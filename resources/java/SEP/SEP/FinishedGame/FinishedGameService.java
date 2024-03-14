package SEP.SEP.FinishedGame;


import SEP.SEP.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class FinishedGameService {

    @Autowired
    private FinishedGameRepository finishedGameRepository;

    public FinishedGame createFinishedGameFromPgn(String pgnContent) throws ParseException {
        FinishedGame finishedGame = new FinishedGame();

        finishedGame.setPartiename(extractInfo(pgnContent, "Event"));
        finishedGame.setSpielerWeiß(extractInfo(pgnContent, "White"));
        finishedGame.setSpielerSchwarz(extractInfo(pgnContent, "Black"));
        finishedGame.setDate(extractDate(pgnContent));
        finishedGame.setMoves(extractMoves(pgnContent));
        finishedGame.setResult(extractInfo(pgnContent, "Result"));
        finishedGame.setImported(true);

        return finishedGame;
    }

    private String extractInfo(String pgn, String tag) {
        Pattern pattern = Pattern.compile("\\[" + tag + " \"(.*?)\"]");
        Matcher matcher = pattern.matcher(pgn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private Date extractDate(String pgn) throws ParseException {
        String dateString = extractInfo(pgn, "Date");
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        return format.parse(dateString);
    }

    private String extractMoves(String pgn) {
        int lastBracketIndex = pgn.lastIndexOf(']');
        if (lastBracketIndex != -1) {
            return pgn.substring(lastBracketIndex + 1).trim();
        }
        return "";
    }


    public String convertToPgn(FinishedGame finishedGame) {
        StringBuilder pgn = new StringBuilder();

        pgn.append("[Event \"").append(finishedGame.getPartiename()).append("\"]\n");
        pgn.append("[Site \"SEP\"]\n");
        pgn.append("[Date \"").append(new SimpleDateFormat("yyyy.MM.dd").format(finishedGame.getDate())).append("\"]\n");
        pgn.append("[Round \"1\"]\n");
        pgn.append("[White \"").append(finishedGame.getSpielerWeiß()).append("\"]\n");
        pgn.append("[Black \"").append(finishedGame.getSpielerSchwarz()).append("\"]\n");
        pgn.append("[Result \"").append(finishedGame.getResult()).append("\"]\n\n");

        // Züge hinzufügen
        pgn.append(finishedGame.getMoves());

        return pgn.toString();
    }

    public List<FinishedGame> getRecentFinishedGames(int numberOfGames, jakarta.servlet.http.HttpSession session) {
        String email=(String) session.getAttribute("loggedInEmail");
        List<FinishedGame> allFinishedGames = finishedGameRepository.findByImportedIsFalseAndSpielerWeißAndResultIsNotNullOrImportedIsFalseAndSpielerSchwarzAndResultIsNotNull(email,email);
        allFinishedGames.sort(Comparator.comparing(FinishedGame::getDate));
        // Auswahl der neuesten Spiele (maximal `numberOfGames` Spiele)
        List<FinishedGame> recentFinishedGames = allFinishedGames.subList(Math.max(0, allFinishedGames.size() - numberOfGames), allFinishedGames.size());

        // Umkehrung der Liste
        Collections.reverse(recentFinishedGames);

        return recentFinishedGames;
    }
}

