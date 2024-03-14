let puzzleCount = 0;
// globale Variable
let importedPuzzles = [];

function importPuzzles() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    const errorMessageElement = document.getElementById('errorMessage');
    const successMessageElement = document.getElementById('successMessage');

    const formData = new FormData();
    formData.append('file', file);

    fetch("/import", {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Fehler beim Importieren der Schachpuzzles.');
            }
            return response.text();
        })
        .then(csvData => {
            console.log('Serverantwort (CSV):', csvData);


            // CSV in ein Array von Zeilen aufteilen
            const csvRows = csvData.split('\n');

            // Brauche ich nicht
            const csvValues = csvRows.map(row => row.split(','));

            //  Speicher die importierten Puzzles als globale Variable
            importedPuzzles = csvValues;

            successMessageElement.textContent = 'Schachpuzzles erfolgreich importiert.';
            window.location.href = '/ChessPuzzle';
        })
        .catch(error => {
            console.error('Error:', error);

            errorMessageElement.textContent = 'Fehler beim Importieren der Schachpuzzles.';
        });
}



function showPuzzles() {
    const menu = document.getElementById('menu');
    const puzzlePage = document.getElementById('puzzlePage');
    menu.style.display = 'none';
    puzzlePage.style.display = 'block';

    fetch('/ChessPuzzle')
        .then(response => {
            if (!response.ok) {
                throw new Error('Fehler beim Laden der Schachpuzzles.');
            }
            return response.json();
        })
        .then(puzzles =>  {
            displayPuzzles(puzzles);
        })
        .catch(error => {
            console.error('Fehler beim Laden der Schachpuzzles:', error);
            errorMessageElement.textContent = 'Fehler beim Laden der Schachpuzzles.';
        });
}
