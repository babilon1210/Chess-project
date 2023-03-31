package com.example.chessWebSocket;

import org.springframework.web.socket.WebSocketSession;

public class WebPlayer {
    private WebSocketSession playerSession;
    protected PlayerStatus status;
    private String playerId;


    public WebPlayer(WebSocketSession playerSession, String playerId, PlayerStatus status){
        this.playerSession = playerSession;
        this.status = status;
        this.playerId = playerId;
    }

    public WebSocketSession getPlayerSession(){
        return this.playerSession;
    }

    public PlayerStatus getPlayerStatus(){
        return this.status;
    }

    public String getPlayerId(){
        return this.playerId;
    }

    public enum PlayerStatus{
        AVAILABLE,
        INGAME;
    }
}
