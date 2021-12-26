package com.example.topology_end;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetController {
    public String hello() {
        return "动态路由后端，Running on http://localhost:8999/";
    }

    /**
     * 设备登录
     *
     * @param dev_no   设备编号
     * @param ip       设备ip
     * @param password 设备密码
     */
    public String telnet_login(String dev_no, String ip, String password) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, telnet login.");
        JSONObject result = new JSONObject();

        telnetClient device = get_device(dev_no);
        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                Boolean state = device.login(ip, 23, password);
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

    /**
     * 初始化设定串行接口
     *
     * @param dev_no    设备编号
     * @param ip_list   ip列表（两个，对应s0和s1，如果没有则为“”或“0.0.0.0”）
     * @param mask_list 掩码列表（两个，对应s0和s1，格式应形如“255.255.255.0”，如果没有则为“0”）
     */
    public String init_serial(String dev_no, String[] ip_list, String[] mask_list) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, init serial.");
        JSONObject result = new JSONObject();

        if (ip_list.length != 2 || mask_list.length != 2) {
            result.put("state", false);
            String msg = "Param error. Ip_list or mask_list don't have the correct length.";
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
                for (int i = 0; i < ip_list.length; i++) {
                    BooleanResult boolean_result = device.initSerial(i, ip_list[i], mask_list[i]);
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


    /**
     * 初始化设定回环接口
     *
     * @param dev_no 设备编号
     * @param port   回环接口的端口（例如loopback 0中的0）
     * @param ip     ip地址
     * @param mask   掩码
     */
    public String init_loopback(String dev_no, String port, String ip, String mask) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, init loopback");
        JSONObject result = new JSONObject();


        telnetClient device = get_device(dev_no);
        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                BooleanResult boolean_result = device.initLoopback(port, ip, mask);
                logger.info(boolean_result.string_result);
                if (!boolean_result.boolean_result) {
                    result.put("state", false);
                    String msg = boolean_result.string_result;
                    result.put("msg", msg);
                    logger.error(msg);
                    return result.toJSONString();
                }
                result.put("state", true);
                String msg = dev_no + " init loopback success.";
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


    /**
     * 获取设备信息（路由表和路由协议）
     *
     * @param dev_no 设备编号
     */
    public String get_info(String dev_no) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, get device info");
        JSONObject result = new JSONObject();

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


    /**
     * 设置静态路由协议
     *
     * @param dev_no       设备编号
     * @param network_list 网络ip列表
     * @param mask_list    掩码列表
     * @param target_list  下一跳ip列表
     */
    public String config_static(String dev_no, String[] network_list, String[] mask_list, String[] target_list) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, config static.");
        JSONObject result = new JSONObject();

        if (network_list.length != mask_list.length || target_list.length != mask_list.length) {
            result.put("state", false);
            String msg = "Param error. network_list, mask_list and target_list don't have the same length.";
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
                device.clear_router();
                for (int i = 0; i < network_list.length; i++) {
                    BooleanResult boolean_result = device.configStatic(network_list[i], mask_list[i], target_list[i]);
                    if (!boolean_result.boolean_result) {
                        result.put("state", false);
                        String msg = boolean_result.string_result;
                        result.put("msg", msg);
                        logger.error(msg);
                        return result.toJSONString();
                    }
                }
                result.put("state", true);
                String msg = dev_no + " static route config success.";
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


    /**
     * 设置RIP路由协议
     *
     * @param dev_no       设备编号
     * @param network_list 网络ip列表
     * @param mask_list    掩码列表
     */
    public String config_rip(String dev_no, String[] network_list, String[] mask_list) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, config rip.");
        JSONObject result = new JSONObject();

        if (network_list.length != mask_list.length) {
            result.put("state", false);
            String msg = "Param error. network_list doesn't have the same length as mask_list.";
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
                device.clear_router();
                for (int i = 0; i < network_list.length; i++) {
                    BooleanResult boolean_result = device.configRip(network_list[i], mask_list[i]);
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

    /**
     * 设置OSPF路由协议
     *
     * @param dev_no       设备编号
     * @param network_list 网络ip列表
     * @param mask_list    掩码列表
     * @param area_list    区域列表
     */
    public String config_ospf(String dev_no, String[] network_list, String[] mask_list, String[] area_list) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, config ospf.");
        JSONObject result = new JSONObject();

        if (network_list.length != mask_list.length || network_list.length != area_list.length) {
            result.put("state", false);
            String msg = "POST param error. Network_list, area_list and mask_list don't have the same length.";
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
                device.clear_router();
                for (int i = 0; i < network_list.length; i++) {
                    BooleanResult boolean_result = device.configOspf(network_list[i], mask_list[i], area_list[i]);
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

    /**
     * ping测试连通性
     *
     * @param dev_no 设备编号
     * @param ip     要ping的ip地址
     */
    public String ping(String dev_no, String ip) {
        //login device
        Logger logger = LoggerFactory.getLogger(TelnetController.class);
        logger.info("Get request, ping.");
        JSONObject result = new JSONObject();

        telnetClient device = get_device(dev_no);
        if (device == null) {
            //device is not exist.
            return null_device_return(logger, result, dev_no);
        } else {
            try {
                BooleanResult boolean_result = device.ping(ip);
                if (boolean_result.boolean_result) {
                    result.put("state", true);
                } else {
                    result.put("state", false);
                }
                String msg = boolean_result.string_result;
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

    /**
     * 设备不存在的通用结果返回
     *
     * @param logger 日志对象
     * @param result JSON结果对象
     * @param dev_no 设备编号
     */
    private String null_device_return(Logger logger, JSONObject result, String dev_no) {
        result.put("state", false);
        String msg = "";
        msg += (dev_no + " is not exist.");
        msg += ("All support devices are: s0, r0, r1, r2.");
        result.put("msg", msg);
        logger.info(msg);
        return result.toJSONString();
    }

    /**
     * 获取设备对象
     *
     * @param dev_no 设备编号
     */
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

    /**
     * 获取路由协议类型
     *
     * @param protocol 路由协议信息
     */
    String get_protocol(String protocol) {
        if (protocol.contains("ospf")) {
            return "当前路由协议: OSPF.";
        }
        if (protocol.contains("rip")) {
            return "当前路由协议: RIP.";
        }
        return "尚未配置路由协议.";
    }
}
