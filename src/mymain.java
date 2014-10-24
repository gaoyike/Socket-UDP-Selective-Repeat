/**
 * Created by chenguanghe on 10/5/14.
 */
public class mymain {
    public static void main(String[] args) {
        String t = "Checksum: 2363Seq: 7Data:!be433cbd-d961-429c-9846-b869f9b87298";
        byte[] data = t.split("Data: ")[1].getBytes();
    }
}
