package com.xpcf.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xpcf.common.to.es.SkuESModel;
import com.xpcf.common.utils.R;
import com.xpcf.mall.search.config.MallELasticSearchConfig;
import com.xpcf.mall.search.constant.ESConstant;
import com.xpcf.mall.search.feign.ProductFeignService;
import com.xpcf.mall.search.service.MallSearchService;
import com.xpcf.mall.search.vo.AttrResponseVo;
import com.xpcf.mall.search.vo.BrandVo;
import com.xpcf.mall.search.vo.SearchParam;
import com.xpcf.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.util.locale.ParseStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/31/2020 1:43 PM
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;


    @Override
    public SearchResult search(SearchParam searchParam) {

        SearchResult result = null;



        SearchRequest searchRequest = buildSearchRequest(searchParam);

        try {
            SearchResponse response = client.search(searchRequest, MallELasticSearchConfig.COMMON_OPTIONS);
            result = buildSearchResult(response,searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder source = new SearchSourceBuilder();

        //1 query bool(must will compute score & filter)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        if(null != param.getCatalog3Id()){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        if(null != param.getBrandId() && param.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        if(null != param.getAttrs() && param.getAttrs().size() > 0){

            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();

                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs",nestedBoolQueryBuilder, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }

        if (null != param.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock().equals(1)));
        }



        if(!StringUtils.isEmpty(param.getSkuPrice())){

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");

            if(s.length == 2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }else {

                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }else if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }

            }
            boolQueryBuilder.filter(rangeQuery);

        }

        source.query(boolQueryBuilder);

        //2 sort

        if(!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            source.sort(s[0],order);
        }

        //3 pagination
        // from (pageNum - 1)* pageSize
        source.from( (param.getPageNum() - 1) * ESConstant.PRODUCT_PAGESIZE );
        source.size(ESConstant.PRODUCT_PAGESIZE);

        //4 highlight

        if(!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            source.highlighter(highlightBuilder);
        }

        //5 aggs
        // brand_agg
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
                //sub agg
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        source.aggregation(brand_agg);

        // catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        source.aggregation(catalog_agg);

        // attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");

        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);

        source.aggregation(attr_agg);

        //6 TODO what


//        System.out.println("DSL: " + source);

        //end
        SearchRequest searchRequest = new SearchRequest(new String[]{ESConstant.PRODUCT_INDEX},source);

        return searchRequest;
    }

    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {

        SearchResult result = new SearchResult();

        // set products
        SearchHits hits = response.getHits();
        List<SkuESModel> skuESModelList = new ArrayList<>();
        if(null != hits.getHits() && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                // 关于 TypeReference<> 使用 当需要 SkuESModel<XX>.class 时使用
                SkuESModel skuESModel = JSON.parseObject(source,SkuESModel.class);

                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].string();
                    skuESModel.setSkuTitle(skuTitle);
                }


                skuESModelList.add(skuESModel);
            }
        }
        result.setProducts(skuESModelList);


        // set total
        Long total = hits.getTotalHits().value;
        result.setTotal(total);

        // set totalPages
        Integer totalPages = Math.toIntExact(total % ESConstant.PRODUCT_PAGESIZE == 0 ? total / ESConstant.PRODUCT_PAGESIZE : (total / ESConstant.PRODUCT_PAGESIZE) + 1);
        result.setTotalPages(totalPages);

        // set curPageNum
        result.setPageNum(param.getPageNum());

        // set catalogVos
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String key = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(key));

            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // set brandVos

        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            Number key = bucket.getKeyAsNumber();
            brandVo.setBrandId(key.longValue());

            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);


        // set attrVos
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            Long attrId = bucket.getKeyAsNumber().longValue();

            String attrName = ((ParsedStringTerms)bucket.getAggregations().get("attr_name_agg"))
                    .getBuckets().get(0).getKeyAsString();

//            String attrValue = ((ParsedStringTerms)bucket.getAggregations().get("attr_value_agg"))
//                    .getBuckets().get(0).getKeyAsString();
            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets()
                    .stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());


            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValue);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        if (null != param.getAttrs() && !param.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> navs = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");

                navVo.setNavValue(s[1]);
                long attrId = Long.parseLong(s[0]);
                R r = productFeignService.info(attrId);
                result.getAttrIds().add(attrId);

                if (r.getCode() == 0) {
                    AttrResponseVo responseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(responseVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.mall.com/list.html?"+replace);

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navs);
        }

        if (null != param.getBrandId() && !param.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });

                StringBuffer stringBuffer = new StringBuffer();
                String replace = "";
                for (BrandVo brand : brands) {
                    stringBuffer.append(brand.getBrandName()).append(";");
                    replace = replaceQueryString(param, brand.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.mall.com/list.html?"+replace);
            }
            navs.add(navVo);
            result.setNavs(navs);
        }
        //TODO catalog



        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8").replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+"=" + encode, "");
    }
}
