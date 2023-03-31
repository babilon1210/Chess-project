package com.chess.engine.ai;

import com.chess.engine.board.Move;

class ScoredMove {
    final float score;
    final Move move;

    protected ScoredMove(Move move, float score){
        this.score = score;
        this.move = move;
    }
}
