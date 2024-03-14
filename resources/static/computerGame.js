function initialize () {
    callBackendForGameData().then (game => {
    if (game === null) { //wenn schon Matt oder Patt ist
         window.location.href = '/home';
    }

    stopTimer(parseInt(game.timer/1000));
    startTimer(parseInt(game.timer/1000));
    createChessboard (game.clickedCol, game.clickedRow, convertFenToString(game.stellung));
})}


document.addEventListener('DOMContentLoaded', function() {
    initialize();
});

function createChessboard (clickedCol, clickedRow, stellung) {

    const chessboard = document.getElementById('chessboard');
    let  fieldCounter=0;
    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement('div');
            square.className = (row + col) % 2 === 0 ? 'square white' : 'square brown';
            if((row===clickedRow) && (col===clickedCol)){
                square.className='square clicked'
            }
            const actPiece = stellung.charAt(fieldCounter).toString();
            fieldCounter++;

            if (actPiece !== " ") {

                const image = document.createElement('img');
                image.src = getPieceImage(actPiece);
                image.width = 50;
                image.height = 50;

                square.appendChild(image);
            }

            square.addEventListener('click', () => feldAngeklickt(col, row));

            //square.appendChild(figuren[row][col]);
            chessboard.appendChild(square);

        }

    }

}

async function feldAngeklickt (col, row) {

    console.log (`Clicked on square at col${col}, row ${row}`);
    //speicherung in datenbank
    await fetch(`/${col}/${row}/computerGameClick`)
        .then(response => response.text())
        .then(ende => {
            if(ende === 'true'){
                window.location.href = '/home'; //oder zu endseite
            }
            if(ende === 'promotion'){
                window.location.href = 'computerGamePromotion'; //ändern
            }
            else{ //'false'
                window.location.href = '/computerGame';
            }
        });
}

function getPieceImage (piece) {
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
    return fetch('/getComputerGameData')
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
            const  emptySquares = parseInt(currentChar, 10);
            for (let j = 0; j < emptySquares; j++) {
                result += ' '; // Leeres Feld hinzufügen
            }
        } else {
            result += currentChar; // Schachfigur hinzufügen
        }
    }

    return result;
}

function stopTimer(durationInSeconds){//Timer wird bei aktueller Zeit angehalten
    const  countdownElement = document.getElementById("timer");
    const minutes = Math.floor(durationInSeconds / 60);
    const seconds = durationInSeconds % 60;
    if(seconds>9){

        countdownElement.textContent = `Restzeit ${minutes}:${seconds}`;
    }
    else{
        countdownElement.textContent = `Restzeit ${minutes}:0${seconds}`;
    }
}

function startTimer(durationInSeconds) {
    let timer = durationInSeconds;
    const countdownElement = document.getElementById("timer");
    const updateTimer = setInterval(function () {
        const minutes = Math.floor(timer / 60);
        const seconds = timer % 60;

        if(seconds>9){
            countdownElement.textContent = `Restzeit ${minutes}:${seconds}`;
        }
        else{
            countdownElement.textContent = `Restzeit ${minutes}:0${seconds}`;
        }

        if (--timer < 0) {
            try {
                    fetch('/timerAbgelaufen', {
                        method: 'POST',
                        // kein expliziter "headers"-Abschnitt
                    });
                    window.location.href = '/home';
                } catch (error) {
                    window.location.href = '/home';
                }
        }
    }, 1000); // Die Funktion wird alle 1000 Millisekunden aufgerufen
}