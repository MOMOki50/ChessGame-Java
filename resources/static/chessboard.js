function initialize() {
    callBackendForGameData().then(game => {
    if (game === null) { //wenn schon Matt oder Patt ist
         window.location.href = '/home';
    }
    //const game = document.getElementById('game');
    const currentUser = document.getElementById('currentUserEmail').value;
    const amZug = istCurrentUserAmZug(currentUser, game.weißAmZug, game.spielerWeiß, game.spielerSchwarz);
    console.log('amZug:', amZug);
    console.log(currentUser);
    //const nameWhite=  vor- und nachname statt mail
    //const nameBlack=
    zeitWeiß=parseInt(game.timerWeiß / 1000);//in Sekunden und ohne Nachkommastellen
    zeitSchwarz=parseInt(game.timerSchwarz / 1000);
    stopTimerBlack(zeitSchwarz, game.spielerSchwarz);//damit das Bild nicht springt jetzt schon angegeben
    stopTimerWhite(zeitWeiß, game.spielerWeiß);//damit das Bild nicht springt
    if(game.zeitpunkt!=null){//Zeit soll erst laufen, wenn weiß beigetreten ist
    if(game.weißAmZug){
    startTimerWhite(zeitWeiß, game.spielerWeiß);
    }
    else{
    startTimerBlack(zeitSchwarz, game.spielerSchwarz);
    }}
    createChessboard(game.clickedCol, game.clickedRow, amZug, convertFenToString(game.stellung), game.col1, game.col2, game.row1, game.row2);
    partiename(game.partiename);
    if(!amZug){
    aufZugWarten(currentUser)}//als kommentar bei Performance-Problemen
    if(amZug){
        let bestMove = document.getElementById('bestMove');
        bestMove.addEventListener('click', () => doBestMove(game.stellung));}
    });
}

function aufZugWarten(currentUser){
    setTimeout(function () {
    callBackendForGameData().then(game => {
        if (game === null) { //wenn schon Matt oder Patt ist
                 window.location.href = '/home';
        }
        const amZug = istCurrentUserAmZug(currentUser, game.weißAmZug, game.spielerWeiß, game.spielerSchwarz);
        if (amZug) {
            window.location.href = '/play';
            } else {
                aufZugWarten(currentUser);
                }});
    },1000);//1 Sekunde Verzögerung
    //verweis auf endbildschirm, wenn Partie vorbei
}

function istCurrentUserAmZug(currentUser, weißAmZug, spielerWeiß, spielerSchwarz){
    if(weißAmZug){
        return currentUser === spielerWeiß;
    }
    return currentUser === spielerSchwarz;
}

document.addEventListener('DOMContentLoaded', function() {
    initialize();
});


function partiename(name){
    const partienameElement = document.getElementById('Partiename');
    partienameElement.textContent = name;
}


function createChessboard(clickedCol, clickedRow, amZug, stellung, col1, col2, row1, row2) {

    /*const anfangsFiguren=[
        ["r", "n", "b", "q", "k", "b", "n", "r"],
        ["p", "p", "p", "p", "p", "p", "p", "p"],
        [" ", " ", " ", " ", " ", " ", " ", " "],
        [" ", " ", " ", " ", " ", " ", " ", " "],
        [" ", " ", " ", " ", " ", " ", " ", " "],
        [" ", " ", " ", " ", " ", " ", " ", " "],
        ["P", "P", "P", "P", "P", "P", "P", "P"],
        ["R", "N", "B", "Q", "K", "B", "N", "R"]
    ];*/
    const chessboard = document.getElementById('chessboard');
    let fieldCounter=0;
    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement('div');
            square.className = (row + col) % 2 === 0 ? 'square white' : 'square brown';
            if(amZug && (row===row1) && (col===col1)){
                square.className='square hellblau'
            }
            if(amZug && (row===row2) && (col===col2)){
                square.className='square dunkelblau'
            }
            if(amZug===true && (row===clickedRow) && (col===clickedCol)){
                square.className='square clicked'
            }
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

            //eventListener bei Anklicken der "Figur" (also des Squares)
            if(amZug === true){
                square.addEventListener('click', () => feldAngeklickt(col, row));
            }
            //square.appendChild(figuren[row][col]);
            chessboard.appendChild(square);

        }

    }

}

async function feldAngeklickt(col, row) {
    console.log(`Clicked on square at col${col}, row ${row}`);
    //speicherung in datenbank
    await fetch(`/${col}/${row}/saveClick`)
        .then(response => response.text())
        .then(ende => {
            if(ende === 'true'){
                window.location.href = '/home'; //oder zu endseite
            }
            if(ende === 'promotion'){
                window.location.href = 'promotion'; //ändern
            }
            else{ //'false'
                window.location.href = '/play';
            }
        });
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


function startTimer(durationInSeconds, elementFarbe, player) {
    let timer = durationInSeconds;
    const countdownElement = document.getElementById(elementFarbe);
    const updateTimer = setInterval(function () {
        const minutes = Math.floor(timer / 60);
        const seconds = timer % 60;

        if(seconds>9){
            countdownElement.textContent = `Restzeit  ${player}: ${minutes}:${seconds}`;
        }
        else{
            countdownElement.textContent = `Restzeit  ${player}: ${minutes}:0${seconds}`;
        }

        if (--timer < 0) {
            clearInterval(updateTimer);
            countdownElement.textContent = "Countdown abgelaufen!";
        }
    }, 1000); // Die Funktion wird alle 1000 Millisekunden aufgerufen
}

function stopTimer(durationInSeconds, elementFarbe, player){//Timer wird bei aktueller Zeit angehalten
    const countdownElement = document.getElementById(elementFarbe);
    const minutes = Math.floor(durationInSeconds / 60);
    const seconds = durationInSeconds % 60;
    if(seconds>9){
        countdownElement.textContent = `Restzeit  ${player}: ${minutes}:${seconds}`;
    }
    else{
        countdownElement.textContent = `Restzeit  ${player}: ${minutes}:0${seconds}`;
    }
}

function startTimerBlack(durationInSeconds, player){
    startTimer(durationInSeconds, 'countdown-black', player)
}

function startTimerWhite(durationInSeconds, player){
    startTimer(durationInSeconds, 'countdown-white', player)
}

function stopTimerBlack(durationInSeconds, player){
    stopTimer(durationInSeconds, 'countdown-black', player)
}

function stopTimerWhite(durationInSeconds, player){
    stopTimer(durationInSeconds, 'countdown-white', player)
}

function callBackendForGameData() {
    return fetch('/getGameData')
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

function giveUp() {
    try {
        fetch('/aufgeben', {
            method: 'POST',
            // kein expliziter "headers"-Abschnitt
        });
        window.location.href = '/home';
    } catch (error) {
        window.location.href = '/home';
    }

}

function doBestMove(fenPosition) {
    console.log("found this FEN: " + fenPosition);
    fetch(`/chess/doMyBestMove?fenPosition=${fenPosition}`, {
        method: 'POST',
    })
        .then(response => {
            if (response.ok) {//wenn die Anfrage erfolgreich war
                return response.text(); // Rückgabe als Text verarbeiten
            } else {
                alert("Anfrage fehlgeschlagen.");
                throw new Error('Anfrage fehlgeschlagen.');
            }
        })
        .then(responseText => { //Text verarbeiten
            console.log(responseText); //Text ausgeben für Troubleshooting
                let col1 = responseText.charAt(0);
                let row1 = responseText.charAt(1);
                let col2 = responseText.charAt(2);
                let row2 = responseText.charAt(3);
                alert("Bewege die Figur von " + col1 + row1 + " nach " + col2 + row2);
                window.location.href = '/play';
        })
        .catch(error => {
            console.error('Error:', error);
            alert("Konnte keinen Zug berechnen.");
        });
}
