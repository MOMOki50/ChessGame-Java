package SEP.SEP.ChessPuzzle;

import SEP.SEP.ChessPuzzle.ChessPuzzle;
import SEP.SEP.ChessPuzzle.ChessPuzzleRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChessPuzzleImportService {
    private final ChessPuzzleRepository chessPuzzleRepository;

    public ChessPuzzleImportService(ChessPuzzleRepository chessPuzzleRepository) {
        this.chessPuzzleRepository = chessPuzzleRepository;
    }

    public List<ChessPuzzle> parseCsv(String csvData) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = new CSVParser(new StringReader(csvData), format);

        List<ChessPuzzle> puzzles = new ArrayList<>();
        for (CSVRecord record : parser) {
            ChessPuzzle puzzle = createChessPuzzleFromFields(record);
            puzzles.add(puzzle);
        }

        parser.close();

        return puzzles;
    }

    public void importPuzzles(List<ChessPuzzle> puzzles) {
        savePuzzles(puzzles);
    }

    private ChessPuzzle createChessPuzzleFromFields(CSVRecord record) {
        ChessPuzzle puzzle = new ChessPuzzle();

        puzzle.setPuzzleId(record.get("PuzzleId"));
        puzzle.setFen(record.get("FEN"));
        puzzle.setMoves(record.get("Moves"));
        // weitere Felder
        puzzle.setRating(parseInteger(record.get("Rating")));
        puzzle.setRatingDeviation(parseInteger(record.get("RatingDeviation")));
        puzzle.setPopularity(parseInteger(record.get("Popularity")));
        puzzle.setNbPlays(parseInteger(record.get("NbPlays")));
        puzzle.setThemes(record.get("Themes"));
        puzzle.setGameUrl(record.get("GameUrl"));
        puzzle.setOpeningTags(record.get("OpeningTags"));


        return puzzle;
    }


    private void savePuzzles(List<ChessPuzzle> puzzles) {
        chessPuzzleRepository.saveAll(puzzles);
    }

    private Integer parseInteger(String value) {
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Hier kannst du entscheiden, wie du mit ungültigen Werten umgehen möchtest
                return 0; // Standardwert zurückgeben
            }
        }
        return 0; // Standardwert zurückgeben, wenn der Wert null oder leer ist
    }
}