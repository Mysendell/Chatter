const chatId = new URLSearchParams(window.location.search).get('id');

async function leaveChat(){
    const username = await fetch('/api/current-user').then((response) => response.text());
    if (confirm("Are you sure you want to leave the chat?")) {
        try {
            const response = await fetch("/api/leave-chat?chatId=" + chatId + "&username=" + username);
            if (!response.ok) {
                throw new Error('Failed to call /api/leave-chat: ' + response.statusText);
            }
            window.location.href = '/home';
        } catch (error) {
            console.error('Error leaving chat:', error);
        }

        window.location.href = '/home';
    }
}

async function addUser(){
    const username = document.getElementById("user-input").value;
    const author = await fetch('/api/current-user').then((response) => response.text());
    try {
        const response = await fetch("/api/add-user?chatId=" + chatId + "&username=" + username + "&author=" + author);
        if (!response.ok) {
            throw new Error('Failed to call /api/add-user: ' + response.statusText);
        }
        window.location.href = '/home';
    } catch (error) {
        console.error('Error adding to chat:', error);
    }
}

async function removeUser(){
    const username = document.getElementById("user-input").value;
    const author = await fetch('/api/current-user').then((response) => response.text());
    try {
        const response = await fetch("/api/remove-user?chatId=" + chatId + "&username=" + username + "&author=" + author);
        if (!response.ok) {
            throw new Error('Failed to call /api/add-user: ' + response.statusText);
        }
        window.location.href = '/home';
    } catch (error) {
        console.error('Error adding to chat:', error);
    }
}
