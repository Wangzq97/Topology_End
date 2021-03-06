package com.example.topology_end;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static com.example.topology_end.TopologyEndApplication.telnet_controller;

public class Configfile {
    List<List> acommands = new ArrayList<>();
    List<List> bcommands = new ArrayList<>();
    List<List> ccommands = new ArrayList<>();

    public Configfile() {
        initList(acommands);
        initList(bcommands);
        initList(ccommands);
    }

    private void initList(List<List> l) {
    	l.clear();
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
        l.add(new ArrayList<String>());
    }


    public String filesplit(@RequestBody String data) {
		initList(acommands);
		initList(bcommands);
 		initList(ccommands);
 		
        Logger logger = LoggerFactory.getLogger(Configfile.class);
        logger.info("configuration");
        JSONObject result = new JSONObject();
//      String[] commands = data.split("\r\n");
        String[] commands = data.split("\n");
        if (commands.length == 0) {
            result.put("state", false);
            String msg = "empty file!!!";
            result.put("msg", msg);
            logger.info(msg);
            return result.toJSONString();
        }
        for (String command : commands) {
            if (command.equals("")) {
                continue;
            } else {
                String[] command1 = command.split("=");
                if (command1.length != 2 && !command.equals("Apache")) {
                    result.put("state", false);
                    String msg = "wrong command:" + command;
                    result.put("msg", msg);
                    logger.info(msg);
                    return result.toJSONString();
                } else if (command.equals("Apache")) continue;

                String left = command1[0];
                String[] head = left.split("\\.");
                if (head.length != 2) {
                    result.put("state", false);
                    String msg = "wrong command:" + command;
                    result.put("msg", msg);
                    logger.info(msg);
                    return result.toJSONString();
                }
                String Router = head[0];
                String methodname = head[1];
                int judge = judgeRouter(Router);
                if (judge == 1) {
                    insertArg(methodname, command1[1], acommands);
                } else if (judge == 2) {
                    insertArg(methodname, command1[1], bcommands);
                } else if (judge == 3) {
                    insertArg(methodname, command1[1], ccommands);
                } else {
                    result.put("state", false);
                    String msg = "wrong command:" + command;
                    result.put("msg", msg);
                    logger.info(msg);
                    return result.toJSONString();
                }
                //MethodStrategy method = map.get(methodname);
                //method.execute(Router, command1[1]);
            }
        }
        executeCommands();
        result.put("state", true);
        String msg = "Config success.";
        result.put("msg", msg);
        logger.info(msg);
        return result.toJSONString();
    }

    private void executeCommands() {
        loginconfig("r0",acommands);
        portconfig("r0",acommands);

        loginconfig("r1",bcommands);
        portconfig("r1",bcommands);

        loginconfig("r2",ccommands);
        portconfig("r2",ccommands);

        commandconfig("r0",acommands);
        commandconfig("r1",bcommands);
        commandconfig("r2",ccommands);
        
        execute("r0", acommands);
        execute("r1", bcommands);
        execute("r2", ccommands);
    }
    private void loginconfig(String router, List<List> list)
    {
        List<String> temp = list.get(0);
        String ip = temp.get(0);
        temp = list.get(1);
        String password = temp.get(0);
        telnet_controller.telnet_login(router, ip, password);
    }
    private void portconfig (String router, List<List> list)
    {
        List<String> temp = list.get(2);
        String[] serial_ip_list = {"", ""};
        String[] serial_mask_list = {"0", "0"};
        Boolean is_serial = false;
        List<String> loiplist = new ArrayList<>();
        List<String> lomasklist = new ArrayList<>();
        for (String portcommand : temp) {
            String[] ports = portcommand.split(":");
            if (ports[0].contains("s0/0/0")) {
                is_serial = true;
                String[] lll = portcommand.substring(7).split(" ");
                serial_ip_list[0] = lll[0];
                if (lll.length != 1)
                    serial_mask_list[0] = lll[1];
                else
                    serial_mask_list[0] = ("0");
            } else if (ports[0].contains("s0/0/1")) {
                is_serial = true;
                String[] lll = portcommand.substring(7).split(" ");
                serial_ip_list[1] = lll[0];
                if (lll.length != 1)
                    serial_mask_list[1] = lll[1];
                else
                    serial_mask_list[1] = ("0");
            } else {
                String[] lll = portcommand.substring(4).split(" ");
                loiplist.add(lll[0]);
                if (lll.length != 1)
                    lomasklist.add(lll[1]);
                else
                    lomasklist.add("0");
            }
        }
        if (is_serial)
            telnet_controller.init_serial(router, serial_ip_list, serial_mask_list);
        for (int i = 0; i < loiplist.size(); ++i) {
            telnet_controller.init_loopback(router, String.valueOf(i), loiplist.get(i), lomasklist.get(i));
        }
    }
    private void commandconfig(String router, List<List> list)
    {
        List<String> temp = list.get(3);
        List<String> networklist = new ArrayList<>();
        List<String> masklist = new ArrayList<>();
        List<String> targetlist = new ArrayList<>();
        List<String> arealist = new ArrayList<>();
        String protocol = temp.get(0).split(" ")[1];
        for (int i = 1; i < temp.size(); ++i) {
            String protocolcommand = temp.get(i);
            if (!protocolcommand.contains(protocol)) {
                String[] strs = protocolcommand.split(" ");
                networklist.add(strs[1]);
                masklist.add(strs[2]);
                if (strs.length == 4) targetlist.add(strs[3]);
                else if (strs.length == 5) arealist.add(strs[4]);
            }
        }
        if (protocol.equals("ospf"))telnet_controller.config_ospf(router, networklist.toArray(new String[0]), masklist.toArray(new String[0]), arealist.toArray(new String[0]));
        else if (protocol.equals("rip"))telnet_controller.config_rip(router, networklist.toArray(new String[0]), masklist.toArray(new String[0]));
        else telnet_controller.config_static(router, networklist.toArray(new String[0]), masklist.toArray(new String[0]), targetlist.toArray(new String[0]));
    }
    private void execute(String router, List<List> list) {
        //ping
        List<String> temp = list.get(4);
        for (String s : temp) telnet_controller.ping(router, s);
        //show
        temp = list.get(5);
        for (int i = 0; i < temp.size(); ++i) telnet_controller.get_info(router);
        //showtest
        temp = list.get(6);

    }

    private int judgeRouter(String router) {
        if (router.equals("RouterA")) return 1;
        else if (router.equals("RouterB")) return 2;
        else if (router.equals("RouterC")) return 3;
        else return -1;
    }

    private void insertArg(String methodname, String Arg, List<List> list) {
        String[] Args = Arg.split(",");
        if (methodname.equals("ip")) {
            list.get(0).add(Args[0]);
        } else if (methodname.equals("password")) {
            list.get(1).add(Args[0]);
        } else if (methodname.equals("port")) {
            for (int i = 0; i < Args.length; i++) list.get(2).add(Args[i]);
        } else if (methodname.equals("command")) {
            for (int i = 0; i < Args.length; i++) list.get(3).add(Args[i]);
        } else if (methodname.equals("ping")) {
            for (int i = 0; i < Args.length; i++) list.get(4).add(Args[i]);
        } else if (methodname.equals("show")) {
            for (int i = 0; i < Args.length; i++) list.get(5).add(Args[i]);
        } else if (methodname.equals("showtest")) {
            for (int i = 0; i < Args.length; i++) list.get(6).add(Args[i]);
        }
    }
}
