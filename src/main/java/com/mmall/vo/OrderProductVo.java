package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by peterchen on 2017/8/21.
 */
public class OrderProductVo {
    private List<OrderItemVo> orderItemVoList;
    private BigDecimal productTotalPric;
    private String imageHost;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getProductTotalPric() {
        return productTotalPric;
    }

    public void setProductTotalPric(BigDecimal productTotalPric) {
        this.productTotalPric = productTotalPric;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
