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
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String network = sc.nextLine();
            String mask = sc.nextLine();
            System.out.println(calculateSegment(network, mask));
        }
    }
}
