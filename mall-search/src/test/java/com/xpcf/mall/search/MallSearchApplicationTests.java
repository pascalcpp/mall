package com.xpcf.mall.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xpcf.mall.search.config.MallELasticSearchConfig;
import com.xpcf.mall.search.service.MallSearchService;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallSearchApplicationTests {

    @Autowired
    MallSearchService mallSearchService;
    @Test
    public void contextLoads() throws IOException {

    }

    @Data
    class User{
        private String name;
        private String gender;
        private Integer age;
    }

}
