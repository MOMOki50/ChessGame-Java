
//soll das fenster zu chessboard geändert werden?
function partieBeitreten() {

    fetch('/spielBeitreten')
            .then(response =>response.json())
            .then(starten => {
                if (starten==true) {
                    window.location.href = '/play';
                }
});

}

document.addEventListener('DOMContentLoaded', function() {
    partieBeitreten();
});