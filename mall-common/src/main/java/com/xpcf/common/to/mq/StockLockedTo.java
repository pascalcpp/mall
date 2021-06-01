package com.xpcf.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/8/2021 8:39 PM
 */
@Data
public class StockLockedTo {
    private Long id;
    private StockDetailTo detail;
}
