package chess;

import chess.DAL.EffectFunction.Move;
import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.NativeArray;
import chess.DAL.schema.Cell;
import chess.DAL.schema.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created By: Assaf, On 16/02/2020
 * Description:
 */
public class UCI implements Runnable
{
    private BProgram program;
    private Cell[][] board;

    @Override
    public void run()
    {
        ContextService contextService = ContextService.getInstance();
        contextService.initFromResources("ContextDB","populateDB.js", "game_rules_context.js");

        contextService.addListener(new Move());

        contextService.addListener(new BProgramRunnerListenerAdapter() {
            @Override
            public void eventSelected(BProgram bp, BEvent theEvent)
            {
                if(theEvent.name.equals("Done Populate"))
                {
                    NativeArray currentBoard =(NativeArray)theEvent.getData();
                    board = new Cell[currentBoard.size()][currentBoard.size()];

                    for(int row = 0; row < currentBoard.size(); row++)
                    {
                        for(int column = 0; column < ((NativeArray)currentBoard.get(row)).size(); column++)
                        {
                            board[row][column] = (Cell)((NativeArray)currentBoard.get(row)).get(column);
                        }
                    }

                    //ready = true;
                    //moveCount = 0;
                }
                else if(theEvent.name.equals("Move"))
                {
                    Map<String, Cell> data = (Map<String, Cell>) theEvent.maybeData;
                    // normal
                    board[data.get("target").row][data.get("target").col].piece = board[data.get("source").row][data.get("source").col].piece;
                    board[data.get("source").row][data.get("source").col].piece = null;
                    print();
                }
            }
        });

        this.program = contextService.getBProgram();
        this.program.setWaitForExternalEvents(true);

        contextService.run();

        playing();

        contextService.close();
    }

    public void playing()
    {
        Scanner scanner = new Scanner(System.in);
        String line;
        String normalStart = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

        while (true)
        {
            line = scanner.nextLine();

            if(line.equals("quit")) break;
            else if(line.equals("print")) print();
            else if(line.startsWith("position")) playMove(line);
            else if(line.equals("start")) this.program.enqueueExternalEvent(new BEvent("ParseFen",normalStart));
        }
    }

    public void playMove(String input)
    {
        String[] tokens = input.split(" ");
        if(tokens.length <= 1) return;

        String lastToken = tokens[tokens.length - 1];

        Cell source = board[lastToken.charAt(1) - '1'][lastToken.charAt(0) - 'a'];
        Cell target = board[lastToken.charAt(3) - '1'][lastToken.charAt(2) - 'a'];

        BEvent move = new BEvent("Move",new HashMap<>() {{
            put("source", source);
            put("target", target);
        }});

        this.program.enqueueExternalEvent(move);
    }

    public void print()
    {
        if(board == null) return;

        String s = "";

        for(int row = board.length - 1; row >= 0 ; row--)
        {
            for (int column = 0; column < board[row].length; column++)
            {
                if(column == 0) s += (row + 1) + " |";
                else s += "|";

                if (board[row][column].piece != null)
                {
                    switch (board[row][column].piece.type)
                    {
                        case Pawn: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "w|" : "l|"); break;
                        case Knight: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "n|" : "N|"); break;
                        case Bishop: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "b|" : "B|"); break;
                        case Rook: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "r|" : "R|"); break;
                        case Queen: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "q|" : "Q|"); break;
                        case King: s += (board[row][column].piece.color.equals(Piece.Color.White) ? "k|" : "K|"); break;
                    }
                }
                else s += " |";

                if(column == (board[row].length - 1)) s += " " + (row + 1);
            }
            s += "\n";
        }

        s += "  ";
        for(char pChar = 'a'; pChar < 'i'; pChar++) s += " " + pChar + " ";

        System.out.println(s);
    }
}
