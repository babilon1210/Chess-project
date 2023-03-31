package com.chess.engine.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

public class MiniMaxTester {

    public static void main(String[] args){

        Board board = Board.createStandardBoard();
        System.out.println(board.toString());
        while(!board.currentPlayer().isInCheckMate()) {
            Move move = MiniMaxEngine.bestMove(board);
            System.out.println(move.getCurrentCoordinate() + " " + move.getDestinationCoordinate());
            board = board.currentPlayer().makeMove(move).getTransitionBoard();
            System.out.println(board.toString());
        }
    }
}
