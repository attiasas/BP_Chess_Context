package chess;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import chess.schema.Cell;

import java.util.Objects;

/**
 * Created By: Assaf, On 21/02/2020
 * Description:
 */
public class Move extends BEvent
{
    public final Cell source;
    public final Cell target;

    public Move(Cell source, Cell target)
    {
        super("Move(" + source + " -> " + target + " : " + source.piece + ")");
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return source.equals(move.source) &&
                target.equals(move.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source, target);
    }
}
