package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.List;
import java.util.ListIterator;

public abstract class _6_HistoryHeuristic extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    private static final int MY_COLOR = 1;
    private static final int ENEMY_COLOR = 0;

    /** [Сторона, чей ход либо фигура][откуда][куда] */
    private final int[][][] moveHistory = new int[2][64][64];
    /** [Сторона, чей ход либо фигура][откуда][куда] */
    private final int[][][] butterfly = new int[2][64][64];

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    /** @return true, если ход не атакующий */
    abstract boolean isNonCapture(Move move);

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int negascout(final boolean isMyMove, int alfa, final int beta, final int depth) {

        final List<Move> allMoves = getLegalMovesFor(isMyMove);
        if (depth <= 0 || isTerminalNode(currNode))
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        /*------------ Сортировка ходов ------------*/
        MoveSorter.historyHeuristicSort(allMoves, moveHistory, isMyMove ? MY_COLOR : ENEMY_COLOR);

        final ListIterator<Move> it = allMoves.listIterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);
        int value = -negascout(!isMyMove, -beta, -alfa, depth - 1);
        if (alfa < value) alfa = value; // PV-node
        undoMove(move);

        while (it.hasNext()) {

            /*------------ Butterfly Heuristic ------------*/
            /*if (isNonCapture(move)) {
                final int side2move = isMyMove ? MY_COLOR : ENEMY_COLOR;
                butterfly[side2move][move.from][move.to] += depth * depth;
                // Либо используют 2 в степени depth:
                // butterfly[side2move][move.from][move.to] += 1 << depth;
            }*/

            if (beta <= value) { // fail-high

                /*------------ History Heuristic ------------*/
                /*if (isNonCapture(move)) {
                    final int side2move = isMyMove ? MY_COLOR : ENEMY_COLOR;

                    moveHistory[side2move][move.from][move.to] += depth * depth;
                    // Либо используют 2 в степени depth:
                    // moveHistory[side2move][move.from][move.to] += 1 << depth;
                }*/

                return beta;
            }

            /*------------ Relative History Heuristic ------------*/
            /*if (beta <= value) { // fail-high
                // тут должна быть обычная эвристика истории
                return beta;
            } else {
                // тут должна быть эвристика бабочки
            }*/
            /*------------ Альтернативный Relative History Heuristic ------------*/
            /*if (beta <= value) { // fail-high
                final int side2move = isMyMove ? MY_COLOR : ENEMY_COLOR;

                // эвристика истории:
                if (isNonCapture(move)) {
                    moveHistory[side2move][move.from][move.to] += depth * depth;
                }

                // эвристика бабочки:
                while (it.hasPrevious()) {
                    move = it.previous();
                    if (isNonCapture(move)) {
                        butterfly[side2move][move.from][move.to] += depth * depth;
                    }
                }
                return beta;
            }*/

            move = it.next();
            makeMove(move);

            // null-window search:
            value = -negascout(!isMyMove, -alfa - 1, -alfa, depth - 1);

            // PVS
            if (alfa < value && value < beta) {
                value = -negascout(!isMyMove, -beta, -alfa, depth - 1);
            }

            undoMove(move);
            if (alfa < value) alfa = value; // PV-node
        }

        return alfa;
    }

    /*------------ Точка запуска алгоритма поиска ------------*/

    Move getNextBestMove() {
        final boolean isMyMove = true;
        final List<Move> allMoves = getLegalMovesFor(isMyMove);

        if (isTerminalNode(currNode)) throw new RuntimeException("Ходов нет, игра окончена");

        /*------------ Сортировка ходов ------------*/

        MoveSorter.historyHeuristicSort(allMoves, moveHistory, MY_COLOR);

        /*------------ Далее обычный поиск ------------*/

        int bestValue = MIN_VALUE;
        Move bestMove = allMoves.get(0);

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
