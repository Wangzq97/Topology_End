package com.example.topology_end;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;


public class telnetClient {
    private String name = "default";        //设备名称
    private String ip = "127.0.0.1";        //设备ip
    private String mask = "24";             //设备掩码
    private String password = "cisco";      //密码

    private int state = -1;                 //路由器模式
    private List<String> static_route_list; //已有的静态路由命令表

    private String prompt = "#";        //结束标识字符串,Windows中是>,Linux中是#
    private char promptChar = '>';        //结束标识字符
    private TelnetClient telnet;
    private InputStream in;                // 输入流,接收返回信息
    private PrintStream out;        // 向服务器写入 命令

    /**
     * @param termtype 协议类型：VT100、VT52、VT220、VTNT、ANSI
     * @param prompt   结果结束标识
     */
    public telnetClient(String termtype, String prompt) {
        telnet = new TelnetClient(termtype);
        setPrompt(prompt);
    }

    public telnetClient(String termtype) {
        telnet = new TelnetClient(termtype);
    }

    public telnetClient() {
        telnet = new TelnetClient();
    }

    /**
     * 登录到目标主机
     *
     * @param ip
     * @param port
     * @param password
     */
    public Boolean login(String ip, int port, String password) {
        try {
            telnet.connect(ip, port);
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
            readUntil("Password:");
            write(password);
            readUntil(">");
            write("enable");
            readUntil("Password:");
            write(password);
            Boolean result = readUntil("#") != null;
            if (result) {
                this.ip = ip;
                state = 0;
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 退回到特权模式
     */
    public boolean back_to_enable() {
        int max_time = 10;
        while (max_time > 0 && state != 0) {
            sendCommand("exit");
            max_time--;
        }
        return state == 0;
    }


    /**
     * 设置串行接口
     *
     * @param port      0或1,S口的哪一个端口
     * @param serial_ip 串行接口的ip
     * @param mask      串行接口的子网掩码
     */
    public BooleanResult initSerial(int port, String serial_ip, String mask) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }

        if (serial_ip.length() == 0 || serial_ip.equals("0.0.0.0")) {
            return new BooleanResult(true, "empty serial ip");
        }
        StringBuilder info = new StringBuilder();
        info.append(sendCommand("configure terminal"));
        info.append("\n");
        info.append(sendCommand("interface s0/0/" + port));
        info.append("\n");
        info.append(sendCommand("ip address " + serial_ip + " " + mask));
        info.append("\n");
        info.append(sendCommand("clock rate 64000"));
        info.append("\n");
        info.append(sendCommand("no shutdown"));
        info.append("\n");
        info.append(sendCommand("exit"));
        info.append(sendCommand("exit"));
        return new BooleanResult(true, String.valueOf(info));
    }


    /**
     * 设置回环接口
     *
     * @param port        回环接口的端口
     * @param loopback_ip 回环接口的ip
     * @param mask        回环接口的子网掩码
     */
    public BooleanResult initLoopback(String port, String loopback_ip, String mask) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }

        StringBuilder info = new StringBuilder();
        info.append(sendCommand("configure terminal"));
        info.append("\n");
        info.append(sendCommand("interface loopback" + port));
        info.append("\n");
        info.append(sendCommand("ip address " + loopback_ip + " " + mask));
        info.append("\n");
        info.append(sendCommand("no shutdown"));
        info.append("\n");
        info.append(sendCommand("exit"));
        info.append(sendCommand("exit"));
        return new BooleanResult(true, String.valueOf(info));
    }


    /**
     * ping
     *
     * @param ip 要ping的ip地址
     */
    public BooleanResult ping(String ip) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }
        StringBuilder info = new StringBuilder();
        info.append(sendCommand("ping " + ip));
        String result = String.valueOf(info);
        if (result.contains("!!!!!")) {
            return new BooleanResult(true, String.valueOf(info));
        } else {
            return new BooleanResult(false, String.valueOf(info));
        }

    }


    /**
     * 配置静态路由
     *
     * @param network   目的网段的ip
     * @param mask      目的网段的子网掩码
     * @param target_ip 下一跳ip地址
     */
    public BooleanResult configStatic(String network, String mask, String target_ip) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }

        if (network.length() == 0 || network.equals("0.0.0.0")) {
            return new BooleanResult(true, "empty network ip");
        }
        StringBuilder info = new StringBuilder();
        String segment = calculateSegment(network, mask);
        String negative_mask = calculateNegativeMask(mask);
        info.append(sendCommand("configure terminal"));
        info.append("\n");
        String command = "ip route " + segment + " " + mask + " " + target_ip;
        info.append(sendCommand(command));
        this.static_route_list.add(command);
        info.append("\n");
        info.append(sendCommand("exit"));
        return new BooleanResult(true, String.valueOf(info));
    }


    /**
     * 配置RIP协议
     *
     * @param network 网段的ip
     * @param mask    网段的子网掩码
     */
    public BooleanResult configRip(String network, String mask) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }

        if (network.length() == 0 || network.equals("0.0.0.0")) {
            return new BooleanResult(true, "empty network ip");
        }
        StringBuilder info = new StringBuilder();
        info.append(sendCommand("configure terminal"));
        info.append("\n");
        info.append(sendCommand("router rip"));
        info.append("\n");
        String segment = calculateSegment(network, mask);
        info.append(sendCommand("network " + segment));
        info.append("\n");
        info.append(sendCommand("exit"));
        info.append(sendCommand("exit"));
        return new BooleanResult(true, String.valueOf(info));
    }

    /**
     * 配置OSPF协议
     *
     * @param network 网段的ip
     * @param mask    网段的子网掩码
     * @param area    网段的区域
     */
    public BooleanResult configOspf(String network, String mask, String area) {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }

        if (network.length() == 0 || network.equals("0.0.0.0")) {
            return new BooleanResult(true, "empty network ip");
        }
        StringBuilder info = new StringBuilder();
        info.append(sendCommand("configure terminal"));
        info.append("\n");
        info.append(sendCommand("router ospf 1"));
        info.append("\n");
        String segment = calculateSegment(network, mask);
        String negative_mask = calculateNegativeMask(mask);
        info.append(sendCommand("network " + segment + ' ' + negative_mask + " area " + area));
        info.append("\n");
        info.append(sendCommand("exit"));
        info.append(sendCommand("exit"));
        return new BooleanResult(true, String.valueOf(info));
    }


    /**
     * 清空路由协议
     */
    public BooleanResult clear_router() {
        if (out == null) {
            return new BooleanResult(false, "Device not login.");
        }
        StringBuilder info = new StringBuilder();
        String router_info = sendCommand("show ip protocols");

        if (router_info.contains("rip")) {
            //clear RIP
            info.append(sendCommand("configure terminal"));
            info.append(sendCommand("no router rip"));
            info.append(sendCommand("exit"));
        }

        if (router_info.contains("ospf")) {
            //clear OSPF
            info.append(sendCommand("configure terminal"));
            info.append(sendCommand("no router ospf 1"));
            info.append(sendCommand("exit"));
        }

        //clear static route
        info.append(sendCommand("configure terminal"));
        for (String command : static_route_list) {
            info.append(sendCommand("no " + command));
        }
        info.append(sendCommand("exit"));
        static_route_list.clear();

        return new BooleanResult(true, String.valueOf(info));
    }


    /**
     * 读取分析结果
     *
     * @param pattern 匹配到该字符串时返回结果
     * @return
     */
    public String readUntil(String pattern) {
        StringBuffer sb = new StringBuffer();
        try {
            char lastChar = (char) -1;
            boolean flag = pattern != null && pattern.length() > 0;
            if (flag)
                lastChar = pattern.charAt(pattern.length() - 1);
            char ch;
            int code = -1;
            while ((code = in.read()) != -1) {
                ch = (char) code;
                sb.append(ch);
                //匹配到结束标识时返回结果
                if (flag) {
                    if (ch == lastChar && sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                } else {
                    //如果没指定结束标识,匹配到默认结束标识字符时返回结果
                    if (ch == promptChar)
                        return sb.toString();
                }
                //登录失败时返回结果
                if (sb.toString().contains("Login Failed")) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 发送命令
     *
     * @param value
     */
    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送命令,返回执行结果
     *
     * @param command
     * @return
     */
    public String sendCommand(String command) {
        try {
            write(command);
            //            result = new String(result.getBytes("ISO_8859_1"), "GBK");        //转一下编码
            String result = readUntil(prompt);
            if (result.contains("(config)")) {
                state = 1;
            } else if (result.contains("(config-if)")) {
                state = 2;
            } else {
                state = 0;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭连接
     */
    public void distinct() {
        try {
            if (telnet != null && !telnet.isConnected())
                telnet.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPrompt(String prompt) {
        if (prompt != null) {
            this.prompt = prompt;
            this.promptChar = prompt.charAt(prompt.length() - 1);
        }
    }


    /**
     * 计算网段
     *
     * @param network 网段的ip
     * @param mask    网段的子网掩码
     */
    public static String calculateSegment(String network, String mask) {
        StringBuilder segment = new StringBuilder();
        String[] network_list = network.split("\\.");
        String[] mask_list = mask.split("\\.");
        for (int i = 0; i < network_list.length; i++) {
            int seg = Integer.parseInt(network_list[i]) & Integer.parseInt(mask_list[i]);
            if (i > 0) {
                segment.append(".");
            }
            segment.append(seg);
        }
        return String.valueOf(segment);
    }


    /**
     * 计算掩码的反码
     *
     * @param mask 子网掩码
     */
    public static String calculateNegativeMask(String mask) {
        StringBuilder negative_mask = new StringBuilder();
        String[] mask_list = mask.split("\\.");
        for (int i = 0; i < mask_list.length; i++) {
            int seg = 255 - Integer.parseInt(mask_list[i]);
            if (i > 0) {
                negative_mask.append(".");
            }
            negative_mask.append(seg);
        }
        return String.valueOf(negative_mask);
    }

    public static void main(String[] args) {
        telnetClient telnet = new telnetClient("VT220", "#");        //Windows,用VT220,否则会乱码
        if (telnet.login("172.16.0.1", 23, "CISCO")) {
            System.out.println("login");
            String rs = telnet.sendCommand("show ip route");
//            System.out.println(rs);
//            try {
//                rs = new String(rs.getBytes("ISO-8859-1"), "GBK");        //转一下编码
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            System.out.println(rs);
        }
    }
}
