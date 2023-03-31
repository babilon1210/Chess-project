package com.chess.engine.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MiniMaxEngine {
    private static int DEPTH = 3;

    public static Move bestMove(Board board){
        ScoredMove scoredMove = evaluate(board, DEPTH);
        return scoredMove.move;
    }

    private static ScoredMove evaluate(Board board, int depth){

        if(depth == 0){
            return new ScoredMove(null, staticEvaluate(board));
        }

        Collection<Move> playerLegalMoves = board.currentPlayer().getLegalMoves();
        List<ScoredMove> scoredMoves = new ArrayList<>();
        ScoredMove bestMove = null;
        for(Move move : playerLegalMoves){
            MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone())
                scoredMoves.add(new ScoredMove(move,evaluate(moveTransition.getTransitionBoard(), depth-1).score));
        }
        if(board.currentPlayer().getAlliance().isWhite()){
            bestMove = new ScoredMove(null, Integer.MIN_VALUE);
            for(ScoredMove scoredMove: scoredMoves){
                if(scoredMove.score > bestMove.score){
                    bestMove = scoredMove;
                }
            }
        }else{
            bestMove = new ScoredMove(null, Integer.MAX_VALUE);
            for(ScoredMove scoredMove: scoredMoves){
                if(scoredMove.score < bestMove.score){
                    bestMove = scoredMove;
                }
            }
        }
        if(bestMove.move == null){
            if(!scoredMoves.isEmpty())
                bestMove = scoredMoves.get(0);
        }

        return bestMove;
    }

    private static float staticEvaluate(Board board){
        int whiteSum=0, blackSum=0;
        for(Piece piece: board.getAllActivePieces()){
            if(piece.getPieceAlliance().isWhite())
                whiteSum+= piece.getPieceType().getPieceValue();
            else
                blackSum+=piece.getPieceType().getPieceValue();
        }

        return (float) (0.1 * (board.whitePlayer().getLegalMoves().size() - board.blackPlayer().getLegalMoves().size())) +
                (whiteSum - blackSum);
    }

}
