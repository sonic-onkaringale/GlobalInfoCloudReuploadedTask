package com.onkaringale.shoptask.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {

    public String userUid;
    public String name;
    public String mobileNo;
    public String emailId;
    public String address;
    public String city;
    public String pincode;
    public String password;
    public String firebaseToken;

    public User(String userUid, String name, String mobileNo, String emailId, String address, String city, String pincode, String password, String firebaseToken) {
        this.userUid = userUid;
        this.name = name;
        this.mobileNo = mobileNo;
        this.emailId = emailId;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.password = password;
        this.firebaseToken = firebaseToken;
    }

    public User() {
    }
    public User(Map<String , Object> map) {
        userUid= (String) map.get("userUid");
        name= (String) map.get("name");
        mobileNo= (String) map.get("mobileNo");
        emailId= (String) map.get("emailId");
        address= (String) map.get("address");
        city= (String) map.get("city");
        pincode= (String) map.get("pincode");
        password= (String) map.get("password");
        firebaseToken= (String) map.get("firebaseToken");
    }

    public Map<String , Object> toMap()
    {
        Map<String ,Object> map = new HashMap<>();
        map.put("userUid",userUid);
        map.put("name",name);
        map.put("mobileNo",mobileNo);
        map.put("emailId",emailId);
        map.put("address",address);
        map.put("city",city);
        map.put("pincode",pincode);
        map.put("password",password);
        map.put("firebaseToken",firebaseToken);
        return map;
    }
}
