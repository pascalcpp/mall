package com.xpcf.mall.thirdparty;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.xpcf.mall.thirdparty.component.SmsComponent;
import com.xpcf.mall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallThirdPartyApplicationTests {

    @Autowired
    private OSS ossClient;

    @Autowired
    private SmsComponent smsComponent;

    @Test
    public void testSendSms() {
        smsComponent.sendSmsCode("19983434671","1111");
    }

    @Test
    public void test() throws FileNotFoundException {
        File file;
        InputStream inputStream = new FileInputStream("D:\\google_download\\ziliao\\docs\\pics\\f6982a3217eb2fa3.jpg");
        ossClient.putObject("mall-xpcf","test.jpg",inputStream);
        System.out.println("finish test");

    }
}
