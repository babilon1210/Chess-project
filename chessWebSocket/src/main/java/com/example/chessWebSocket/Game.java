package com.example.chessWebSocket;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveStatus;
import com.chess.engine.player.MoveTransition;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

public abstract class Game {

    private Board board;
    //private WebPlayer whitePlayer, blackPlayer;
    private int gameId;
    private GameType gameType;

    public Game(Board board,
                int gameId,
                GameType gameType){
        this.board = Board.createStandardBoard();
        this.gameId = gameId;
        this.gameType = gameType;
    }

    public int getGameId(){
        return this.gameId;
    }

    public Board getBoard(){
        return this.board;
    }

    public GameType getGameType(){
        return this.gameType;
    }

    public void updateBoard(Board transitionBoard){
        this.board=transitionBoard;
    }

    public MoveStatus handleMove(Move move){
        MoveTransition moveTransition = this.board.currentPlayer().makeMove(move);
        if(moveTransition.getMoveStatus().isDone()){
            updateBoard(moveTransition.getTransitionBoard());
        }
        return moveTransition.getMoveStatus();
    }
    public abstract void updatePlayers(TextMessage message);



    public static final class MultiplayerGame extends Game{

        WebPlayer whitePlayer, blackPlayer;
        public MultiplayerGame(Board board, int gameId, GameType gameType, WebPlayer whitePlayer, WebPlayer blackPlayer) {
            super(board, gameId, gameType);
            this.whitePlayer = whitePlayer;
            this.blackPlayer = blackPlayer;
        }

        public WebPlayer getWhitePlayer(){
            return this.whitePlayer;
        }

        public WebPlayer getBlackPlayer(){
            return this.blackPlayer;
        }

        @Override
        public void updatePlayers(TextMessage message) {
            try {
                whitePlayer.getPlayerSession().sendMessage(message);
                blackPlayer.getPlayerSession().sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static final class SinglePlayerGame extends Game{
        WebPlayer singlePlayer;
        public SinglePlayerGame(Board board,
                                int gameId,
                                GameType gameType,
                                WebPlayer singlePlayer){
            super(board,gameId, gameType);
            this.singlePlayer = singlePlayer;
        }

        public WebPlayer getSinglePlayer(){
            return this.singlePlayer;
        }

        @Override
        public void updatePlayers(TextMessage message){
            try {
                singlePlayer.getPlayerSession().sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public enum GameType{
        MULTIPLALYER{
            @Override
            public boolean isMultiplayer() {
                return true;
            }
        },
        SINGLE_PLAYER {
            @Override
            public boolean isMultiplayer() {
                return false;
            }
        };

        public abstract boolean isMultiplayer();

    }
}
