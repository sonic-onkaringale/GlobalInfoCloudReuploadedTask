package com.onkaringale.shoptask.db.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CartModel {
    @Id(assignable = true)
    public Long id;

    public String product;
    public int quantity;

    public CartModel(Long id, String product, int quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
    }
}
