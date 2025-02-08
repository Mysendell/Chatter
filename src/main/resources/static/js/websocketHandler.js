let stompClient = null; // Global STOMP client

async function connect() {
    const socket = new WebSocket('ws://localhost:8080/ws'); // Replace 'your-server-url' with the appropriate WebSocket URL
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        //debug: (message) => console.log(message),
    });

    stompClient.onConnect = async () => {

        const chatId = new URLSearchParams(window.location.search).get('id');
        if (!chatId) {
            console.error('Chat ID is missing in the URL query parameters.');
            return;
        }

        socket.onerror = (error) => {
            console.error('WebSocket error:', error);
        };


        stompClient.subscribe(`/topic/chat/${chatId}/online`, (message) => {
            const users = JSON.parse(message.body);
            updateOnlineUsers(users);
        });

        try {
            const username = await fetch('/api/current-user').then((response) => response.text());
            stompClient.publish({
                destination: '/app/chat/online',
                body: JSON.stringify({chatId, username}),
            });
        } catch (err) {
            console.error('Error fetching current user:', err);
        }
    };

    stompClient.onStompError = (error) => {
        console.error('STOMP error:', error);
    };

    stompClient.activate();
}

async function disconnect() {
    if (stompClient) {
        const chatId = new URLSearchParams(window.location.search).get('id');
        try {
            const username = await fetch('/api/current-user').then((response) => response.text());
            if (chatId && username) {
                stompClient.publish({
                    destination: '/app/chat/offline',
                    body: JSON.stringify({chatId, username}),
                });
            }
        } catch (err) {
            console.error('Error fetching current user before disconnecting:', err);
        }
        await stompClient.deactivate();
        console.log('Disconnected from WebSocket');
    }
}

function updateOnlineUsers(users) {
    const onlineUsersElement = document.getElementById('online-users');
    if (!onlineUsersElement) {
        console.error('HTML element with ID "online-users" not found.');
        return;
    }

    onlineUsersElement.innerHTML = '';

    users.forEach((user) => {
        const div = document.createElement('div');
        div.textContent = user;
        div.classList.add('online-user');
        onlineUsersElement.appendChild(div);
    });
}

window.addEventListener('load', connect);
window.addEventListener('beforeunload', disconnect);