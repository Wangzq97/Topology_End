package com.example.topology_end;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Configfile {
	Map<String, MethodStrategy> map = new HashMap<>();
	List<List> acommands;
	List<List> bcommands;
	List<List> ccommands;
	
	public Configfile() {
		initList(acommands);
		initList(bcommands);
		initList(ccommands);
		

		//
		//
		//
	}
	
	public static void main(String[] args) {
		Configfile c = new Configfile();
		c.filesplit("1\n1");
	}
	
	private void initList(List<List> l) {
		l = new ArrayList<List>();
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
		l.add(new ArrayList<String>());
	}
	
	public String filesplit(String data) {
		Logger logger = LoggerFactory.getLogger(Configfile.class);
		logger.info("configuration");
		JSONObject result = new JSONObject();
		//ip,password?
		String[] commands = data.split("\n");
		if(commands.length == 0) {
			result.put("state", false);
            String msg = "empty file!!!";
            result.put("msg", msg);
            logger.info(msg);
            return result.toJSONString();
		}
		for(String command : commands) {
			if(command.equals("")) continue;
			else {
				String[] command1 = command.split("=");
				if(command1.length != 2 && command != "Apache") {
					result.put("state", false);
	                String msg = "wrong command:" + command;
	                result.put("msg", msg);
	                logger.info(msg);
	                return result.toJSONString();
				}
				else if(command == "Apache") continue;
				
				String left = command1[0];
				String[] head = left.split(".");
				if(head.length != 2) {
					result.put("state", false);
					String msg = "wrong command:" + command;
		            result.put("msg", msg);
		            logger.info(msg);
		            return result.toJSONString();
				}
				String Router = head[0];
				String methodname = head[1];
				int judge = judgeRouter(Router);
				if(judge == 1) {
					insertArg(methodname, command1[1], acommands);
				}
				else if(judge == 2) {
					insertArg(methodname, command1[1], bcommands);
				}
				else if(judge == 3) {
					insertArg(methodname, command1[1], ccommands);
				}
				else {
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
		return "";
	}

private void executeCommands() {
	execute("r0",acommands);
	execute("r1",bcommands);
	execute("r2",ccommands);
}

private void execute(String router,List<List> list) {
	List<String> temp = list.get(0);
	String ip = temp.get(0);
	temp = list.get(1);
	String password = temp.get(0);
	TopologyEndApplication.telnet_controller.telnet_login(router, ip, password);
	temp = list.get(2);
	List<String> serialiplist = new ArrayList<>();
	List<String> serialmasklist = new ArrayList<>();
	List<String> loiplist = new ArrayList<>();
	List<String> lomasklist = new ArrayList<>();
	for(String portcommand : temp) {
		String[] ports = portcommand.split(":");
		if(ports[0].contains("s0/0/")) {
			String[] lll = portcommand.substring(7).split(" ");
			serialiplist.add(lll[0]);
			if(lll.length != 1)
				serialmasklist.add(lll[1]);
			else
				serialmasklist.add("0");
		}
		else {
			String[] lll = portcommand.substring(4).split(" ");
			loiplist.add(lll[0]);
			if(lll.length != 1)
				lomasklist.add(lll[1]);
			else
				lomasklist.add("0");
		}
	}
	if(serialiplist.size() != 0)
		TopologyEndApplication.telnet_controller.init_serial(router, (String[])serialiplist.toArray(), (String[])serialmasklist.toArray());
	for(int i = 0; i < loiplist.size(); ++i) {
		TopologyEndApplication.telnet_controller.init_loopback(router, String.valueOf(i), loiplist.get(i), lomasklist.get(i));
	}
	temp = list.get(3);
	List<String> networklist = new ArrayList<>();
	List<String> masklist = new ArrayList<>();
	List<String> targetlist = new ArrayList<>();
	List<String> arealist = new ArrayList<>();
	String protocol = temp.get(0).split(" ")[1];
	for(int i = 1; i < temp.size(); ++i) {
		String protocolcommand = temp.get(i);
		if(!protocolcommand.contains(protocol)) {
			String[] strs = protocolcommand.split(" ");
			networklist.add(strs[1]);
			masklist.add(strs[2]);
			if(strs.length == 4) targetlist.add(strs[3]);
			else if(strs.length == 5) arealist.add(strs[4]);
		}
	}
	if(protocol == "ospf") TopologyEndApplication.telnet_controller.config_ospf(router, (String[])networklist.toArray(), (String[])masklist.toArray(), (String[])arealist.toArray());
	else if(protocol == "rip") TopologyEndApplication.telnet_controller.config_rip(router, (String[])networklist.toArray(), (String[])masklist.toArray());
	else TopologyEndApplication.telnet_controller.config_static(router, (String[])networklist.toArray(), (String[])masklist.toArray(), (String[])targetlist.toArray());
	temp = list.get(4);
	for(String s : temp) TopologyEndApplication.telnet_controller.ping(router, s);
	temp = list.get(5);
	for(int i = 0; i < temp.size(); ++i) TopologyEndApplication.telnet_controller.get_info(router);
	temp = list.get(6);
	
}

private int judgeRouter(String router) {
	if(router.equals("RouterA")) return 1;
	else if(router.equals("RouterB")) return 2;
	else if(router.equals("RouterC")) return 3;
	else return -1;
}

private void insertArg(String methodname, String Arg, List<List> list) {
	String[] Args = Arg.split(",");
	if(methodname.equals("ip")) {
		list.get(0).add(Args[0]);
	}
	else if(methodname.equals("password")) {
		list.get(1).add(Args[0]);
	}
	else if(methodname.equals("port")) {
		for(int i=0;i<Args.length;i++)list.get(2).add(Args[i]);
	}
	else if(methodname.equals("command")) {
		for(int i=0;i<Args.length;i++)list.get(3).add(Args[i]);
	}
	else if(methodname.equals("ping")) {
		for(int i=0;i<Args.length;i++)list.get(4).add(Args[i]);
	}
	else if(methodname.equals("show")) {
		for(int i=0;i<Args.length;i++)list.get(5).add(Args[i]);
	}
	else if(methodname.equals("showtest")) {
		for(int i=0;i<Args.length;i++)list.get(6).add(Args[i]);
	}
}
}
interface MethodStrategy{
	public String execute(String router, String args);
}

class PingMethod implements MethodStrategy{

	@Override
	public String execute(String router, String args) {
		
		return null;
	}
	
}
