function initiate(){
    callBackendForGameData().then(game => {

        console.log(game);
        //Timer können eingefügt werden
            document.getElementById('Partiename').textContent = game.partiename;
            zeitWeiß = parseInt(game.timerWeiß / 1000); //in Sekunden und ohne Nachkommastellen
            zeitSchwarz = parseInt(game.timerSchwarz / 1000);
            stopTimerBlack(zeitSchwarz, game.spielerSchwarz); //damit das Bild nicht springt jetzt schon angegeben
            stopTimerWhite(zeitWeiß, game.spielerWeiß); //damit das Bild nicht springt
            if (game.zeitpunkt != null) { //Zeit soll erst laufen, wenn weiß beigetreten ist
                if (game.weißAmZug) {
                    startTimerWhite(zeitWeiß, game.spielerWeiß);
                } else {
                    startTimerBlack(zeitSchwarz, game.spielerSchwarz);
                }
            }
            createChessboard(convertFenToString(game.stellung));
            getStellung(game.stellung);
    }).catch(error => {
        window.location.href = '/home'; //wenn Partie schon beendet ist (Game Objekt wurde dann gelöscht)
    })
}

initiate();

function getStellung(fen){
    setTimeout(function () {
        callBackendForGameData().then(game => {
            if (game === null) { //wenn schon Matt oder Patt ist
                window.location.href = '/streaming';
            }
            if (fen != game.stellung) {
                console.log("reload!");
                location.reload();
            } else {
                getStellung(fen);
                console.log("getStellung!");
            }});
    },1000);//1 Sekunde Verzögerung
    //verweis auf endbildschirm, wenn Partie vorbei
}

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
    const id= document.getElementById('spielid').innerText;
    return fetch('/' + id + '/getLiveGameData')
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
    startTimer(durationInSeconds, 'spielerSchwarz', player)
}

function startTimerWhite(durationInSeconds, player){
    startTimer(durationInSeconds, 'spielerWeiß', player)
}

function stopTimerBlack(durationInSeconds, player){
    stopTimer(durationInSeconds, 'spielerSchwarz', player)
}

function stopTimerWhite(durationInSeconds, player){
    stopTimer(durationInSeconds, 'spielerWeiß', player)
}