const chatResultsElement = document.getElementById("chats");
const chatSearchInputElement = document.getElementById("chat-search-input");
const userSearchInputElement = document.getElementById("user-search-input");

function fetchChats(page = 0, chat="", userList=[], size=5) {
    fetch(`/api/chats?page=${page}&size=${size}&chat=${chat}&userList=${userList}`)
        .then(response => response.json())
        .then(data => displayChats(data.content, data.number, data.totalPages)); // Pass pagination data
}

function displayChats(chatData, currentPage, totalPages) {
    chatResultsElement.innerHTML = "";
    if(chatData.length === 0){
        chatResultsElement.innerHTML = "<p>No chats found. Start one today!</p>";
        return;
    }
    let resultsHTML = `
    <table>
        <thead>
            <tr>
                <th>Name</th>
                <th>Members</th>
            </tr>
        </thead>
        <tbody>
    `;

    for (const chat of chatData) {
        resultsHTML += `
        <tr>
            <td><a href="/chat?id=${chat.id}">${chat.name}</a></td>
            <td>${chat.usersString}</td>
        </tr>
        `;
        initializeNotifications(chat.id)
    }

    resultsHTML += `
        </tbody>
    </table>
    `;

    resultsHTML += "<div id='pagination-controls'>";

    if (currentPage > 0) {
        resultsHTML += `<button onclick="changeChatPage(${currentPage - 1})">Previous</button>`;
    }

    resultsHTML += `<span> Page ${currentPage + 1} of ${totalPages} </span>`;

    if (currentPage < totalPages - 1) {
        resultsHTML += `<button onclick="changeChatPage(${currentPage + 1})">Next</button>`;
    }

    resultsHTML += "</div>";

    chatResultsElement.innerHTML = resultsHTML;
}

async function changeChatPage(newPage=0) {
    const username = await fetch('/api/current-user').then((response) => response.text());
    const chatInputElement = chatSearchInputElement.value;
    const userSearchInputElement = document.getElementById("user-search-input");
    let userList = [];
    if(userSearchInputElement.value !== "")
        userList = userSearchInputElement.value.split(",").map(user => user.trim());
    if(!userList.includes(username))
        userList.push(username);
    fetchChats(newPage, chatInputElement, userList);
}

async function initializeNotifications(chatId){
    const username = await fetch('/api/current-user').then((response) => response.text());
    notifications = await fetch(`/api/notifications?username=${username}&chatId=${chatId}`).then((response) => response.json());
    for(const notification of notifications){
        displayNotification(notification);
    }
}

window.addEventListener('load', async () => {
    changeChatPage();
})