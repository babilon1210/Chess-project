package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.board.Move.KingSideCastleMove;
import com.chess.engine.board.Move.QueenSideCastleMove;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WhitePlayer extends Player {
    public WhitePlayer(final Board board,
                       final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentsLegals) {
        List<Move> kingCastles = new ArrayList<>();
        final King whiteKing = this.playerKing;

        if(whiteKing.isFirstMove() && !isInCheck){
            if(!board.getTile(61).isTileOccupied() &&
               !board.getTile(62).isTileOccupied()){
                final Tile kingCastleTile = board.getTile(63);
                if(kingCastleTile.isTileOccupied() && kingCastleTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(61,opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(62, opponentsLegals).isEmpty()) {
                        kingCastles.add(new KingSideCastleMove(board, whiteKing,
                                              62, (Rook) kingCastleTile.getPiece(),
                                                               kingCastleTile.getTileCoordinate(), 61));
                    }
                }
            }
            if(!board.getTile(59).isTileOccupied() &&
               !board.getTile(58).isTileOccupied() &&
               !board.getTile(57).isTileOccupied()){
                final Tile queenCastleTile = board.getTile(56);
                if(queenCastleTile.isTileOccupied() && queenCastleTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(59, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(58, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(57,opponentsLegals).isEmpty()) {
                        kingCastles.add(new QueenSideCastleMove(board, whiteKing, 58, (Rook) queenCastleTile.getPiece(), queenCastleTile.getTileCoordinate(), 59));
                    }
                }
            }

        }
        return ImmutableList.copyOf(kingCastles);
        }

}

