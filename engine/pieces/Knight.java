package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorAttackMove;
import com.chess.engine.board.Move.MajorMove;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends Piece{

    private final static int[] CANDIDATE_MOVE_COORDINATES ={-17, -15, -10, -6, 6, 10, 15, 17};

    public Knight(final int piecePosition,
                final Alliance pieceAlliance,
                final boolean isFirstMove){
        super(PieceType.KNIGHT, piecePosition,pieceAlliance,isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        int candidateDestinationCoordinate;
        List<Move> legalMoves = new ArrayList<>();
        for(final int candidateDestinationOffset : CANDIDATE_MOVE_COORDINATES){
            candidateDestinationCoordinate = this.piecePosition + candidateDestinationOffset;

            if(isFirstColumnExclusion(this.piecePosition, candidateDestinationOffset) ||
                    isSecondColumnExclusion(this.piecePosition, candidateDestinationOffset) ||
                isSeventhColumnExclusion(this.piecePosition, candidateDestinationOffset) ||
                isEighthColumnExclusion(this.piecePosition, candidateDestinationOffset)){
                    continue;
            }

            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                if(!candidateDestinationTile.isTileOccupied()){
                    legalMoves.add(new MajorMove(board,this, candidateDestinationCoordinate));
                }
                else
                {
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance destinationPieceAlliance = pieceAtDestination.getPieceAlliance();

                    if(destinationPieceAlliance != this.getPieceAlliance()){
                        legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Knight movePiece(Move move){
        return new Knight(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), false);
    }

    @Override
    public String toString(){
        return this.pieceType.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateDestinationOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateDestinationOffset == -17 || candidateDestinationOffset == -10 || candidateDestinationOffset == 6 || candidateDestinationOffset == 15);
    }

    private static boolean isSecondColumnExclusion(final int currentPosition, final int candidateDestinationOffset){
        return BoardUtils.SECOND_COLUMN[currentPosition] && (candidateDestinationOffset == -10 || candidateDestinationOffset == 6);
    }

    private static boolean isSeventhColumnExclusion(final int currentPosition, final int candidateDestinationOffset){
        return BoardUtils.SEVENTH_COLUMN[currentPosition] && (candidateDestinationOffset == 10 || candidateDestinationOffset == -6);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidatePosition){
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidatePosition == 10 || candidatePosition == -6 || candidatePosition == 17 || candidatePosition == -15);
    }

}
