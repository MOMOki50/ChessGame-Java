import { Client } from '@stomp/stompjs';

const userId = Number(document.getElementById('userId').value);
const chatId = Number(document.getElementById('chatId').value);
const username = document.getElementById("username").value;

const client = new Client({
    brokerURL: 'ws://localhost:8080/chat',
});

window.onload = () => {
    // Initialisiere und öffne die WebSocket-Verbindung
    const chatId = document.getElementById('chatId').value;
    // Hier deine Logik, um die Chat-ID zu bestimmen
    console.log(client);
    connectToWebSocket(chatId);
    markChatAsRead(chatId, userId);
};

function connectToWebSocket(chatId) {
    client.onConnect = function(frame) {
        // Abonniert einen bestimmten Chat-Kanal
        console.log("Connected: " + frame);
        console.log("ChatID: " + chatId)
        client.subscribe(`/topic/chat/messages/${chatId}`, (message) => {
            // Verarbeitet die empfangenen Nachrichten
            console.log(message);
            console.log(message.body);
            const messageBody = JSON.parse(message.body);
            displayMessage(message.id,messageBody); //messageId muss mit übergeben werden, weil die im messageBody alleine nicht drin ist, und daher aus messageBody alleine nicht extrahiert werden kann
            console.log(messageBody)
        });

    };
    client.activate();
}

client.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

client.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function displayMessage(messageId, messageBody) {
    const chatMessagesDiv = document.getElementById("chat-messages");
    const messageElement = document.createElement('div');
    messageElement.classList.add("message");


    if(messageBody.userID == userId) {
        messageElement.classList.add("myMessage");
        console.log('marked myMessage: ' + messageId);
    } else {
        messageElement.classList.add("othersMessage");
        updateMessageReadStatus(messageId);
        console.log(messageId);
    }

    const messageText = document.createElement('p');
    messageText.innerText = `${messageBody.username}: ${messageBody.messageContent}`;
    messageElement.appendChild(messageText);
    chatMessagesDiv.appendChild(messageElement);
    window.location.reload();
}

document.querySelector('.input-form').addEventListener('submit',  (event) => {
    event.preventDefault(); // Verhindert das Neuladen der Seite

    const messageInput = document.querySelector('.user-input');

    if (messageInput.value.trim() !== '') {
        const message = {
            userID: userId,
            messageContent: messageInput.value.trim(),
            username: username,
        };

        // Sende die Nachricht über WebSocket
        client.publish({
            destination: `/app/chat/sendMessage/${chatId}`,
            body: JSON.stringify(message)
        });
        messageInput.value = ''; // Setze das Eingabefeld zurück
    }
});

function markChatAsRead(chatId, currentUserId) {//Fetch Request, um Chat als gelesen zu markieren
    fetch(`/Chat/${chatId}/markChatAsRead?currentUserId=${currentUserId}`, {
        method: 'POST'
    })
            .then(response => {
                // Handle the response as needed
                console.log('POST request successful');
            })
            .catch(error => {
                // Handle any errors
                console.error('Error performing POST request:', error);
            });
}

function updateMessageReadStatus(messageId) {//Fetch Request, um einzelne Nachricht als gelesen zu markieren
    fetch(`/markAsRead/${chatId}/${messageId}`, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to update message read status');
            }
        })
        .catch(error => {
            console.error('Error updating message read status:', error);
    });
    window.location.reload();
}

function editMessage(messageId) {//
    console.log('accessing editMessage with messageId' + messageId);
    let newContent = document.querySelector('#editInput' + messageId).value;
    fetch(`/editMessage/${chatId}/${messageId}?newContent=${newContent}`, {
        method: 'POST'//sendet Anfrage an Server, an spezifizierten Endpunkt, übergibt die ${} Parameter
    })
        .then(response => {
            if (response.ok) {//wenn die Anfrage erfolgreich war
                return response.text();
            } else {
                throw new Error('Nachricht bearbeiten fehlgeschlagen');
            }
        })
        .then(data => {//verarbeitet die zurückgebenenen Daten
            if (data.trim() === "true") {//verarbeitet die zurückgebenen Daten, entfernt leeren Raum und prüft, ob "true" zurückgegeben wurde
                alert('Nachricht erfolgreich bearbeitet');
            } else if (data.trim() === "false") {
                alert('Nachricht konnte nicht bearbeitet werden, weil sie in der Zwischenzeit als gelesen markiert wurde');
            } else {//wenn data.trim() weder "true" noch "false" ist, wird dieser Fehler angezeigt, sollte nicht vorkommen
                throw new Error('Server Fehler, konnte Nachricht nicht bearbeiten');
            }
        })
        .catch(error => {//falls die Verarbeitung oben Fehler produziert
            console.error('Konnte Nachricht nicht bearbeiten, Fehler:', error);
            alert('konnte Nachricht nicht bearbeiten'); // Alert für Fehler
        });
    window.location.reload();
}

const popupButtons = document.querySelectorAll(".popupButton");
// Add event listener to each button
popupButtons.forEach((button) => {
    button.addEventListener("click", () => {
        let messageId = button.closest(".message").getAttribute("data-message-id"); //extrahiert die Id der Nachricht aus dem Element, das am nächsten zum Button ist
        console.log('popup button for messageId '+ messageId);
        let editPopup = document.getElementById('editPopup' + messageId); //wählt EditPopup, das zur Nachricht gehört
        console.log('edit popup:' + editPopup);
        let saveEdit = document.getElementById('saveEditButton' + messageId); //wählt richtigen button für Speichern aus
        let cancelEdit = document.getElementById('cancelEditButton' + messageId);
        let messageContent = document.getElementById('messageContentInput' + messageId).value; //um immer den ursprünglichen Inhalt der Nachricht anzuzeigen, wenn popup geöffnet wird, ohne zu speichern
        console.log('msg content: ' +  messageContent);
        editPopup.classList.add("show");
        let editInput = document.querySelector('#editInput' + messageId); //wählt das Input Feld aus.
        console.log('edit input: ' + editInput);

        window.addEventListener("click", function (event) {
            if (event.target === editPopup) {
                editPopup.classList.remove("show");
                editInput.value = messageContent;
            }
        });

        window.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                editPopup.classList.remove("show");
                editInput.value = messageContent;
            }
        });

        cancelEdit.addEventListener("click", function () {
            editPopup.classList.remove("show");
            editInput.value = messageContent;
        });

        saveEdit.addEventListener("click", function () {
            editMessage(messageId);//ruft Funktion editMessage auf
            editPopup.classList.remove("show");
            window.location.reload();
            });
        })
});
