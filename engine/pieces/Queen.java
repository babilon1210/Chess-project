package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.MajorMove;
import com.chess.engine.board.Move.MajorAttackMove;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends Piece{

    private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES= {8,1,7,9,-8,-1,-7,-9};

    public Queen(final int piecePosition,
                 final Alliance pieceAlliance,
                 final boolean isFirstMove){
        super(PieceType.QUEEN, piecePosition, pieceAlliance, isFirstMove);
    }


    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES){
            int candidateDestinationCoordinate = this.piecePosition;

            while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                if(isFirstColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
                        isEighthColumnExclusion(candidateDestinationCoordinate, candidateCoordinateOffset))
                            break;

                candidateDestinationCoordinate+=candidateCoordinateOffset;

                if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                    if(!candidateDestinationTile.isTileOccupied()){
                        legalMoves.add(new MajorMove(board,this, candidateDestinationCoordinate));
                    }else{
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();

                        if(this.getPieceAlliance() != pieceAtDestination.getPieceAlliance()){
                            legalMoves.add(new MajorAttackMove(board,this,candidateDestinationCoordinate, pieceAtDestination));
                        }
                        break;
                    }
                }
            }

        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Queen movePiece(Move move){
        return new Queen(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), false);
    }

    @Override
    public String toString(){
        return this.pieceType.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int destinationOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && (destinationOffset == -9 || destinationOffset == 7 || destinationOffset == -1);
    }


    private static boolean isEighthColumnExclusion(final int currentCoordinate, final int destinationOffset){
        return BoardUtils.EIGHTH_COLUMN[currentCoordinate] && (destinationOffset == 9 || destinationOffset == -7 || destinationOffset == 1);
    }
}
