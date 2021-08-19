package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public abstract class _1_Negascout_PVS extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int negascout(final boolean isMyMove, int alfa, final int beta, final int depth) {

        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);
        if (depth <= 0 || isTerminalNode(currNode))
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        final Iterator<Move> it = allMoves.iterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);
        int value = -negascout(!isMyMove, -beta, -alfa, depth - 1);
        if (alfa < value) alfa = value; // PV-node
        undoMove(move);

        while (it.hasNext()) {

            if (beta <= value) return beta; // fail-high
            // if (beta <= value) return value; // fail-soft

            move = it.next();
            makeMove(move);

            // null-window search:
            value = -negascout(!isMyMove, -alfa - 1, -alfa, depth - 1);

            // negascout
            if (alfa < value && value < beta && depth > 1) {
                final int value2 = -negascout(!isMyMove, -beta, -value, depth - 1);
                if (value < value2) value = value2;
            }

            // PVS
            /*if (alfa < value && value < beta) {
                value = -negascout(!isMyMove, -beta, -alfa, depth - 1);
            }*/

            undoMove(move);
            if (alfa < value) alfa = value; // PV-node
        }

        return alfa;
    }

    /*------------ Точка запуска алгоритма поиска ------------*/

    Move getNextBestMove() {
        final boolean isMyMove = true;
        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);

        if (isTerminalNode(currNode)) throw new RuntimeException("Ходов нет, игра окончена");

        int bestValue = MIN_VALUE;
        Move bestMove = allMoves.getFirst();

        for (final Move move : allMoves) {
            makeMove(move);
            final int value = -negascout(!isMyMove, MIN_VALUE, MAX_VALUE, MAX_DEPTH);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
