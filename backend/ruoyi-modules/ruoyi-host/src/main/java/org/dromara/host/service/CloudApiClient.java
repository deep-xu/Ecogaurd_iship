package org.dromara.host.service;

import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lyn
 */
public class CloudApiClient {

    // 带重试机制的HTTP上传（指数退避）
    public Boolean uploadData(Map<String, Object> data) throws InterruptedException, SyncFailedException {
        //int retry = 0;
        //while (retry++ < 3) {
        String path = (String)data.get("image");
        File file = new File(path);
        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {


                MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
                //Map<String, Object> params = new HashMap<>();
                data.put("file", createIs(multipartFile));
                HttpResponse response = HttpRequest.post("http://47.86.30.65:8080/ship/alert/upload")
                    //.header("appId", bppConfig.getAppId())
                    //.header("Authorization", "Bearer token")
                    .header("Content-Type", "multipart/form-data")
                    .form(data)
                    .execute();
                try (response) {
                    String body = response.body();
                    JSONObject jsonObject = JSONUtil.parseObj(body);
                    String code = jsonObject.getStr("code");
                    return "200".equals(code);
                }
                /*HttpUtil.createPost("http://47.86.30.65:8080/")
                    .form(data)
                    //.formStr(data)
                    .executeAsync();*/
                //return true;

            } catch (Exception e) {
                return false;
                // Thread.sleep(1000L * (int) Math.pow(2, retry));
            }
        }
        return false;

    }


        public  Boolean uploadImage(File file) {
            String serverUrl = "http://localhost:8080/upload";

            try(FileInputStream fileInputStream = new FileInputStream(file)) {
                MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
                Map<String, Object> params = new HashMap<>();
                params.put("file", createIs(multipartFile));
                HttpResponse response = HttpRequest.post(serverUrl)
                    //.header("appId", bppConfig.getAppId())
                    .header("Authorization", "Bearer token")
                    .header("Content-Type", "application/json")
                    .form(params)
                    .execute();

                try(response){
                    String body = response.body();
                    JSONObject jsonObject = JSONUtil.parseObj(body);
                    String code = jsonObject.getStr("code");
                    return "200".equals(code);
                }

            } catch (Exception e) {
                throw new RuntimeException("上传图片请求失败:"+ e.getMessage(),e);
            }

        }


    private InputStreamResource createIs(MultipartFile file) {
        InputStreamResource isr = null;
        try {
            isr = new InputStreamResource(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isr;
    }



    // 数据幂等性处理（防重复）
    /*@PostMapping("/ingest")
    public ResponseEntity<?> ingestData(@RequestBody DataPacket packet) {
        if (redisTemplate.opsForValue().setIfAbsent(packet.getMsgId(), "PROCESSED")) {
            databaseService.save(packet.getData()); // 仅处理一次
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(409).body("Duplicate request");
    }*/
}
