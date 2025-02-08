let stompClient = null;
let heartbeatInterval = null;

async function connect() {
    let wsProtocol = 'ws://';
    if (window.location.protocol === 'https:') wsProtocol = 'wss://';
    const socket = new WebSocket(`${wsProtocol}${window.location.host}/ws`);
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
    });

    stompClient.onConnect = async () => {
        const chatId = new URLSearchParams(window.location.search).get('id');

        stompClient.subscribe(`/topic/chat/${chatId}/online`, (response) => {
            const usersStatus = JSON.parse(response.body);
            updateUsersList(usersStatus); // Update the user list dynamically
        });

        stompClient.subscribe(`/topic/chat/${chatId}/message`, (response) => {
            const message = JSON.parse(response.body);
            receiveMessage(message);
        })

        try {
            const username = await fetch('/api/current-user').then((response) => response.text());

            stompClient.publish({
                destination: '/app/chat/online',
                body: JSON.stringify({ chatId, username }),
            });

            startHeartbeat(chatId, username);
        } catch (err) {
            console.error('Error fetching current user:', err);
        }
    };

    stompClient.onStompError = (error) => {
        console.error('STOMP error:', error);
    };

    stompClient.onDisconnect = async () => {
        clearHeartbeat();
        await sendOfflineSignal();
    };

    stompClient.activate();
}

async function disconnect() {
    if (stompClient) {
        clearHeartbeat();
        await sendOfflineSignal();
        await stompClient.deactivate();
        console.log('Disconnected from WebSocket');
    }
}

function startHeartbeat(chatId, username) {
    stompClient.publish({
        destination: '/app/chat/heartbeat',
        body: JSON.stringify({ chatId, username }),
    });
    heartbeatInterval = setInterval(() => {
        stompClient.publish({
            destination: '/app/chat/heartbeat',
            body: JSON.stringify({ chatId, username }),
        });
    }, 10000);
}

function clearHeartbeat() {
    if (heartbeatInterval) {
        clearInterval(heartbeatInterval); // Stop sending heartbeats
        heartbeatInterval = null;
    }
}

async function sendOfflineSignal() {
    const chatId = new URLSearchParams(window.location.search).get('id');
    try {
        const username = await fetch('/api/current-user').then((response) => response.text());
        if (chatId && username) {
            stompClient.publish({
                destination: '/app/chat/offline',
                body: JSON.stringify({ chatId, username }),
            });
        }
    } catch (err) {
        console.error('Error sending offline signal:', err);
    }
}

function updateUsersList(usersStatus) {
    const usersListElement = document.getElementById('online-users');
    if (!usersListElement) {
        console.error('HTML element with ID "users-list" not found.');
        return;
    }

    usersListElement.innerHTML = '';

    for (const [username, isOnline] of Object.entries(usersStatus)) {
        const userElement = document.createElement('div');
        userElement.textContent = username;

        if (!isOnline) {
            userElement.classList.add('offline-user');
        }

        userElement.classList.add('user');

        usersListElement.appendChild(userElement);
    }
}

async function sendMessage() {
    const chatId = new URLSearchParams(window.location.search).get('id');
    const message = document.getElementById('message-input').value;
    if (!message) {
        console.error('Message is empty.');
        return;
    }

    try {
        const username = await fetch('/api/current-user').then((response) => response.text());
        stompClient.publish({
            destination: `/app/chat/message`,
            body: JSON.stringify({
                chatId: chatId,
                message: message,
                author: username,
                timestamp: Date.now(),
            }),
        });
    } catch (err) {
        console.error('Error fetching username:', err);
    }

    document.getElementById('message-input').value = ''; // Clear the input field
}

function receiveMessage(message) {
    const messagesElement = document.getElementById('chat-messages');

    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message');

    const timestampSpan = document.createElement('span');
    timestampSpan.classList.add('time');
    timestampSpan.textContent = new Date(message.timestamp).toLocaleTimeString() + ' | ';
    messageDiv.appendChild(timestampSpan);

    const authorSpan = document.createElement('span');
    authorSpan.textContent = message.author + ':';
    messageDiv.appendChild(authorSpan);

    messageDiv.appendChild(document.createElement('br'));

    const messageSpan = document.createElement('span');
    messageSpan.classList.add('message-text');
    messageSpan.textContent = message.message;
    messageDiv.appendChild(messageSpan);

    messagesElement.appendChild(messageDiv);

    messagesElement.scrollTop = messagesElement.scrollHeight;
}

document.getElementById('message-input').addEventListener('keypress', (event) => {
    if (event.key === 'Enter') {
        sendMessage();
    }
});

window.addEventListener('load', () => {
    connect();
    const inputElement = document.getElementById('message-input');
    inputElement.focus();
    const messagesElement = document.getElementById('chat-messages');
    messagesElement.scrollTop = messagesElement.scrollHeight;
});

window.addEventListener('beforeunload', async () => {
    await disconnect();
});

