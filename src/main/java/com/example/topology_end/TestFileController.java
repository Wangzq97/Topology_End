package com.example.topology_end;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Map;

@RestController
public class TestFileController {
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public String test_file(@RequestBody JSONObject data) {
        Logger logger = LoggerFactory.getLogger(TestFileController.class);
        String verify_content = data.getString("verify_content");
        JSONObject res = new JSONObject();
        JSONObject jsonObj = JSONObject.parseObject(verify_content);
        JSONObject test = jsonObj.getJSONObject("test");
        Iterator<Map.Entry<String, Object>> it = test.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> testCase = it.next();
            String caseId = testCase.getKey();
            JSONObject value = (JSONObject) testCase.getValue();
            String outPut = value.getString("output");
            outPut = outPut.replaceAll(" \\[[0-9]*/[0-9]*\\] ", " ");
            String inPut = value.getString("input");
            String devno = value.getString("dev_no");
            logger.info("case id is "+caseId);
            logger.info("inPut is "+inPut);
            logger.info("predict outPut is "+outPut);
            TopologyEndApplication.telnet_controller.send_command(devno, "terminal length 0");
            String cmdRes = TopologyEndApplication.telnet_controller.send_command(devno, inPut);
            cmdRes = cmdRes.replaceAll(" \\[[0-9]*/[0-9]*\\] ", " ");
            cmdRes = cmdRes.replaceAll("/32", "");
            logger.info("actual outPut is "+cmdRes);
            for (String item : outPut.split("\n")) {
                if (!cmdRes.contains(item)) {
                    res.put("state", false);
                    return res.toJSONString();
                }
            }
        }
        res.put("state", true);
        logger.info("test success.");
        return res.toJSONString();
    }
}
