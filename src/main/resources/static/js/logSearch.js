const logtableElement = document.getElementById("logs");
const actionInput = document.getElementById("log-search-actions");
const targetInput = document.getElementById("log-search-target");
const authorInput = document.getElementById("log-search-author");

function fetchLogs(author, target, action, page = 0, size = 15) {
    fetch(`/api/logs?author=${author}&target=${target}&action=${action}&page=${page}&size=${size}`)
        .then(r => r.json())
        .then(r => makeTable(r.content, r.number, r.totalPages)); // Pass page and totalPages
}

function makeTable(tableData, currentPage, totalPages) {
    logtableElement.innerHTML = ""; // Clear any existing content

    let tableHTML = `
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Timestamp</th>
                <th>Author</th>
                <th>Action</th>
                <th>Target</th>
            </tr>
        </thead>
        <tbody>
`;

    for (const row of tableData) {
        tableHTML += `
        <tr>
            <td>${row.id}</td>
            <td>${row.timestamp}</td>
            <td>${row.author}</td>
            <td>${row.action}</td>
            <td>${row.target}</td>
        </tr>
    `;
    }

    tableHTML += `
        </tbody>
    </table>
`;

    // Add Pagination Controls
    tableHTML += "<div id='pagination-controls'>";

    if (currentPage > 0) {
        tableHTML += `<button onclick="changePage(${currentPage - 1})">Previous</button>`;
    }

    tableHTML += `<span> Page ${currentPage + 1} of ${totalPages} </span>`;

    if (currentPage < totalPages - 1) {
        tableHTML += `<button onclick="changePage(${currentPage + 1})">Next</button>`;
    }

    tableHTML += "</div>";

    logtableElement.innerHTML = tableHTML; // Insert the generated table into the element
}

function changePage(newPage=0) {
    let author = authorInput.value;
    let target = targetInput.value;
    let action = actionInput.value;
    fetchLogs(author, target, action, newPage); // Call fetchLogs with updated page
}

changePage()
