package com.xpcf.mall.order.web;

import com.xpcf.common.exception.NoStockException;
import com.xpcf.mall.order.service.OrderService;
import com.xpcf.mall.order.vo.OrderConfirmVo;
import com.xpcf.mall.order.vo.OrderSubmitVo;
import com.xpcf.mall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 2/1/2021 11:55 PM
 */
@Controller
@Slf4j
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpSession session) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        session.setAttribute("msg", "");
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode().equals(0)) {
                redirectAttributes.addFlashAttribute("submitOrderResp", responseVo);
                return "redirect:http://order.mall.com/pay.html";
            } else {
                String msg = "下单失败: ";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "订单信息过期，请刷新后提交";
                        break;
                    case 2:
                        msg += "订单价格发生变化， 请确认后提交";
                        break;
                    case 3:
                        msg += "商品锁定失败，商品库存不足";
                        break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.mall.com/toTrade";
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            log.info(message);
            if (StringUtils.isEmpty(message)) {
                message = "出现未知异常";
            }
            redirectAttributes.addFlashAttribute("msg", message);

            return "redirect:http://order.mall.com/toTrade";
        }

    }
}
