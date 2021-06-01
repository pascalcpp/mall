package com.xpcf.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.xpcf.mall.order.config.AlipayTemplate;
import com.xpcf.mall.order.service.OrderService;
import com.xpcf.mall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/11/2021 3:44 AM
 */
@Controller
@Slf4j
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * TODO 接口幂等性
     * 参考订单实现
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        log.info(pay);
        return pay;
    }

}
