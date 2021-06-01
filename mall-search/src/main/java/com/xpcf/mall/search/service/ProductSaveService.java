package com.xpcf.mall.search.service;

import com.xpcf.common.to.es.SkuESModel;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 1:48 PM
 */

public interface ProductSaveService {

    Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;
}
