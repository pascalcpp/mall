package com.xpcf.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.xpcf.common.to.es.SkuESModel;
import com.xpcf.mall.search.config.MallELasticSearchConfig;
import com.xpcf.mall.search.constant.ESConstant;
import com.xpcf.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 1:50 PM
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();

        for (SkuESModel skuESModel : skuESModels) {
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);

            indexRequest.id(skuESModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuESModel);
            indexRequest.source(jsonString, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = client.bulk(bulkRequest, MallELasticSearchConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        if(b){
            List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
                return item.getId();
            }).collect(Collectors.toList());
            log.error("商品上架错误: {}",collect);
        }
        return !b;

    }
}
