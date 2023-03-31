package com.chess.engine;

import com.chess.engine.board.BoardUtils;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;

public enum Alliance {

    WHITE{
        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer,
                                   final BlackPlayer blackPlayer){
            return whitePlayer;
        }
        @Override
        public boolean isPromotionTile(int candidateDestinationCoordinate){
            return BoardUtils.EIGHTH_RANK[candidateDestinationCoordinate];
        }
        @Override
        public int getDirection(){
            return -1;
        }

        @Override
        public boolean isWhite() {
            return true;
        }

        @Override
        public boolean isBlack() {
            return false;
        }

        @Override
        public String toString(){
            return "White";
        }
    },
    BLACK{
        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer,
                                   final BlackPlayer blackPlayer){
            return blackPlayer;
        }

        @Override
        public boolean isPromotionTile(int candidateDestinationCoordinate){
            return BoardUtils.FIRST_RANK[candidateDestinationCoordinate];
        }
        @Override
        public int getDirection(){
            return 1;
        }

        @Override
        public boolean isWhite() {
            return false;
        }

        @Override
        public boolean isBlack() {
            return true;
        }

        @Override
        public String toString(){
            return "Black";
        }
    };
    public abstract int getDirection();
    public abstract boolean isWhite();
    public abstract boolean isBlack();
    public abstract boolean isPromotionTile(int candidateDestinationCoordinate);
    public abstract Player choosePlayer(WhitePlayer whitePlayer, BlackPlayer blackPlayer);
}
