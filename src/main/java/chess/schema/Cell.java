package chess.schema;

import javax.persistence.*;

/**
 * Created By: Assaf, On 17/02/2020
 * Description:
 */
@Entity
@NamedQueries(value = {
        @NamedQuery(name = "Cell", query = "SELECT c FROM Cell c"),
        @NamedQuery(name = "EmptyCell", query = "SELECT c FROM Cell c WHERE c.piece IS NULL"),
        @NamedQuery(name = "NotEmptyCell", query = "SELECT c FROM Cell c WHERE c.piece IS NOT NULL"),
        @NamedQuery(name = "UpdateCell", query = "Update Cell c set c.piece=:piece where c=:cell"),
        @NamedQuery(name = "SpecificCell", query = "SELECT c FROM Cell c WHERE c.row=:row AND c.col=:col"),
        // -----------------------------------------
        @NamedQuery(name = "PieceCell", query = "SELECT c FROM Cell c WHERE c.piece = :piece"),
        @NamedQuery(name = "PawnCell", query = "SELECT c FROM Cell c WHERE c.piece IS NOT NULL AND c.piece.type = 'Pawn'")
})
public class Cell extends BaseEntity
{
    @Column
    public final int row;
    @Column
    public final int col;
    @OneToOne(cascade = CascadeType.MERGE)
    public Piece piece;

    public Cell()
    {
        super();
        row = -1;
        col = -1;
        this.piece = null;
    }

    public Cell(int row, int col)
    {
        super("Cell[" + row + "," + col + "]");
        this.row = row;
        this.col = col;
        this.piece = null;
    }
}
