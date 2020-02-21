import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.Scanner;

/**
 * Created By: Assaf, On 16/02/2020
 * Description:
 */
public class UCI extends Thread
{
    BProgram program;

    @Override
    public void run()
    {
        ContextService contextService = ContextService.getInstance();
        contextService.initFromResources("ContextDB","populateDB.js");

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
        String normalStart = /*"8/8/6pp/8/8/8/8/8";*/"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

        while (true)
        {
            line = scanner.nextLine();

            if(line.equals("quit")) break;
            else if(line.equals("print")) print();
            else if(line.equals("start")) this.program.enqueueExternalEvent(new BEvent("ParseFen",normalStart));
        }
    }

    public void print()
    {

    }
}
