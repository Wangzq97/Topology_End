import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class test {
    public static String calculateSegment(String network, String mask) {
        StringBuilder segment = new StringBuilder();
        String[] network_list = network.split("\\.");
        String[] mask_list = mask.split("\\.");
        for (int i = 0; i < network_list.length; i++) {
            int seg = Integer.parseInt(network_list[i]) & Integer.parseInt(mask_list[i]);
            System.out.println(seg);

            if (i > 0) {
                segment.append(".");
            }
            segment.append(seg);
        }
        return String.valueOf(segment);
    }

    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        String[] s_array = new String[5];
//        List<String> s_list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            s_array[i] = "string num " + i;
//            s_list.add("string num " + i);
//        }
//        JSONObject o = new JSONObject();
//        o.put("s_array", s_array);
//        o.put("s_list", s_list);
//        o.put("ip", "192.168.0.1/24");
////        System.out.println(o.toJSONString());
//        JSONObject result = new JSONObject();
//        result.put("o",o.toJSONString());
//        System.out.println(result.toJSONString());
        String s = "xxx [11/22] yyy";
        s=s.replaceAll(" \\[.*\\] ", " ");
        System.out.println(s);

//        String info = "Router#show interface FastEthernet0/0\n" +
//                "FastEthernet0/0 is administratively down, line protocol is down (disabled)\n" +
//                "  Hardware is Lance, address is 000d.bdba.e401 (bia 000d.bdba.e401)\n" +
//                "  MTU 1500 bytes, BW 100000 Kbit, DLY 100 usec,\n" +
//                "     reliability 255/255, txload 1/255, rxload 1/255\n" +
//                "  Encapsulation ARPA, loopback not set\n" +
//                "  ARP type: ARPA, ARP Timeout 04:00:00, \n" +
//                "  Last input 00:00:08, output 00:00:05, output hang never\n" +
//                "  Last clearing of \"show interface\" counters never\n" +
//                "  Input queue: 0/75/0 (size/max/drops); Total output drops: 0\n" +
//                "  Queueing strategy: fifo\n" +
//                "  Output queue :0/40 (size/max)\n" +
//                "  5 minute input rate 0 bits/sec, 0 packets/sec\n" +
//                "  5 minute output rate 0 bits/sec, 0 packets/sec\n" +
//                "     0 packets input, 0 bytes, 0 no buffer\n" +
//                "     Received 0 broadcasts, 0 runts, 0 giants, 0 throttles\n" +
//                "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored, 0 abort\n" +
//                "     0 input packets with dribble condition detected\n" +
//                "     0 packets output, 0 bytes, 0 underruns\n" +
//                "     0 output errors, 0 collisions, 1 interface resets\n" +
//                "     0 babbles, 0 late collision, 0 deferred\n" +
//                "     0 lost carrier, 0 no carrier\n" +
//                "     0 output buffer failures, 0 output buffers swapped out";
//
//        String[] info_list = info.split("\n");
//
//        JSONObject result = new JSONObject();
//        result.put("link_state", info.contains("line protocol is up"));
//        result.put("ip", "");
//        for (String s : info_list) {
//            if (s.contains("Internet address is ")) {
//                int index = s.indexOf("Internet address is ");
//                String ip = s.substring(index + "Internet address is ".length());
//                result.put("ip", ip);
//            }
//        }
//
//        System.out.println(result.toJSONString());
    }
}
