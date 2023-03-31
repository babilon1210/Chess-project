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

public class King extends Piece{

    private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES = {9,8,7,1,-1,-7,-8,-9};

    public King(final int pieceCoordinate,
                final Alliance pieceAlliance,
                final boolean isFirstMove){
        super(PieceType.KING, pieceCoordinate,pieceAlliance,isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int candidateDestinationOffset : CANDIDATE_MOVE_VECTOR_COORDINATES){
            if(isFirstColumnExclusion(this.piecePosition,candidateDestinationOffset)
                    || isEighthColumnExclusion(this.piecePosition,candidateDestinationOffset)){
                continue;
            }
            int candidateDestinationCoordinate = this.piecePosition + candidateDestinationOffset;
            if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                if(!candidateDestinationTile.isTileOccupied()){
                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                }else{
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    if(pieceAtDestination.pieceAlliance != this.pieceAlliance){
                        legalMoves.add(new MajorAttackMove(board,this,candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public King movePiece(Move move){
        return new King(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), false);
    }

    @Override
    public String toString(){
        return this.pieceType.toString();
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateDestinationOffset){
        return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateDestinationOffset == -1
                || candidateDestinationOffset == -9
                || candidateDestinationOffset == 7);
    }

    private static final boolean isEighthColumnExclusion(final int currentPosition, final int candidateDestinationOffset){
        return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateDestinationOffset == 1
                || candidateDestinationOffset == 9
                || candidateDestinationOffset == -7);
    }

}

