package com.xpcf.mall.search.service;

import com.xpcf.mall.search.vo.SearchParam;
import com.xpcf.mall.search.vo.SearchResult;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/31/2020 1:43 PM
 */
public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
