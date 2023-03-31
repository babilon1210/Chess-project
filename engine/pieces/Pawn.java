package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Move.PawnPromotionMove;
import com.chess.engine.board.Move.PawnMove;
import com.chess.engine.board.Tile;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece{

    private static final int[] CANDIDATE_MOVE_COORDINATES = {16,9,8,7};

    public Pawn(final int piecePosition,
                final Alliance pieceAlliance,
                final boolean isFirstMove){
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for(final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES){
              final int candidateDestinationCoordinate = this.piecePosition + (this.pieceAlliance.getDirection() * currentCandidateOffset);

              if(!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
                  continue;
              }
              final Tile candidateTile = board.getTile(candidateDestinationCoordinate);

              if(currentCandidateOffset == 8 && !candidateTile.isTileOccupied()) {
                  if(this.getPieceAlliance().isPromotionTile(candidateDestinationCoordinate))
                        legalMoves.add(new PawnPromotionMove(new PawnMove(board, this, candidateDestinationCoordinate)));
                  else
                        legalMoves.add(new PawnMove(board, this, candidateDestinationCoordinate));
              }
              else if(currentCandidateOffset == 16 && this.isFirstMove() &&
                      ((BoardUtils.SECOND_RANK[this.piecePosition] && this.getPieceAlliance().isWhite())
                  || (BoardUtils.SEVENTH_RANK[this.piecePosition] && this.getPieceAlliance().isBlack()))){
                      final int behindCandidateDestinationCoordinate = this.piecePosition + (this.getPieceAlliance().getDirection() * 8);
                      if(!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                      !candidateTile.isTileOccupied()){
                          legalMoves.add(new Move.PawnJump(board,this,candidateDestinationCoordinate));
                      }
              }else if(currentCandidateOffset == 7 && !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite())
                    || (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))){
                  if(candidateTile.isTileOccupied()){
                      final Piece pieceAtDestinationCoordinate = candidateTile.getPiece();
                      if(pieceAtDestinationCoordinate.getPieceAlliance() != this.getPieceAlliance()){
                          if(this.getPieceAlliance().isPromotionTile(candidateDestinationCoordinate))
                              legalMoves.add(new PawnPromotionMove(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestinationCoordinate)));
                          else
                              legalMoves.add(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestinationCoordinate));
                      }
                  }else if(board.getEnPassantPawn() != null){
                      if(board.getEnPassantPawn().getPiecePosition() == this.piecePosition+1){
                          if(this.getPieceAlliance() != board.getEnPassantPawn().getPieceAlliance())
                                legalMoves.add(new Move.PawnEnPassantAttackMove(board,this,candidateDestinationCoordinate,board.getEnPassantPawn()));
                      }
                  }
              }else if(currentCandidateOffset == 9 && !((BoardUtils.FIRST_COLUMN[this.getPiecePosition()] && this.pieceAlliance.isWhite())
                      || (BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))){
                  if(candidateTile.isTileOccupied()){
                      final Piece pieceAtDestinationCoordinate = candidateTile.getPiece();
                      if(pieceAtDestinationCoordinate.getPieceAlliance() != this.getPieceAlliance()){
                          if(this.getPieceAlliance().isPromotionTile(candidateDestinationCoordinate))
                              legalMoves.add(new PawnPromotionMove(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestinationCoordinate)));
                          else
                              legalMoves.add(new Move.PawnAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestinationCoordinate));
                      }
                  }else if(board.getEnPassantPawn() != null){
                      if(board.getEnPassantPawn().getPiecePosition() == this.piecePosition-1){
                          if(this.getPieceAlliance() != board.getEnPassantPawn().getPieceAlliance())
                                legalMoves.add(new Move.PawnEnPassantAttackMove(board,this,candidateDestinationCoordinate,board.getEnPassantPawn()));
                      }
                  }
              }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Pawn movePiece(Move move){
        return new Pawn(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance(), false);
    }

    public Piece getPromotionPiece(){
        return new Queen(this.piecePosition,this.pieceAlliance, false);
    }

    @Override
    public String toString(){
        return this.pieceType.toString();
    }
}
