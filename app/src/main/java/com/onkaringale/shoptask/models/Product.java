package com.onkaringale.shoptask.models;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.type.DateTime;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    public String productUid;
    public String productName;
    public String productDescription;
    @ServerTimestamp
    public Timestamp createdTime;
    public Double price;
    public List<String> imageLinks;

    public Product() {
    }

    public Product(String productUid, String productName, String productDescription, Double price, List<String> imageLinks) {
        this.productUid = productUid;
        this.productName = productName;
        this.productDescription = productDescription;
        this.createdTime = null;
        this.price = price;
        this.imageLinks = imageLinks;
    }

    public Product(Map<String, Object> map) {
        productUid = (String) map.get("productUid");
        productName = (String) map.get("productName");
        productDescription = (String) map.get("productDescription");
        createdTime= (Timestamp) map.get("createdTime");
        price= (Double) map.get("price");
        imageLinks= (List<String>) map.get("imageLinks");
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("productUid",productUid);
        map.put("productName",productName);
        map.put("productDescription",productDescription);
        map.put("createdTime",createdTime);
        map.put("price",price);
        map.put("imageLinks",imageLinks);
        return map;
    }


}
