package SEP.SEP.ChessComputer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.nio.file.*;

public class Stockfish {
    private String stockfishPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String stockfishPath = null;
        if (osName.contains("win")) {
            Path stockfishRelativePath = Paths.get("src", "main", "resources", "stockfish", "stockfish-windows-x86-64-avx2.exe");
            stockfishPath = stockfishRelativePath.toAbsolutePath().toString();
            return stockfishPath;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            return stockfishPath = "/stockfish/stockfish_14_x64_avx2";
        } else if (osName.contains("mac")) {
            Path stockfishRelativePath = Paths.get("src", "main", "resources", "stockfish", "stockfish_14_x64_avx2");
            stockfishPath = stockfishRelativePath.toAbsolutePath().toString();
            return stockfishPath;
        } else {
            System.err.println("Unsupported operating system: " + osName);
        }
        return stockfishPath();
    }

    public String bestMove(String fenPosition, String time, String depth) {
        String bestMove = null;
        String command = "position fen " + fenPosition + "\n" + "go movetime " + time + " depth " + depth;
        String stockfishPath = stockfishPath();
        String line = "empty";
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(stockfishPath);
            processBuilder.redirectErrorStream(true); // leitet Fehlerausgabe in Standardausgabe um
            Process process = processBuilder.start();

            if (process.isAlive()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                OutputStream outputStream = process.getOutputStream();
                outputStream.write(command.getBytes());
                outputStream.flush();

                /* Die write/flush Befehle hier sind zwar doppelt, sind aber notwendig. Ohne diese Befehle,
                 * kann die while-Schleife nicht lesen, was im reader drin steht. Wahrscheinlich, weil sich
                 * der Prozess beendet, bevor die while-Schleife beginnt, da er nichts zu schreiben hat.
                 */

                line = "empty"; //failsafe. Wenn line=null ist, beginnt die while-Schleife nicht

                int timeInt = Integer.parseInt(time);
                int maxIterations = Integer.parseInt(time); //failsafe für infinite loop

                while ((line = reader.readLine()) != null && maxIterations > 0) {
                    if (timeInt == maxIterations) {
                        outputStream.write(command.getBytes());
                        outputStream.flush();
                    }
                    if (line.contains("bestmove")) {
                        String[] parts = line.split(" ");
                        bestMove = parts[1];
                        return bestMove;
                    } else if (line.contains(" pv ")) {
                        String[] parts = line.split(" ");
                        int index = Arrays.asList(parts).indexOf("pv") + 1;
                        if (index < parts.length) {
                            bestMove = parts[index];
                        }
                    }
                    maxIterations--; //failsafe für infinite loop
                }

                /*
                 * Stockfish gibt für diesen command (go) oft einen bestmove in der letzten Zeile aus.
                 * Da die letzte Zeile aber nicht immer das Ende der Berechnung ist, da für die while-Schleife
                 * ein Failsafe hinterlegt ist, der verhindert, dass die Schleife ewig läuft, kann es sein,
                 * dass in der letzten Zeile, die vor Abbruch ausgegeben wird, kein Schlüsselwort "bestmove" ist.
                 * Um trotzdem ein Ergebnis zurückgeben zu können, wird aus jeder Zeile der Zug, der mit dem
                 * Schlüsselwort "pv" (Principal Variation) markiert ist, extrahiert und in der Variable zwischengespeichert.
                 */

                if (bestMove != null) {
                    return bestMove;
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return "Error executing Stockfish command\n" +
                            "exitCode: " + exitCode + "\n" +
                            "Stockfish line: " + line + "\n" +
                            "command: " + command;
                }

                process.destroy();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error executing Stockfish command\n" +
                    "IOException: " + e +
                    "\n" + command;
        }

        return bestMove;
    }
}