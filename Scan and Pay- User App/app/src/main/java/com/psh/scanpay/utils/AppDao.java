package com.psh.scanpay.utils;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.psh.scanpay.models.Order;
import com.psh.scanpay.models.ProductItem;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface AppDao {
    @Insert(onConflict = REPLACE)
    void insertProduct(ProductItem productItem);

    @Query("DELETE FROM new_cart WHERE ID = :id")
    void deleteProduct(int id);

    @Query("UPDATE new_cart SET product_quantity = product_quantity+1 WHERE ID = :id")
    void incrementQuantity(int id);

    @Query("UPDATE new_cart SET product_quantity = product_quantity-1 WHERE ID = :id")
    void decrementQuantity(int id);

    @Query("SELECT * FROM new_cart")
    List<ProductItem> getCartItems();

    @Query("DELETE FROM new_cart")
    void deleteAllCartItems();

    @Insert(onConflict = REPLACE)
    void insertOrder(Order order);

    @Query("SELECT * FROM order_history ORDER BY ID DESC")
    List<Order> getAllOrders();

}
