function removeFriendRequest(button, accepted) {
    const friendRequest = button.closest('.friend-request');
    friendRequest.classList.add('removed');

    setTimeout(() => {
        friendRequest.remove();
    }, 300); // Nach 500 Millisekunden wird das Element entfernt

    // Hier kannst du die entsprechende Aktion ausführen, wenn die Anfrage angenommen oder abgelehnt wird.
}