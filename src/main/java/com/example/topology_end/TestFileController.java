package com.example.topology_end;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Iterator;
import java.util.Map;

@Controller
public class TestFileController {
    @RequestMapping(value = "/verify",method = RequestMethod.POST)
    public String test_file(@RequestBody String verify_content){
        JSONObject res = new JSONObject();
        JSONObject jsonObj = JSONObject.parseObject(verify_content);
        JSONObject test = jsonObj.getJSONObject("test");
        Iterator<Map.Entry<String, Object>> it = test.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Object> testCase = it.next();
            String caseId = testCase.getKey();
            JSONObject value = (JSONObject) testCase.getValue();
            String outPut = value.getString("output");
            String input = value.getString("input");
            String devno = value.getString("dev_no");
            String cmdRes = TopologyEndApplication.telnet_controller.send_command(devno,input);
            for(String item:outPut.split("\n")){
                res.put("msg",cmdRes);
                if(!cmdRes.contains(item)){
                    res.put("state",false);
                    return res.toJSONString();
                }
            }
        }
        res.put("state",true);
        return res.toJSONString();
    }
}
