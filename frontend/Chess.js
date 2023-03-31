let gameId = -1;
let alliance
let currentPlayer
let eatIndex
let OpponentEatIndex
let currentPlayerKingPosition
const loadingElement = document.getElementById('loading');
const multiplayerButton = document.getElementById("playMultiplayerButton");
const playComputerButton = document.getElementById("playComputerButton");
const buttonsContainer = document.getElementById('buttonsContainer');


function hideButtons() {
    buttonsContainer.classList.add('hidden');
    multiplayerButton.setAttribute("disabled", "disabled");
    playComputerButton.setAttribute("disabled", "disabled");
}

function showLoading() {
    loadingElement.classList.remove('hidden');
}

function hideLoading() {
    loadingElement.classList.add('hidden');
}

function coloring() {
    const tile = document.querySelectorAll('.tile')

    tile.forEach(tile => {
        tileId = tile.id
        rowId = tile.parentElement.id.charAt(1)

        if (rowId % 2 == 0) {
            if (tileId % 2 == 0) {
                tile.style.backgroundColor = 'rgb(240, 201, 150)'
            }
            if (tileId % 2 !== 0) {
                tile.style.backgroundColor = 'rgb(100,75,43)'
            }
        } else {
            if (tileId % 2 == 0) {
                tile.style.backgroundColor = 'rgb(100,75,43)'
            }
            if (tileId % 2 !== 0) {
                tile.style.backgroundColor = 'rgb(240, 201, 150)'
            }
        }


    })
}
coloring()


function insertImage(tileId, pieceName) {
    currTile = document.getElementById(tileId);
    currTile.innerHTML = `<img class='allimg' src="art/${pieceName}.gif" alt="">`
    currTile.style.cursor = 'pointer'
}


let socket
let checkedKingTile = null
function setConnection() {
    socket = new WebSocket("ws://localhost:8080/chess")

    socket.addEventListener("open", (event) => {
        console.log("Connected to WebSocket");

        // Send a message to the server
        //socket.send("Hello, server!");
    });

    const popupWindow = window.open("", "popupWindow");
    // Handle message from server
    gameHistoryTable = document.getElementById("game-history-table");
    let newRow = gameHistoryTable.insertRow(-1)
    let isInCheckmate = false;

    socket.addEventListener("message", (event) => {
        let messageType
        let arr
        let isInCheck
        let isAttackMove
        let moveDescription = null
        let tableCellIndex


        const jsonObj = JSON.parse(event.data);
        messageType = jsonObj.type
        console.log("Received message:", jsonObj);
        console.log(messageType);
        switch (messageType) {
            case "multiplayerAvailable":
                multiplayerButton.removeAttribute('disabled');
                multiplayerButton.textContent = "Multiplayer \r\n (Othen players are connected)"
                break;
            case "multiplayerDisabled":
                multiplayerButton.setAttribute("disabled", true);
                multiplayerButton.textContent = "Multiplayer \r\n (Only available when other players are connected)"
                break;
            case "setAlliance":
                hideLoading()
                hideButtons()
                alliance = jsonObj.alliance
                gameId = jsonObj.gameId
                console.log(gameId)
                if (alliance === "Black") {
                    eatIndex = 64
                    OpponentEatIndex = 80
                    console.log("alliance is black!")
                    let board = document.querySelector('ul');
                    board.classList.add('flip');
                    let eatRow = document.getElementById('r10')
                    eatRow.classList.add('flip')
                    let eatRow2 = document.getElementById('r9')
                    eatRow2.classList.add('flip')
                    document.querySelectorAll('.tile').forEach(currTile => {
                        currTile.classList.add('flip');
                    })
                } else {
                    eatIndex = 80
                    OpponentEatIndex = 64
                }
                break;
            case "boardUpdate":
                document.querySelectorAll('.tile').forEach(currTile => {
                    currTile.innerHTML = '';
                })
                try {
                    currentPlayer = jsonObj.currentPlayer
                    arr = jsonObj.boardPieceDescription.split(","); // "John"
                    isInCheck = jsonObj.isInCheck;
                    isInCheckmate = jsonObj.isInCheckMate; // 30
                    isAttackMove = jsonObj.isAttackMove;
                    moveDescription = jsonObj.moveDescription;
                    currentPlayerKingPosition = jsonObj.currentPlayerKingPosition;
                } catch (e) {
                    console.error('Failed to parse JSON:', e);
                }


                if (isInCheck || isInCheckmate) {
                    checkedKingTile = document.getElementById(currentPlayerKingPosition);
                    checkedKingTile.style.boxShadow = "0 0 10px 2px red";
                } else if (checkedKingTile !== null) {
                    console.log("checkedKingElseIf")
                    checkedKingTile.style.boxShadow = "none";
                    checkedKingTile = null
                }


                for (let i = 0; i < arr.length; i++) {
                    pieceInfo = arr[i].split(" ");
                    if (pieceInfo.length > 1) {
                        console.log(parseInt(pieceInfo[1]))
                        insertImage(parseInt(pieceInfo[1]), pieceInfo[0]);
                    }
                }

                //setBoard TODO fix
                setTimeout(function () {
                    if (isInCheckmate) {
                        var response = confirm("Checkmate! Play again?");
                        if (response == true) {
                            window.location.reload();
                        } else {
                            socket.close()
                        }
                    }
                }, 100);

                if (moveDescription !== "null") {
                    if (currentPlayer === "Black") {
                        tableCellIndex = 0
                        newRow = gameHistoryTable.insertRow(-1)
                    }
                    else {
                        tableCellIndex = 1
                    }
                    let moveCell = newRow.insertCell(tableCellIndex)
                    moveCell.textContent = moveDescription;
                }

                if (isAttackMove !== "null") {
                    if (isAttackMove) {
                        if (currentPlayer !== alliance) {
                            insertImage(eatIndex, jsonObj.attackedPiece)
                            eatIndex++
                        }
                        else {
                            insertImage(OpponentEatIndex, jsonObj.attackedPiece)
                            OpponentEatIndex++
                        }

                        // if (alliance === "Black" && alliance !== currentPlayer) {
                        //     eatTile = document.getElementById(eatIndex)
                        //     currTile.classList.add('flip');
                        // }
                    }
                }

                break

            case "playerLeftGame":
                let response = confirm("Your opponent left the game. Would you like to play against a new opponent?")
                if (response == true) {
                    window.location.reload();
                    console.log("Opponenet left the game")
                } else {
                    socket.close();
                }
                break;
        }
    });

    socket.addEventListener("close", (event) => {
        console.log("Disconnected from WebSocket" + event.code);
        // Handle WebSocket close event
    });

    socket.addEventListener("error", (event) => {
        console.error("WebSocket error:", event);

        // Handle WebSocket error event
    });

}
setConnection()
let sourceTileCoordinate = null
let destinationTileCoordinate = null
let sourceTile = null
let imageElement = null
let imageName = null


document.querySelectorAll('.tile').forEach(currTile => {

    currTile.addEventListener('click', function () {
        if (currTile.innerHTML !== "") {
            imgElement = currTile.querySelector('img');
            imageName = imgElement.getAttribute('src').split('/').pop();
            console.log(imageName);
        }
        if (sourceTileCoordinate === null) {
            if (currTile.innerHTML !== "" && imageName.charAt(0) === alliance.charAt(0) && alliance === currentPlayer) {
                sourceTileCoordinate = currTile.id
                currTile.style.boxShadow = "0 0 10px 2px blue";
                sourceTile = currTile
            }
            return
        }
        sourceTile.style.boxShadow = "none"
        destinationTileCoordinate = currTile.id
        jsonObj = {
            type: "move",
            sourceCoordinate: sourceTileCoordinate,
            destinationCoordinate: destinationTileCoordinate,
            gameId: gameId
        }
        jsonString = JSON.stringify(jsonObj);
        socket.send(jsonString)
        if (checkedKingTile !== null) {
            checkedKingTile.style.boxShadow = "0 0 10px 2px red"
        }
        sourceTileCoordinate = null
        destinationTileCoordinate = null
    })
})

function chooseAlliance() {
    let chooseAlliance = document.getElementById("chooseAlliance");
    //hideButtons();
    chooseAlliance.removeAttribute("hidden")
}

function startSinglePlayer(button) {
    jsonObj = {
        type: "singlePlayer",
        gameId: gameId
    }
    if (button.id === "allianceWhite") {
        jsonObj.alliance = "white"
    } else if (button.id === "allianceBlack") {
        jsonObj.alliance = "black"
    }
    jsonString = JSON.stringify(jsonObj)
    socket.send(jsonString)
}

function startMultiplayer() {
    showLoading()
    jsonObj = {
        type: "multiplayer",
        gameId: gameId
    }
    jsonString = JSON.stringify(jsonObj)
    socket.send(jsonString)
}

// function setBoard(arr) {
//     for (let i = 0; i < arr.length; i++) {
//         pieceInfo = arr[i].split(" ");
//         console.log(pieceInfo)
//         if (pieceInfo.length > 1)
//             insertImage(parseInt(pieceInfo[1]), pieceInfo[0]);
//     }
// }
