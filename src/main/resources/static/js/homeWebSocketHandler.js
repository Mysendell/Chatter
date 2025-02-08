let stompClient = null;
let heartbeatInterval = null;

async function connect() {
    const chatId = 0;
    let wsProtocol = 'ws://';
    if (window.location.protocol === 'https:') wsProtocol = 'wss://';
    const socket = new WebSocket(`${wsProtocol}${window.location.host}/ws`);
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
    });

    stompClient.onConnect = async () => {
        stompClient.subscribe(`/topic/online`, (response) => {
            const usersStatus = JSON.parse(response.body);
            updateUsersList(usersStatus);
        });
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
    const usersListElement = document.getElementById('onlineUsers');

    usersListElement.innerHTML = '';

    for(const users of Object.values(usersStatus)) {
    for (const [username, isOnline] of Object.entries(users)) {
        if (!isOnline) {
            continue;
        }
        const userElement = document.createElement('div');
        userElement.textContent = username;
        userElement.classList.add('user');

        usersListElement.appendChild(userElement);
    }
    }
}

window.addEventListener('load', connect);

window.addEventListener('beforeunload', async () => {
    await disconnect();
});