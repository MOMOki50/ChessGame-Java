package SEP.SEP.ChessPuzzle;

import SEP.SEP.ChessPuzzle.ChessPuzzle;
import SEP.SEP.ChessPuzzle.ChessPuzzleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
public class ChessPuzzleService {

    private final ChessPuzzleRepository chessPuzzleRepository;
    private final ChessPuzzleImportService chessPuzzleImportService;

    public ChessPuzzleService(ChessPuzzleRepository chessPuzzleRepository, ChessPuzzleImportService chessPuzzleImportService) {
        this.chessPuzzleRepository = chessPuzzleRepository;
        this.chessPuzzleImportService = chessPuzzleImportService;
    }

    public List<ChessPuzzle> getAllPuzzles() {
        return chessPuzzleRepository.findAll();
    }

    public void importPuzzlesFromCsv(String csvData) {
        try {
            List<ChessPuzzle> puzzles = chessPuzzleImportService.parseCsv(csvData);
            chessPuzzleImportService.importPuzzles(puzzles);
        } catch (IOException e) {


            throw new RuntimeException("Fehler beim Importieren der Schachpuzzles.", e);
        }
    }
}