/**
 * Test main method
 * Name: Chenguang He
 * Email: readman@iastate.edu
 * Created by chenguanghe on 10/4/14.
 */

import java.util.UUID;

public class TestMain {
    public static void main(String[] args) throws Exception {
        rdtServices.AddParties(0, 0, 5, 4000); // no loss no corrupt
        rdtServices.AddParties((float) 0.2, (float) 0.3, 3, 4000);   // some loss some corruput
        rdtServices.AddParties((float) 0, (float) 0.5, 4, 4000); // no loss, only corrupt
        rdtServices.AddParties((float) 0.5, (float) 0, 2, 4000); // no corrupt, only loss
        int times = 5;
        for (int i = 0; i < times; i++) {
            String s = UUID.randomUUID().toString();
            char[] chars = s.toCharArray();
            rdtServices.RdtSend(1, chars, chars.length, 0);
            rdtServices.RdtSend(2, chars, chars.length, 0);
            rdtServices.RdtSend(1, chars, chars.length, 0);

        }
        rdtServices.RdtStats(1, 1);
        rdtServices.RdtStats(2, 2);
    }
}
