package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlackPlayer extends Player{
    public BlackPlayer(final Board board,
                       final Collection<Move> blackStandardLegalMoves,
                       final Collection<Move> whiteStandardLegalMoves) {
        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals, final Collection<Move> opponentsLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        final King blackKing = this.playerKing;

        if(blackKing.isFirstMove() && !isInCheck){
            if(!board.getTile(5).isTileOccupied() &&
               !board.getTile(6).isTileOccupied()){
                final Tile kingCastleTile = board.getTile(7);
                if(kingCastleTile.isTileOccupied() && kingCastleTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(5,opponentsLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(6, opponentsLegals).isEmpty()) {
                        kingCastles.add(new Move.KingSideCastleMove(board, blackKing,
                                                   6, (Rook)kingCastleTile.getPiece(),
                                                                    kingCastleTile.getTileCoordinate(),
                                                   5));
                    }
                }
            }
            if(!board.getTile(3).isTileOccupied() &&
               !board.getTile(2).isTileOccupied() &&
               !board.getTile(1).isTileOccupied()){
                final Tile queenCastleTile = board.getTile(0);
                if(queenCastleTile.isTileOccupied() && queenCastleTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(3, opponentsLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(2, opponentsLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(1,opponentsLegals).isEmpty()) {
                        kingCastles.add(new Move.QueenSideCastleMove(board, blackKing,
                                                    2, (Rook)queenCastleTile.getPiece(),
                                                                     queenCastleTile.getTileCoordinate(), 3));
                    }
                }
            }

        }
        return ImmutableList.copyOf(kingCastles);
    }
}
