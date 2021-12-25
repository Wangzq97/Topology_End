package com.example.topology_end;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class TelnetClientController {
    @RequestMapping(value = "/")
    public String hello() {
        return "动态路由后端，Running on http://localhost:8999/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String telnet_login(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, telnet login");
        JSONObject result = new JSONObject();

        String dev_no = (String) data.get("dev_no");
        String ip = (String) data.get("ip");
        String pwd = (String) data.get("pwd");
        telnetClient device = get_device(dev_no);

        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                Boolean state = device.login(ip, 23, pwd);
                if (!state) {
                    result.put("state", false);
                    String msg = "Login fail, wrong password.";
                    result.put("msg", msg);
                    logger.info(msg);
                } else {
                    result.put("state", true);
                    String msg = dev_no + " login success.";
                    result.put("msg", msg);
                    logger.info(msg);
                }
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public String init_serial(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, init serial");
        JSONObject result = new JSONObject();

        String dev_no = (String) data.get("dev_no");
        JSONArray json_ip_list = data.getJSONArray("ip_list");
        JSONArray json_mask_list = data.getJSONArray("mask_list");

        if (json_ip_list.size() != json_mask_list.size()) {
            result.put("state", false);
            String msg = "POST param error. Ip_list doesn't have the same length as mask_list";
            result.put("msg", msg);
            logger.error(msg);
            return result.toJSONString();
        }
        telnetClient device = get_device(dev_no);

        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                for (int i = 0; i < json_ip_list.size(); i++) {
                    String ip = (String) json_ip_list.get(i);
                    String mask = (String) json_mask_list.get(i);
                    BooleanResult boolean_result = device.initSerial(i, ip, mask);
                    logger.info(boolean_result.string_result);
                    if (!boolean_result.boolean_result) {
                        result.put("state", false);
                        String msg = boolean_result.string_result;
                        result.put("msg", msg);
                        logger.error(msg);
                        return result.toJSONString();
                    }
                }
                result.put("state", true);
                String msg = dev_no + " init serial success.";
                result.put("msg", msg);
                logger.info(msg);
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public String get_info(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, get device info");
        JSONObject result = new JSONObject();

        String dev_no = (String) data.get("dev_no");
        telnetClient device = get_device(dev_no);
        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                device.sendCommand("terminal length 0");  //命令不分页显示
                String route = device.sendCommand("show ip route");
                String protocol = device.sendCommand("show ip protocols");
                String msg = get_protocol(protocol);
                JSONObject info = new JSONObject();
                info.put("route", route);
                info.put("protocol", protocol);
                result.put("state", true);
                result.put("msg", msg);
                result.put("info", info);
                logger.info(msg);
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }

    @RequestMapping(value = "/config/rip", method = RequestMethod.POST)
    public String config_rip(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, config rip");
        JSONObject result = new JSONObject();
        String dev_no = (String) data.get("dev_no");
        JSONArray network_list = data.getJSONArray("network_list");
        JSONArray mask_list = data.getJSONArray("mask_list");
        telnetClient device = get_device(dev_no);

        if (network_list.size() != mask_list.size()) {
            result.put("state", false);
            String msg = "POST param error. network_list doesn't have the same length as mask_list";
            result.put("msg", msg);
            logger.error(msg);
            return result.toJSONString();
        }

        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                device.clear_router();
                for (int i = 0; i < network_list.size(); i++) {
                    String network = (String) network_list.get(i);
                    String mask = (String) mask_list.get(i);
                    BooleanResult boolean_result = device.configRip(network, mask);
                    if (!boolean_result.boolean_result) {
                        result.put("state", false);
                        String msg = boolean_result.string_result;
                        result.put("msg", msg);
                        logger.error(msg);
                        return result.toJSONString();
                    }
                }
                result.put("state", true);
                String msg = dev_no + " RIP config success.";
                result.put("msg", msg);
                logger.info(msg);
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }

    @RequestMapping(value = "/config/ospf", method = RequestMethod.POST)
    public String config_ospf(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, config rip");
        JSONObject result = new JSONObject();
        String dev_no = (String) data.get("dev_no");
        JSONArray network_list = data.getJSONArray("network_list");
        JSONArray area_list = data.getJSONArray("area_list");
        JSONArray mask_list = data.getJSONArray("mask_list");
        telnetClient device = get_device(dev_no);
        if (network_list.size() != mask_list.size() || network_list.size() != area_list.size()) {
            result.put("state", false);
            String msg = "POST param error. network_list, area_list and mask_list don't have the same length";
            result.put("msg", msg);
            logger.error(msg);
            return result.toJSONString();
        }

        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                device.clear_router();
                for (int i = 0; i < network_list.size(); i++) {
                    String network = (String) network_list.get(i);
                    String area = (String) area_list.get(i);
                    String mask = (String) mask_list.get(i);

                    BooleanResult boolean_result = device.configOspf(network, area, mask);
                    if (!boolean_result.boolean_result) {
                        result.put("state", false);
                        String msg = boolean_result.string_result;
                        result.put("msg", msg);
                        logger.error(msg);
                        return result.toJSONString();
                    }
                }
                result.put("state", true);
                String msg = dev_no + " OSPF config success.";
                result.put("msg", msg);
                logger.info(msg);
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }

    @RequestMapping(value = "/ping", method = RequestMethod.POST)
    public String ping(@RequestBody JSONObject data) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetClientController.class);
        logger.info("POST Request, ping");
        JSONObject result = new JSONObject();
        String dev_no = (String) data.get("dev_no");
        String ip = (String) data.get("ip");
        telnetClient device = get_device(dev_no);
        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                BooleanResult boolean_result = device.ping(ip);
                if(boolean_result.boolean_result) {
                    result.put("state", true);
                }else{
                    result.put("state", false);
                }
                String msg =boolean_result.string_result;
                result.put("msg", msg);
                logger.info(msg);
                return result.toJSONString();
            } catch (Exception e) {
                result.put("state", false);
                String msg = "Service error.";
                result.put("msg", msg);
                logger.error(msg);
                return result.toJSONString();
            }
        }
    }


    private String null_device_return(Logger logger, JSONObject result, String dev_no) {
        result.put("state", false);
        String msg = "";
        msg += (dev_no + " is not exist.");
        msg += ("All support devices are: s0, r0, r1, r2.");
        result.put("msg", msg);
        logger.info(msg);
        return result.toJSONString();
    }

    private telnetClient get_device(String dev_no) {
        //get device by dev_no
        if (dev_no.equals("s0")) {
            return TopologyEndApplication.switch0;
        }
        if (dev_no.equals("r0")) {
            return TopologyEndApplication.router0;
        }
        if (dev_no.equals("r1")) {
            return TopologyEndApplication.router1;
        }
        if (dev_no.equals("r2")) {
            return TopologyEndApplication.router2;
        }
        return null;
    }


    String get_protocol(String protocol) {
        if (protocol.contains("ospf")) {
            return "当前路由协议: OSPF";
        }
        if (protocol.contains("rip")) {
            return "当前路由协议: RIP";
        }
        return "尚未配置路由协议";
    }
}
