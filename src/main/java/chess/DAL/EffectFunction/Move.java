package chess.DAL.EffectFunction;

import chess.DAL.schema.Cell;
import chess.DAL.schema.Piece;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created By: Assaf, On 26/02/2020
 * Description:
 */
public class Move extends BProgramRunnerListenerAdapter
{
    @Override
    public void eventSelected(BProgram bp, BEvent e) {

        if(e.name.equals("Move")) {
            System.out.println("here");
            Map<String, Cell> data = (Map<String, Cell>) e.maybeData;
            Cell source = data.get("source");
            Cell target = data.get("target");
            Piece targetPiece = target.piece;
            List<ContextService.CommandEvent> transaction = new ArrayList<>(){{
                new ContextService.UpdateEvent("UpdateCell",new HashMap<>() {{
                    put("piece", source.piece);
                    put("cell", target);
                }});
                new ContextService.UpdateEvent("UpdateCell",new HashMap<>() {{
                    put("piece", null);
                    put("cell", source);
                }});
            }};
            if(targetPiece != null) {
                transaction.add(new ContextService.UpdateEvent("RemovePiece",new HashMap<>() {{
                    put("piece", targetPiece);
                }}));
            }

            new ContextService.TransactionEvent(transaction.toArray(new ContextService.CommandEvent[]{})).execute();
        }
    }
}
