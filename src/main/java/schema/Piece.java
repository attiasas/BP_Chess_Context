package schema;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.HashMap;

/**
 * Created By: Assaf, On 17/02/2020
 * Description:
 */
@Entity
public class Piece extends BaseEntity
{
    public enum Type
    {
        Pawn,Knight,Bishop,Rook,Queen,King
    }

    public enum Color
    {
        Black,White
    }

    @Enumerated(EnumType.STRING)
    public final Type type;
    @Enumerated(EnumType.STRING)
    public final Color color;
    @Column
    public final int counter;

    private static HashMap<Type,Integer> whiteCounter = new HashMap<>();
    private static HashMap<
            Type,Integer> blackCounter = new HashMap<>();

    public Piece()
    {
        super();
        color = null;
        counter = 0;
        type = null;
    }

    public Piece(Type type, Color color)
    {
        super(type + "-" + color + "-" + ((color.equals(Color.White) ? whiteCounter : blackCounter).get(type) != null ? (color.equals(Color.White) ? whiteCounter : blackCounter).get(type) : 1));

        HashMap<Type,Integer> map = (color.equals(Color.White) ? whiteCounter : blackCounter);
        if(!map.containsKey(type))
        {
            this.counter = 1;
            map.put(type,2);
        }
        else
        {
            this.counter = map.get(type);
            map.replace(type, this.counter + 1);
        }

        this.type = type;
        this.color = color;
    }
}
