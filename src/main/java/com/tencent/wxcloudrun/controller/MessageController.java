package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import javax.xml.ws.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MessageController {
    private final String appid = "wx07af032f69e86360";
    private final String secret = "3252c3e30dc041f80986bf01e780001b";
    private Logger LOGGER = LoggerFactory.getLogger(MessageController.class);
    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/sendMessage/{code}")
    private String sendMessage(@PathVariable(value = "code") String code){
        String openId = getOpenId(code);
        return pushMessage(openId);
    }

    public String getOpenId(String code){

        ResponseEntity<String> entity = restTemplate.getForEntity("https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code=" + code + "&grant_type=authorization_code", String.class);

        String body = entity.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        LOGGER.info("获取的openid响应"+body);
        assert jsonObject != null;
        String openid = jsonObject.getString("openid");
        return openid;
    }

    public String getAccessToken(){
        Map<String,Object> params = new HashMap<>();
        params.put("grant_type","client_credential");
        params.put("appid",appid);
        params.put("secret",secret);
        LOGGER.info("token获取参数" + params);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}", String.class, params);
        String result = responseEntity.getBody();
        LOGGER.info("请求结果"+result);


        JSONObject object = JSONObject.parseObject(result);
        String access_token = object.getString("access_token");
        String expires_in = object.getString("expires_in");
        LOGGER.info("token"+access_token);
        LOGGER.info("过期时间"+expires_in);
        return access_token;
    }


    public String pushMessage(String openid){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String time = now.format(dateTimeFormatter);
        String accessToken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
       LOGGER.info("消息推送请求路径"+url);
        Map<String,Object> params = new HashMap<>();
        params.put("access_token",accessToken);
        params.put("template_id","NIYUCvpL8fFZxM5pmqi7YUjn1v4QbYFMnjbpSp1IELc");
        params.put("touser",openid);

        Map<String,Object> data = new HashMap<>();
        data.put("thing17","代同学");
        data.put("thing2","吃药啦");
        data.put("date4",time);
        data.put("thing12","天气不错，四十来°");
        data.put("thing11","没有备注信息哦");
//        JSONObject object = JSONObject.parseObject(data.toString());

        String datas = JSON.toJSONString(data);

        params.put("data",datas);
        params.put("miniprogram_state","developer");
        params.put("lang","zh_CN");
        LOGGER.info("发送消息数据" + params);
        String res = JSON.toJSONString(params);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, res, String.class);
        return responseEntity.getBody();

    }


}
