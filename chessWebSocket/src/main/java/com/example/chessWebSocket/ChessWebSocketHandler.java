package com.example.chessWebSocket;

import com.chess.engine.ai.AiEngine;
import com.chess.engine.player.MoveStatus;
import com.example.chessWebSocket.Game.MultiplayerGame;
import com.example.chessWebSocket.Game.SinglePlayerGame;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;

public class ChessWebSocketHandler implements WebSocketHandler {

    private int gameIdCounter = 0;
    private String json;
    private Queue<WebPlayer> multiplayers = new LinkedList<>();
    private Map<String, WebPlayer> players = new HashMap<>();
    private Queue<Integer> gameIdsLeftover = new PriorityQueue<>();

    private Map<Integer, Game> games = new HashMap();
    private Map<WebSocketSession, Integer> sessionGameIdMap= new HashMap();
    private WebPlayer whitePlayer =null, blackPlayer =null;
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode user = mapper.createObjectNode();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        players.put(session.getId(),new WebPlayer(session, session.getId(), WebPlayer.PlayerStatus.AVAILABLE));
        if(players.size() >= 2){
            user.put("type", "multiplayerAvailable");
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            notifyAllAvailablePlayers(new TextMessage(json));
            user.removeAll();
        }

//        if(sessions.size() >= 2){
//            int gameId = produceGameId();
//            whitePlayer = multiplayers.poll();
//            blackPlayer = multiplayers.poll();
//            sessionGameIdMap.put(whitePlayer.getPlayerSession(), gameId);
//            sessionGameIdMap.put(blackPlayer.getPlayerSession(),gameId);
//            intializePlayer(blackPlayer, Alliance.BLACK.toString(), gameId);
//            intializePlayer(whitePlayer, Alliance.WHITE.toString(), gameId);
//            Board newBoard = Board.createStandardBoard();
//            games.put(gameId, new Game(newBoard, whitePlayer, blackPlayer, gameId));
//            sendBoardUpdate(games.get(gameId),null);
//            whitePlayer =null;
//            blackPlayer =null;
//        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        WebPlayer currPlayer = players.get(session.getId());
        JsonNode rootNode = mapper.readTree(message.getPayload().toString());
        String type = rootNode.get("type").asText();
        int currGameId = rootNode.get("gameId").asInt();
        Game currGame = games.get(currGameId);
        switch (type){
            case "move":
                int sourceCoordinate = rootNode.get("sourceCoordinate").asInt();
                int destinationCoordinate = rootNode.get("destinationCoordinate").asInt();
                Move move = Move.MoveFactory.createMove(currGame.getBoard(),sourceCoordinate,destinationCoordinate);
                MoveStatus moveStatus = currGame.handleMove(move);
                if(moveStatus.isDone()){
                    sendBoardUpdate(currGame, move);
                }else{
                    return;
                }
                if(!currGame.getGameType().isMultiplayer()){
                    Move computerMove = AiEngine.bestMove(currGame.getBoard());
                    //System.out.println(move.getCurrentCoordinate() + " " + move.getDestinationCoordinate());
                    currGame.updateBoard(currGame.getBoard().currentPlayer().makeMove(computerMove).getTransitionBoard());
                    sendBoardUpdate(currGame, computerMove);
                }
                break;
            case "singlePlayer":
                String alliance = rootNode.get("alliance").asText();
                int gameId = produceGameId();
                WebPlayer singlePlayer = players.get(session.getId());
                singlePlayer.status = WebPlayer.PlayerStatus.INGAME;
                System.out.println("singlePlayer: " + singlePlayer.getPlayerId() + " " + session.getId());
                sessionGameIdMap.put(singlePlayer.getPlayerSession(), gameId);
                Board newBoard = Board.createStandardBoard();
                games.put(gameId, new SinglePlayerGame(newBoard, gameId, Game.GameType.SINGLE_PLAYER, singlePlayer));
                sendBoardUpdate(games.get(gameId), null);

                if(alliance.equals("white"))
                    intializePlayer(singlePlayer, Alliance.WHITE.toString(), gameId);
                else if(alliance.equals("black")) {
                    intializePlayer(singlePlayer, Alliance.BLACK.toString(), gameId);
                    Move firstMove = AiEngine.bestMove(newBoard);
                    //System.out.println(move.getCurrentCoordinate() + " " + move.getDestinationCoordinate());
                    games.get(gameId).updateBoard(games.get(gameId).getBoard().currentPlayer().makeMove(firstMove).getTransitionBoard());
                    sendBoardUpdate(games.get(gameId), firstMove);
                }

                break;
            case "multiplayer":
                multiplayers.add(currPlayer);

                if(multiplayers.size() >= 2){
//                    int gameId = produceGameId();
//                    whitePlayer = multiplayers.poll();
//                    blackPlayer = multiplayers.poll();
//                    whitePlayer.status = WebPlayer.PlayerStatus.INGAME;
//                    blackPlayer.status = WebPlayer.PlayerStatus.INGAME;
//                    sessionGameIdMap.put(whitePlayer.getPlayerSession(), gameId);
//                    sessionGameIdMap.put(blackPlayer.getPlayerSession(),gameId);
//                    intializePlayer(blackPlayer, Alliance.BLACK.toString(), gameId);
//                    intializePlayer(whitePlayer, Alliance.WHITE.toString(), gameId);
//                    Board newBoard = Board.createStandardBoard();
//                    games.put(gameId, new MultiplayerGame(newBoard, gameId, Game.GameType.MULTIPLALYER, whitePlayer, blackPlayer));
//                    sendBoardUpdate(games.get(gameId),null);
//                    whitePlayer =null;
//                    blackPlayer =null;
                    initializeMultiplayerGame();
                }
                break;

        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        user.put("type", "playerLeftGame");
        int gameId = getSessionGameId(session);
        System.out.println("GameID: " + gameId);
        if(gameId >= 0) {
            Game closedGame = games.get(gameId);
            if(closedGame.getGameType().isMultiplayer()) {
                MultiplayerGame multiplayerClosedGame = (MultiplayerGame) games.get(gameId);
                System.out.println(multiplayerClosedGame.getBlackPlayer().getPlayerId());
                WebPlayer whitePlayer = multiplayerClosedGame.getWhitePlayer();
                WebPlayer blackPlayer = multiplayerClosedGame.getBlackPlayer();
                try {
                    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
                    if (session.getId() == multiplayerClosedGame.getWhitePlayer().getPlayerId()) {
                        System.out.println("White player left game!");
                        blackPlayer.getPlayerSession().sendMessage(new TextMessage(json));
                    } else {
                        whitePlayer.getPlayerSession().sendMessage(new TextMessage(json));
                        System.out.println("Black player left game!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                players.remove(whitePlayer.getPlayerId());
                players.remove(blackPlayer.getPlayerId());
                sessionGameIdMap.remove(whitePlayer.getPlayerSession());
                sessionGameIdMap.remove(blackPlayer.getPlayerSession());
            }else {
                SinglePlayerGame singlePlayerClosedGame = (SinglePlayerGame) games.get(gameId);
                WebPlayer singlePlayer= singlePlayerClosedGame.getSinglePlayer();
                players.remove(singlePlayer.getPlayerId());
                sessionGameIdMap.remove(singlePlayer);
            }
            games.remove(gameId);
            gameIdsLeftover.add(gameId);
            user.removeAll();
            System.out.println("size1: " + players.size());
            return;
        }
        players.remove(session.getId());
        if(players.size() < 2){
            user.put("type", "multiplayerDisabled");
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            notifyAllAvailablePlayers(new TextMessage(json));
            user.removeAll();
        }
        System.out.println("size2: " + players.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public String getBoardDescription(Board board){
        StringBuilder builder = new StringBuilder("");
        for(Piece piece : board.getWhitePieces()){
            builder.append(piece.getPieceAlliance().toString().charAt(0) + ""
                    + piece.getPieceType() +
                    " " + piece.getPiecePosition() + ",");
        }
        for(Piece piece : board.getBlackPieces()){
            builder.append(piece.getPieceAlliance().toString().charAt(0) + ""
                    + piece.getPieceType() +
                    " " + piece.getPiecePosition() + ",");
        }

        return builder.toString();
    }

    public int produceGameId(){
        if(!gameIdsLeftover.isEmpty()) {
            return gameIdsLeftover.poll();
        }else{
            gameIdCounter++;
            return gameIdCounter;
        }
    }

    private void intializePlayer(WebPlayer player, String alliance, int gameId){
        user.put("type", "setAlliance");
        user.put("alliance", alliance);
        user.put("gameId", gameId);
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            player.getPlayerSession().sendMessage(new TextMessage(json));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user.removeAll();
    }

    private void initializeMultiplayerGame(){
        int gameId = produceGameId();
        whitePlayer = multiplayers.poll();
        blackPlayer = multiplayers.poll();
        sessionGameIdMap.put(whitePlayer.getPlayerSession(), gameId);
        sessionGameIdMap.put(blackPlayer.getPlayerSession(),gameId);
        players.get(whitePlayer.getPlayerId()).status= WebPlayer.PlayerStatus.INGAME;
        players.get(blackPlayer.getPlayerId()).status= WebPlayer.PlayerStatus.INGAME;
        intializePlayer(blackPlayer, Alliance.BLACK.toString(), gameId);
        intializePlayer(whitePlayer, Alliance.WHITE.toString(), gameId);
        Board newBoard = Board.createStandardBoard();
        games.put(gameId, new MultiplayerGame(newBoard, gameId, Game.GameType.MULTIPLALYER, whitePlayer, blackPlayer));
        sendBoardUpdate(games.get(gameId),null);
        whitePlayer =null;
        blackPlayer =null;
        if(multiplayers.size() < 2){
            disableMultiplayer();
        }
    }

    private void initializeSinglePlayerGame(){
        int gameId = produceGameId();
        whitePlayer = multiplayers.poll();
        sessionGameIdMap.put(whitePlayer.getPlayerSession(), gameId);
        sessionGameIdMap.put(blackPlayer.getPlayerSession(),gameId);
        intializePlayer(blackPlayer, Alliance.BLACK.toString(), gameId);
        intializePlayer(whitePlayer, Alliance.WHITE.toString(), gameId);
        Board newBoard = Board.createStandardBoard();
        games.put(gameId, new SinglePlayerGame(newBoard, gameId, Game.GameType.SINGLE_PLAYER, whitePlayer));
        sendBoardUpdate(games.get(gameId),null);
        whitePlayer =null;
        blackPlayer =null;
    }

    private void sendBoardUpdate(Game game, Move lastMove){
        user.put("type", "boardUpdate");
        user.put("boardPieceDescription", getBoardDescription( game.getBoard()));
        user.put("isInCheck", game.getBoard().currentPlayer().isInCheck());
        user.put("isInCheckMate", game.getBoard().currentPlayer().isInCheckMate());
        user.put("currentPlayer", game.getBoard().currentPlayer().getAlliance().toString());
        user.put("currentPlayerKingPosition", game.getBoard().currentPlayer().getPlayerKing().getPiecePosition());
        if(lastMove != null) {
            user.put("moveDescription", lastMove.toString());
            user.put("isAttackMove", lastMove.isAttack());
            if(lastMove.isAttack())
                user.put("attackedPiece", lastMove.getAttackedPiece().getPieceAlliance().toString().charAt(0) + "" +
                        lastMove.getAttackedPiece().getPieceType().toString());
            else
                user.put("attackedPiece","null");
        }else{
            user.put("moveDescription", "null");
            user.put("isAttackMove", "null");
            user.put("attackedPiece", "null");
        }
        try{
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            TextMessage boardUpdate = new TextMessage(json);
            game.updatePlayers(boardUpdate);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        user.removeAll();
    }

    public void handleMove(Game currGame, int sourceCoordinate, int destinationCoordinate){
        Board currBoard = currGame.getBoard();
        final Move move = Move.MoveFactory.createMove(currBoard,
                sourceCoordinate,
                destinationCoordinate);
        MoveTransition moveTransition = currBoard.currentPlayer().makeMove(move);
        if(moveTransition.getMoveStatus().isDone()){
            currBoard = moveTransition.getTransitionBoard();
            currGame.updateBoard(currBoard);
            sendBoardUpdate(currGame, move);
        }
        System.out.println(moveTransition.getMoveStatus() + ", " + currBoard.currentPlayer().getAlliance());
    }

    public void notifyAllAvailablePlayers(TextMessage message){
        for (Map.Entry<String, WebPlayer> entry : players.entrySet()) {
            WebPlayer currPlayer = entry.getValue();
            try {
                currPlayer.getPlayerSession().sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    public void handlePlayerLeft(Game currGame, String leftPlayerAlliance){
//        user.put("type", "playerLeft");
//        try {
//            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
//            if (leftPlayerAlliance == "White") {
//                System.out.println("White left game:" + json);
//                currGame.getBlackPlayer().getPlayerSession().sendMessage(new TextMessage(json));
//            } else{
//                currGame.getWhitePlayer().getPlayerSession().sendMessage(new TextMessage(json));
//                System.out.println("Black left game:" + json);
//            }
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//        user.removeAll();
//    }

    public int getSessionGameId(WebSocketSession session){
        Integer sessionGameId = sessionGameIdMap.get(session);
        return (sessionGameId == null) ? -1 : sessionGameId;
    }

    public void disableMultiplayer(){
        user.put("type", "multiplayerDisabled");
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        notifyAllAvailablePlayers(new TextMessage(json));
        user.removeAll();
    }
}
