package com.chess.engine.board;

import com.chess.engine.board.Board.Builder;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

public abstract class Move {
    final Board board;
    final int destinationCoordinate;
    final Piece movedPiece;
    protected final boolean isFirstMove;
    public static final Move NULL_MOVE = new NullMove();

    public Move(Board board, Piece movedPiece, int destinationCoordinate){
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = movedPiece.isFirstMove();
    }

    private Move(Board board, int destinationCoordinate){
        this.board = board;
        this.destinationCoordinate = destinationCoordinate;
        this.movedPiece = null;
        this.isFirstMove = false;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getDestinationCoordinate();
        result = prime * result + this.movedPiece.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other){
       if(this == other)
           return true;

       if(!(other instanceof Move))
           return false;

       final Move otherMove = (Move) other;

       return this.destinationCoordinate == otherMove.destinationCoordinate &&
               getMovedPiece().equals(otherMove.getMovedPiece());

    }

    public Board getBoard(){
        return this.board;
    }

    public int getDestinationCoordinate(){
        return this.destinationCoordinate;
    }
    public int getCurrentCoordinate(){
        return movedPiece.getPiecePosition();
    }

    public Board execute(){
        final Builder builder = new Builder();
        for(Piece piece : this.board.currentPlayer().getActivePieces()){
            if(!piece.equals(this.movedPiece)){
                builder.setPiece(piece);
            }
        }
        for(Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
           builder.setPiece(piece);
        }
        Piece movedPiece = this.movedPiece.movePiece(this);
        builder.setPiece(movedPiece);
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());

        return builder.build();
    }

    public Piece getMovedPiece(){
        return this.movedPiece;
    }

    public boolean isAttack(){
        return false;
    }

    public boolean isCastlingMove(){
        return false;
    }

    public Piece getAttackedPiece(){
        return null;
    }

    public static final class MajorAttackMove extends AttackMove{

        public MajorAttackMove(Board board, Piece movedPiece, int destinationCoordinate, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(Object other){
            return this == other || other instanceof MajorAttackMove && super.equals(other);
        }

        @Override
        public String toString(){
            return this.movedPiece + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static final class MajorMove extends Move{
        public MajorMove(final Board board,
                         final Piece movedPiece,
                         final int destinationCoordinate){
            super(board,movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof MajorMove && super.equals(other);
        }

        @Override
        public String toString(){
            return movedPiece.getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class AttackMove extends Move{
        final Piece attackedPiece;
        public AttackMove(Board board, Piece movedPiece, int destinationCoordinate,  Piece attackedPiece){
            super(board,movedPiece,destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode(){
            return super.hashCode() + attackedPiece.hashCode();
        }
        @Override
        public boolean equals(final Object other){
            if(this == other)
                return true;

            if(!(other instanceof AttackMove))
                return false;

            final AttackMove otherAttackMove = (AttackMove) other;
            return super.equals(otherAttackMove) && attackedPiece.equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public Board execute(){
            final Builder builder = new Builder();
            for(Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!piece.equals(this.movedPiece))
                    builder.setPiece(piece);
            }
            for(Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
                if(!piece.equals(this.attackedPiece))
                    builder.setPiece(piece);
            }
            builder.setPiece(movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public boolean isAttack(){
            return true;
        }

        @Override
        public Piece getAttackedPiece(){
            return this.attackedPiece;
        }

        @Override
        public String toString(){
            return this.movedPiece +
                    BoardUtils.getPositionAtCoordinate(movedPiece.getPiecePosition()) +
                    "x" +
                    this.attackedPiece +
                    this.destinationCoordinate;
        }
    }

    public static class PawnMove extends Move{
        public PawnMove(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate){
            super(board,movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(Object other){
            return this == other || other instanceof PawnMove && super.equals(other);
        }

        @Override
        public String toString(){
            return movedPiece.toString() + "" + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnPromotionMove extends Move{
        private final Move decoratedMove;
        private final Pawn promotedPawn;
        public PawnPromotionMove(final Move decoratedMove){
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(),decoratedMove.getDestinationCoordinate());

            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn)this.decoratedMove.getMovedPiece();
        }
        @Override
        public Board execute(){
            final Board board = this.decoratedMove.execute();
            final Builder builder = new Builder();
            for(Piece piece : board.currentPlayer().getActivePieces()){
                if(!piece.equals(promotedPawn)){
                    builder.setPiece(piece);
                }
            }
            for(Piece piece :  board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            builder.setPiece(promotedPawn.getPromotionPiece().movePiece(this));
            builder.setMoveMaker(board.currentPlayer().getAlliance());

            return builder.build();
        }

        @Override
        public boolean isAttack(){
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece(){
            return decoratedMove.getAttackedPiece();
        }
        @Override
        public String toString(){
            return decoratedMove.toString();
        }
    }

    public static class PawnAttackMove extends AttackMove{
        public PawnAttackMove(final Board board,
                              final Piece movedPiece,
                              final int destinationCoordinate,
                              final Piece attackedPiece){
            super(board,movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(Object other){
            return this == other || other instanceof PawnAttackMove && super.equals(other);
        }

        @Override
        public String toString(){
            return BoardUtils.getPositionAtCoordinate(this.movedPiece.getPiecePosition()).charAt(0) + "x" +
                    BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnEnPassantAttackMove extends AttackMove{
        public PawnEnPassantAttackMove(final Board board,
                              final Piece movedPiece,
                              final int destinationCoordinate,
                              final Piece attackedPiece){
            super(board,movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(Object other){
            return this == other || other instanceof PawnEnPassantAttackMove && super.equals(other);
        }

        @Override
        public Board execute(){
            Builder builder = new Builder();

            for(Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!piece.equals(this.movedPiece))
                    builder.setPiece(piece);
            }

            for(Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
                if(!piece.equals(this.attackedPiece)){
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

    }

    public static final class PawnJump extends Move{
        public PawnJump(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate){
            super(board,movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute(){
            final Builder builder = new Builder();

            for(Piece piece : this.board.currentPlayer().getActivePieces()){
                if(!piece.equals(this.movedPiece)){
                    builder.setPiece(piece);
                }
            }
            for(Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }
            final Pawn movedPawn = (Pawn) this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            builder.setEnPassantPawn(movedPawn);
            return builder.build();
        }

        @Override
        public String toString(){
            return this.movedPiece.getPieceType().toString() + BoardUtils.getPositionAtCoordinate(destinationCoordinate);
        }
    }

    static abstract class CastleMove extends Move{

        protected Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;
        public CastleMove(final Board board,
                         final Piece movedPiece,
                         final int destinationCoordinate,
                         final Rook castleRook,
                         final int castleRookStart,
                         final int castleRookDestination){
            super(board,movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook(){
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove(){
            return true;
        }

        @Override
        public Board execute(){
            final Builder builder = new Builder();

            for(final Piece piece : board.currentPlayer().getActivePieces()){
                if(!piece.equals(this.movedPiece) && !piece.equals(this.castleRook))
                    builder.setPiece(piece);
            }
            for(final Piece piece : board.currentPlayer().getOpponent().getActivePieces()){
                builder.setPiece(piece);
            }

            builder.setPiece(movedPiece.movePiece(this));
            //TODO Look into the first move on normal pieces
            builder.setPiece(new Rook(castleRookDestination, board.currentPlayer().getAlliance(), false));
            builder.setMoveMaker(board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public int hashCode(){
            final int prime = 31;
            int result = super.hashCode();
            result = result * prime + this.castleRook.hashCode();
            result = result * prime + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other){
            if(this == other)
                return true;
            if(!(other instanceof CastleMove))
                return false;
            final CastleMove otherCastleMove = (CastleMove) other;
            return super.equals(otherCastleMove) && this.castleRook.equals(otherCastleMove.getCastleRook());
        }
    }

    public static final class KingSideCastleMove extends CastleMove{
        public KingSideCastleMove(final Board board,
                                  final Piece movedPiece,
                                  final int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookStart,
                                  final int castleRookDestination){
            super(board,movedPiece,
                  destinationCoordinate,castleRook,
                  castleRookStart, castleRookDestination);
        }
        @Override
        public boolean equals(final Object other){
            return (this == other) || other instanceof KingSideCastleMove && super.equals(other);
        }

        @Override
        public String toString(){
            return "O-O";
        }
    }

    public static final class QueenSideCastleMove extends CastleMove{
        public QueenSideCastleMove(final Board board,
                                   final Piece movedPiece,
                                   final int destinationCoordinate,
                                   final Rook castleRook,
                                   final int castleRookStart,
                                   final int castleRookDestination){
            super(board,movedPiece,
                  destinationCoordinate, castleRook,
                  castleRookStart, castleRookDestination);

        }

        @Override
        public boolean equals(final Object other){
            return (this == other) || other instanceof QueenSideCastleMove && super.equals(other);
        }

        @Override
        public String toString(){
            return "O-O-O";
        }
    }

    public static final class NullMove extends Move{
        public NullMove(){
            super(null,65);
        }

        @Override
        public int getCurrentCoordinate(){
            return -1;
        }

        @Override
        public Board execute(){
            throw new RuntimeException("Cannot execute the NullMove");
        }
    }

    public static class MoveFactory{
        public MoveFactory(){
            throw new RuntimeException("Not instantiable!");
        }
        public static Move createMove(final Board board,
                               final int currentCoordinate,
                               final int destinationCoordinate){
            for(Move move : board.getAllLegalMoves()){
                if(move.getCurrentCoordinate() == currentCoordinate &&
                   move.getDestinationCoordinate() == destinationCoordinate){
                      return move;
                }
            }
            return NULL_MOVE;
        }
    }
}
