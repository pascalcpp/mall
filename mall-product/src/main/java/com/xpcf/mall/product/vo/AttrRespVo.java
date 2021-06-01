package com.xpcf.mall.product.vo;

import lombok.Data;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/20/2020 10:22 AM
 */
@Data
public class AttrRespVo extends AttrVo{
    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

}
