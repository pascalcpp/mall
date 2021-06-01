package com.xpcf.mall.product.vo;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/26/2020 7:05 PM
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2VO {

    private String catalog1Id;
    private List<Catalog3VO> catalog3List;
    private String name;
    private String id;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3VO{
        private String catalog2Id;
        private String name;
        private String id;
    }

}
