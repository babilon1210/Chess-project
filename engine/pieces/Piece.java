package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;

public abstract class Piece {

    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected boolean isFirstMove;
    protected final PieceType pieceType;
    private final int cachedHashCode;



    Piece(final PieceType pieceType,
          final int piecePosition,
          final Alliance pieceAlliance,
          final boolean isFirstMove){
        this.pieceAlliance = pieceAlliance;
        this.piecePosition = piecePosition;
        this.pieceType = pieceType;
        //TODO define firstMove logic
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    private int computeHashCode(){
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object other){
        if(this == other)
            return true;

        if(!(other instanceof Piece))
            return false;
        final Piece otherPiece = (Piece) other;
        return this.piecePosition == otherPiece.getPiecePosition() &&
                this.pieceAlliance == otherPiece.getPieceAlliance()&&
                this.isFirstMove == otherPiece.isFirstMove() &&
                this.pieceType == otherPiece.getPieceType();

    }

    public PieceType getPieceType(){
        return this.pieceType;
    }
    public int getPieceValue(){
        return this.pieceType.getPieceValue();
    }

    public boolean isFirstMove(){
        return this.isFirstMove;
    }

    public Alliance getPieceAlliance(){
        return this.pieceAlliance;
    }

    public int getPiecePosition(){
        return this.piecePosition;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);
    public abstract Piece movePiece(Move move);

    public enum PieceType{
        PAWN("P", 1){
            @Override
            public boolean isKing() {
                return false;
            }
        },
        KNIGHT("N", 3) {
            @Override
            public boolean isKing() {
                return false;
            }
        },
        BISHOP("B", 3) {
            @Override
            public boolean isKing() {
                return false;
            }
        },
        ROOK("R", 5) {
            @Override
            public boolean isKing() {
                return false;
            }
        },
        QUEEN("Q", 9){
            @Override
            public boolean isKing(){
                return false;
            }
        },
        KING("K", 100){
            @Override
            public boolean isKing(){
                return true;
            }
        };
        private String pieceName;
        private int pieceValue;
        PieceType(final String pieceName, final int pieceValue){
            this.pieceValue = pieceValue;
            this.pieceName = pieceName;
        }

        @Override
        public String toString(){
            return this.pieceName;
        }

        public int getPieceValue(){
            return this.pieceValue;
        }
        public abstract boolean isKing();
    }

}
