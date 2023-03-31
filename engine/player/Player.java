package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Player {
    protected final Board board;
    protected final King playerKing;
    protected Collection<Move> legalMoves;

    protected boolean isInCheck;

    public Player(final Board board,
                  final Collection<Move> legalMoves,
                  final Collection<Move> opponentLegalMoves){
        this.board = board;
        this.playerKing = establishKing();
        isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
        legalMoves.addAll(calculateKingCastles(legalMoves,opponentLegalMoves));
        this.legalMoves = Collections.unmodifiableCollection(legalMoves);
    }

    protected static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> opponentMoves){
        List<Move> attackMoves = new ArrayList<>();
        for(Move move : opponentMoves){
            if(piecePosition == move.getDestinationCoordinate()){
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    private King establishKing(){
        for(Piece piece : getActivePieces()){
            if(piece.getPieceType().isKing()){
                return (King) piece;
            }
        }
        throw new RuntimeException("Should not reach this code section, no king no valid board");
    }
    public King getPlayerKing(){
        return this.playerKing;
    }
    public Collection<Move> getLegalMoves(){
        return this.legalMoves;
    }

    public boolean isMoveLegal(Move move){
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck(){
        return this.isInCheck;
    }
    public boolean isInCheckMate(){
        return this.isInCheck && !hasEscapeMoves();
    }
    public boolean isInStaleMate(){
        return !this.isInCheck && !hasEscapeMoves();
    }
    public boolean isCastled(){
        return false;
    }
    public boolean hasEscapeMoves(){
        for(Move move: legalMoves){
            MoveTransition moveTransition = makeMove(move);
            if(moveTransition.getMoveStatus().isDone())
                return true;
        }

        return false;
    }

    public MoveTransition makeMove(final Move move){

        if(!isMoveLegal(move)){
            return new MoveTransition(this.board,move,MoveStatus.ILLEGAL_MOVE);
        }
        final Board transitionBoard = move.execute();

        if(transitionBoard.currentPlayer().getOpponent().isInCheck()){
            return new MoveTransition(this.board,move,MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }
        return new MoveTransition(transitionBoard,move,MoveStatus.DONE);
    }


    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();
    protected abstract Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentsLegals);
}
