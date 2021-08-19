package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;
import java.util.Iterator;

public abstract class _8_EnhancedForwardPruning extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    /**
     * Улучшенный алгоритм с прямым отсечением
     *
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int EFP(
            final boolean isMyMove,
            int alfa,
            final int beta,
            final int depth,
            final NodeType nodeType) {

        /*------------ Поиск в ТТ ------------*/

        // ...

        /*------------ Получение ходов ------------*/

        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);
        if (depth <= 0 || isTerminalNode(currNode))
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        if (nodeType != NodeType.PV_NODE) {

            /*------------ Любой алгоритм, реализующий Forward Pruning ------------*/

            // ...
            // Он обязательно должен возвращать бету (fail-high), чтобы предотвратить то, что
            // резервное значение передней обрезанной All-node вызывает отсечение на PV-node,
            // расположенной выше
            // ...
        }

        /*------------ PVS ------------*/

        final Iterator<Move> it = allMoves.iterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);

        NodeType nextNodeType = NodeType.PV_NODE;
        if (nodeType == NodeType.CUT_NODE) nextNodeType = NodeType.ALL_NODE;
        else if (nodeType == NodeType.ALL_NODE) nextNodeType = NodeType.CUT_NODE;

        int best = -EFP(!isMyMove, -beta, -alfa, depth - 1, nextNodeType);

        if (beta <= best) { // Cut-node

            /*------------ Кладем в ТТ результат value ------------*/
            // ...

            return best; // fail-soft
            // return beta; // fail-high
        }
        undoMove(move);

        while (it.hasNext()) {
            move = it.next();

            if (alfa < best) alfa = best;

            makeMove(move);

            // null-window search:
            int value =
                    -EFP(
                            !isMyMove,
                            -alfa - 1,
                            -alfa,
                            depth - 1,
                            nodeType == NodeType.CUT_NODE ? NodeType.ALL_NODE : NodeType.CUT_NODE);

            // PVS
            if (alfa < value && value < beta
                    // Если окно PV-node уже было закрыто (beta <= value), и PVS должен возвращать
                    // значение, равное бете (альфа + 1), нам все равно придется выполнить повторный
                    // поиск
                    || nodeType == NodeType.PV_NODE && value == beta && beta == alfa + 1) {
                // value не является реальной нижней границей
                if (value == alfa + 1) {
                    // Если мы проведем повторный поиск и возвращаемое значение PVS будет равно
                    // альфа + 1, мы должны выполнить повторный поиск с альфой в качестве нижней
                    // границы
                    value = alfa;
                }
                value = -EFP(!isMyMove, -beta, -value, depth - 1, nodeType);
            }

            undoMove(move);

            if (best < value) {
                best = value;
                if (beta <= best) { // Cut-node

                    /*------------ Кладем в ТТ результат value ------------*/
                    // ...

                    return best; // fail-soft
                    // return beta; // fail-high
                }
            }
        }

        // Cut-node, в которых произошел сбой со значением, равным альфа, не сохраняются в ТТ,
        // т.к. их значения неопределенные
        if (nodeType == NodeType.CUT_NODE && best == alfa) return best;

        /*------------ Кладем в ТТ результат value ------------*/
        // ...

        return best;
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
            final int value = -EFP(!isMyMove, MIN_VALUE, MAX_VALUE, MAX_DEPTH, NodeType.PV_NODE);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
