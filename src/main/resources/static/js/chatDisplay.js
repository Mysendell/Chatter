const chatResultsElement = document.getElementById("chats");

function fetchChats(username, page = 0, size = 10) {
    fetch(`/api/chats?username=${username}&page=${page}&size=${size}`)
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
    fetchChats(username, newPage);
}

changeChatPage();