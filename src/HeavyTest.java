import java.util.UUID;

/**
 * Heavy test gives a very big tasks for my program.
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 10/5/14.
 */
public class HeavyTest {
    public static void main(String[] args) throws Exception {
        rdtServices.AddParties(0, 0, 5, 4000); // no loss no corrupt
        rdtServices.AddParties((float) 0.2, (float) 0.3, 3, 4000);
        rdtServices.AddParties((float) 0, (float) 0.5, 4, 4000);
        rdtServices.AddParties((float) 0.5, (float) 0, 2, 4000);
        rdtServices.AddParties((float)0.4, (float)0.6, 5, 4000);
        rdtServices.AddParties((float) 0.2, (float) 0.3, 3, 4000);
        rdtServices.AddParties((float) 0, (float) 0.5, 4, 4000);
        rdtServices.AddParties((float) 0.5, (float) 0, 2, 4000);
        rdtServices.AddParties((float) 0.4, (float)0, 5, 4000);
        rdtServices.AddParties((float) 0.2, (float) 0.3, 3, 4000);
        rdtServices.AddParties((float) 0.2, (float) 0.5, 4, 4000);
        rdtServices.AddParties((float) 0.5, (float) 0, 2, 4000);
        int times = 30;
        for (int i = 0; i < times; i++) {
            String s = UUID.randomUUID().toString();
            char[] chars = s.toCharArray();
            rdtServices.RdtSend(1, chars, chars.length, 0);
            rdtServices.RdtSend(2, chars, chars.length, 0);
            rdtServices.RdtSend(3, chars, chars.length, 0);
            rdtServices.RdtSend(4, chars, chars.length, 0);
            rdtServices.RdtSend(5, chars, chars.length, 0);
            rdtServices.RdtSend(6, chars, chars.length, 0);
            rdtServices.RdtSend(7, chars, chars.length, 0);
            rdtServices.RdtSend(8, chars, chars.length, 0);
            rdtServices.RdtSend(9, chars, chars.length, 0);
            rdtServices.RdtSend(10, chars, chars.length, 0);
            rdtServices.RdtSend(11, chars, chars.length, 0);
            rdtServices.RdtSend(12, chars, chars.length, 0);
        }
        rdtServices.RdtStats(1, 1);
        rdtServices.RdtStats(2, 2);
        rdtServices.RdtStats(3, 3);
        rdtServices.RdtStats(4, 4);
        rdtServices.RdtStats(5, 1);
        rdtServices.RdtStats(6, 2);
        rdtServices.RdtStats(7, 3);
        rdtServices.RdtStats(8, 4);
        rdtServices.RdtStats(9, 1);
        rdtServices.RdtStats(10, 2);
        rdtServices.RdtStats(11, 3);
        rdtServices.RdtStats(12, 4);

    }
}
