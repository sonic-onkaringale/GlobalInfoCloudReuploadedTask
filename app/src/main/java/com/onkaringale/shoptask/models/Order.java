package com.onkaringale.shoptask.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    public String orderUid;
    public String userId;
    public List<String> products;
    public List<Integer> productQuantity;
    public String customerName;
    public String customerAddress;
    public String customerMobileNo;
    @ServerTimestamp
    public Timestamp placedOrderTime;
    public Integer orderStatus;
    public Timestamp deliveryDate;
    public String rejectionReason;

    public Order(String orderUid,String userId, List<String> products, List<Integer> productQuantity, String customerName, String customerAddress, String customerMobileNo, Timestamp placedOrderTime, Integer orderStatus, Timestamp deliveryDate, String rejectionReason) {
        this.orderUid = orderUid;
        this.userId=userId;
        this.products = products;
        this.productQuantity = productQuantity;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerMobileNo = customerMobileNo;
        this.placedOrderTime = placedOrderTime;
        this.orderStatus = orderStatus;
        this.deliveryDate = deliveryDate;
        this.rejectionReason = rejectionReason;
    }

    public Order() {
    }

    public Order(Map<String, Object> map) {
        orderUid = (String) map.get("orderUid");
        userId= (String) map.get("userId");
        products= (List<String>) map.get("products");
        productQuantity= (List<Integer>) map.get("productQuantity");
        customerName= (String) map.get("customerName");
        customerAddress= (String) map.get("customerAddress");
        customerMobileNo= (String) map.get("customerMobileNo");
        placedOrderTime= (Timestamp) map.get("placedOrderTime");
        orderStatus= (Integer) ( (Long) map.get("orderStatus")).intValue();
        deliveryDate= (Timestamp) map.get("deliveryDate");
        rejectionReason= (String) map.get("rejectionReason");
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderUid",orderUid);
        map.put("userId",userId);
        map.put("products",products);
        map.put("productQuantity",productQuantity);
        map.put("customerName",customerName);
        map.put("customerAddress",customerAddress);
        map.put("customerMobileNo",customerMobileNo);
        map.put("placedOrderTime",placedOrderTime);
        map.put("orderStatus",orderStatus);
        map.put("deliveryDate",deliveryDate);
        map.put("rejectionReason",rejectionReason);
        return map;
    }
}
