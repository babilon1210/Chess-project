package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Bishop extends Piece{
    private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {7,9,-7,-9};

    public Bishop(final int piecePosition,
                  final Alliance pieceAlliance,
                  final boolean isFirstMove){
        super(PieceType.BISHOP, piecePosition, pieceAlliance, isFirstMove);
    }
    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        for(final int currentCandidateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES){

            int candidateDestinationCoordinate = this.piecePosition;
            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                if(isFirstColumnExclusion(candidateDestinationCoordinate, currentCandidateOffset) ||
                isEighthColumnExclusion(candidateDestinationCoordinate, currentCandidateOffset)){
                    break;
                }
                candidateDestinationCoordinate+=currentCandidateOffset;
                if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                    if(!candidateDestinationTile.isTileOccupied()){
                        legalMoves.add(new Move.MajorMove(board,this, candidateDestinationCoordinate));
                    }
                    else
                    {
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();

                        if(this.pieceAlliance != pieceAlliance){
                            legalMoves.add(new Move.MajorAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                        }
                            break;
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Bishop movePiece(Move move){
        return new Bishop(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), false);
    }

    @Override
    public String toString(){
        return this.pieceType.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentCoordinate, final int destinationOffset){
        return BoardUtils.FIRST_COLUMN[currentCoordinate] && (destinationOffset == -9 || destinationOffset == 7);
    }
    private static boolean isEighthColumnExclusion(final int currentCoordinate, final int destinationOffset){
        return BoardUtils.EIGHTH_COLUMN[currentCoordinate] && (destinationOffset == 9 || destinationOffset == -7);
    }


}
