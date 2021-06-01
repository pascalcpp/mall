package com.xpcf.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.xpcf.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2021000117610414";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCPdFYf7vw9YtM4Nh7irjh6qm/Gw0+HjHlAUujKsuMlW6BmFWfcTOT9v2Vr66HwF3wf8MuMelw7mObP4/n24EhSlHFCWDqGGLJEZkcPaOyNgLaVtzADyQNySUjnLhvXQ3dl8Yk8iufDoksqcJNnf5QjdAed04Iif8IQgXTOqZPGWcohqDODAIsGcwE5+jc8y8Gp1WcQwGEoZXYUOjdenk0ntD/eI8W0MsyT25PhCn3FPYk52KuOpeOOsCbupqQR8ny8rxsUiP8pHhrxSjTznGoVuSlN5+0J9izy/aHy2ISl1kQMnv1d3vJgVuv4BZYbLzFPaECObcSJ4lW6ju7rYBAdAgMBAAECggEAV1bwPL8HSeSKTtX8leyWrHBqX2x4VkG7bONNomOpkgwoKx7rPiB+5tMJz9wFmyThSJ0VgLDayNo95OKbRKimv8hq8IbBEvWkS6qtpv8tzrVH9P00OYnAFGvz+/SBtNz5q9KTyI2Vr/F2rB8gT8pIChHRtVCaEaERKXV4BX8PXppQ7+CuCwN85X3LDLpd/dfkjKYvzZizPhg87Pk2Umqs5JknQafF8mpa5yyimqQ6cscqGLO3m2KVCHj83yieRl0Dy0pqR4f+eJoY5KN5Dr0XkvdP//4oRSLRvzYZw91xzf5GkGyUPfStLjt9lxFzuZkaMdJTU8Mgh8/diit2Tw7ZIQKBgQDBEoQlrWh9KeIea6tfJPs3crvpS6PtEvefI5O88Y3vBrHp2DOn5DLbki/qtzfntmw/IwNsIeuL2ic2cI6CrhQwZuVv8wMcanm/T976XtF/GHRsSk3aPVENHS5mjZuJmkPvJaQMndAThsdITll3jggrkrNsbJSXICka7g7IlvjyCQKBgQC+NdMa+4ImoihkuwNEUJH55Jl3R0ri3jX+4+7izUu0x7GA2BBxNHOFfgNkmFf3HgTs2r/eOTat/fVGl7xCOGR6l3WY+W9QjHQP+4v6OOC3knUqZXznuKcOaNLtfnu8ifUeqhLIsumX51To1Cu9KhelFryv6FiotrfNmAtaG+JidQKBgFj0YgRGABVi3basXtnIXSJv71oOqqlJCuO8bY4pMP7G8+PM5AffoPr/QKqA3fYZW4g/Zo1El5To3xY7B7333H9seRt6OJ24BP37G735P8ayCrn2BiaeB6DmKAHCDr54YAJoMkxJzZuhhmGvD5OA0gzZW2eCI3f5rlalBhSvUmWZAoGABPXIXILEgRdncEEEM8raE6cnbVnZlRLwW4Y8G4P4/mzarx5zR1Bz1r5TflMOsC4DON33dOcPCvh9ZerdCOkJjbzXXzbZ9ULEYApSOyMykPbrwUBGQpCpIpIPNgw/U8pRLwyGUt9/geuRZkTqIxOu7Cosn3WyoiupnV3ipeXmQGUCgYBFAwzyam+neQGI00FiPkwys4dRZ2/9WXQ+3XJ1isZ/JwyNbDsKLlsF8S6+Kirr+q8xxVCnN/FsmoAek186V0QB0R2WhIfciuFHpDWVJBsxQSiC9jhS6kbM8KHRxTOUVmgPho6ywTrFTBa4uwo2ornftdeU9qZSYsr+RCpmbhKNCg==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoQ9gUNbSFBi8ECp/A2o5/m0eakTillG6+IEEdnCvCdgE7c/AvqTT0RICjJXBC6Dk+SWCsQVrOLHC+91gsgZRC9DPuaBlkB4ihBlCI29Co8vqVlqUBybopOMdLsk8w+DirOLAFRyjNy3o0QCHvGDV4v92eOdw6JDg6172aKPcRjFxab+80J9lEv0EI6eXG7hK4YMGbwu95qiMmriewC0WRTiWsb0j2oP5KoUHff/0vMZaO4eza+A1+Xd+NYUGweNdFmZgxccYd4s2k8iKvktOyw4pROuZvCGiyTw85z/NqRNcWBq/vYiQ/aIiQ2/Rn/7Hd2B11ZGw8ediOxmQMy3MmQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://order.mall.com/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://member.mall.com/memberOrder.html";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    public static String timeout = "30m";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "d:\\";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
