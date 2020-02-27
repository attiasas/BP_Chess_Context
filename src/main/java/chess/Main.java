package chess;

/**
 * Created By: Assaf, On 14/02/2020
 * Description:
 */
public class Main
{
    public static void main(String[] args) throws InterruptedException {
        UCI uci = new UCI();

        Thread t = new Thread(uci);
        t.start();
        t.join();
    }
}
