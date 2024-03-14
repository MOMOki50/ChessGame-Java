function initiate(){
    callBackendForGameData().then(game => {
    document.getElementById('spielerSchwarz').textContent = game.spielerSchwarz;
    document.getElementById('Partiename').textContent = game.partiename;
    document.getElementById('zug').textContent = 0;
    const history=game.history;
    createChessboard(convertFenToString(history[0]));
    document.getElementById('spielerWeiß').textContent = game.spielerWeiß;
    });
}

initiate();

function createChessboard(stellung) {

    const chessboard = document.getElementById('chessboard');
    chessboard.innerHTML = '';
    let fieldCounter=0;
    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement('div');
            square.className = (row + col) % 2 === 0 ? 'square white' : 'square brown';


            //const actPiece = figuren[row][col];
            const actPiece = stellung.charAt(fieldCounter).toString();
            fieldCounter++;

            if (actPiece !== " ") {

                const image = document.createElement('img');
                image.src = getPieceImage(actPiece);
                image.width = 50;
                image.height = 50;

                square.appendChild(image);
            }


            //square.appendChild(figuren[row][col]);
            chessboard.appendChild(square);

        }
    }
}



function getPieceImage(piece) {
    switch (piece) {
        case 'r': return './images/schachfiguren/black_rook.png';
        case 'n': return './images/schachfiguren/black_knight.png';
        case 'b': return './images/schachfiguren/black_bishop.png';
        case 'q': return './images/schachfiguren/black_queen.png';
        case 'k': return './images/schachfiguren/black_king.png';
        case 'p': return './images/schachfiguren/black_pawn.png';
        case 'R': return './images/schachfiguren/white_rook.png';
        case 'N': return './images/schachfiguren/white_knight.png';
        case 'B': return './images/schachfiguren/white_bishop.png';
        case 'Q': return './images/schachfiguren/white_queen.png';
        case 'K': return './images/schachfiguren/white_king.png';
        case 'P': return './images/schachfiguren/white_pawn.png';
        default: return ''; // Kein Bild für leere Felder, kommt im moment eh nicht vor, da abfrage whichPiece !== ""
    }
}






function callBackendForGameData() {
    const id=document.getElementById('id').innerText;
    return fetch('/' + id + '/getFinishedGameData')
            .then(response => response.json())
            .catch(error => {
                window.location.href = '/home'; //wenn Partie schon beendet ist (Game Objekt wurde dann gelöscht)
            });
}

function convertFenToString(fen) {
    let result = '';

    // Aufteilen der FEN-Notation in ihre Komponenten
    const fenParts = fen.split(' ');
    const piecePlacement = fenParts[0];

    // Iterieren durch jedes Zeichen in der Stellung
    for (let i = 0; i < piecePlacement.length; i++) {
        let currentChar = piecePlacement.charAt(i);

        if(currentChar=='/'){
            i++;
            currentChar = piecePlacement.charAt(i);
        }

        if(currentChar==' '){
                    break;
                }



        // Überprüfen, ob es sich um eine Ziffer handelt (Leere Felder)
        if (/\d/.test(currentChar)) {
            const emptySquares = parseInt(currentChar, 10);
            for (let j = 0; j < emptySquares; j++) {
                result += ' '; // Leeres Feld hinzufügen
            }
        } else {
            result += currentChar; // Schachfigur hinzufügen
        }
    }

    return result;
}

function next(){
    callBackendForGameData().then(game => {
    let zug = parseInt(document.getElementById('zug').textContent)+1;
    console.log(game.history.length);
    console.log(zug);
    if(zug<game.history.length){
        document.getElementById('zug').textContent=zug;
        console.log(game.history[zug]);
        createChessboard(convertFenToString(game.history[zug]));
    }
    });
}

function bevore(){
    callBackendForGameData().then(game => {
        let zug = parseInt(document.getElementById('zug').textContent)-1;
        console.log(game.history.length);
        console.log(zug);
        if(zug>=0){
            document.getElementById('zug').textContent=zug;
            console.log(game.history[zug]);
            createChessboard(convertFenToString(game.history[zug]));
        }
        });
}