package com.xpcf.common.constant;

import lombok.Getter;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 12/23/2020 4:17 PM
 */
public class WareConstant {

    @Getter
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),FINISH(3,"已完成"),
        HAS_ERROR(4,"有异常");


        private int code;
        private String msg;

        PurchaseStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
    }


    @Getter
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),FINISH(3,"已完成"),
        HAS_ERROR(4,"采购失败");


        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
    }

}
