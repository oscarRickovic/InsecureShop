package com.insecureshop.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.insecureshop.ProductDetail
import com.insecureshop.util.SecurePreferences
import java.security.MessageDigest
import android.util.Base64
import java.security.SecureRandom
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Util {

    fun verifyUserNamePassword(context: Context, username: String, password: String): Boolean {
        // Get stored credentials from encrypted preferences
        val securePrefs = SecurePreferences(context)
        val storedUsername = securePrefs.getString("AUTH_USERNAME", "")
        val storedPasswordHash = securePrefs.getString("AUTH_PASSWORD_HASH", "")
        val storedSalt = securePrefs.getString("AUTH_SALT", "")
        
        // If no stored credentials, use default for initial setup only
        if (storedUsername.isEmpty() || storedPasswordHash.isEmpty()) {
            // Only for first-time setup - should implement proper registration
            if (username == "admin" && password == "securePassword123") {
                val salt = generateSalt()
                val passwordHash = hashPassword(password, salt)
                
                // Store the credentials securely
                securePrefs.putString("AUTH_USERNAME", username)
                securePrefs.putString("AUTH_PASSWORD_HASH", passwordHash)
                securePrefs.putString("AUTH_SALT", salt)
                
                return true
            }
            return false
        }
        
        // For normal authentication - verify username and hash the provided password
        if (username == storedUsername) {
            val hashedPassword = hashPassword(password, storedSalt)
            return secureTimeConstantCompare(hashedPassword, storedPasswordHash)
        }
        
        return false
    }

    // Generate a random salt
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    // Hash password with salt using SHA-256
    private fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(saltBytes)
        val hashedBytes = md.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    }

    // Time-constant comparison to prevent timing attacks
    private fun secureTimeConstantCompare(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    private fun getProductList(): ArrayList<ProductDetail> {
        val productList = ArrayList<ProductDetail>()
        productList.add(ProductDetail(1, "Laptop", "https://images.pexels.com/photos/7974/pexels-photo.jpg", "80", 1, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(2, "Hat", "https://images.pexels.com/photos/984619/pexels-photo-984619.jpeg", "10", 2, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(3, "Sunglasses", "https://images.pexels.com/photos/343720/pexels-photo-343720.jpeg", "10", 4, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(4, "Watch", "https://images.pexels.com/photos/277390/pexels-photo-277390.jpeg", "30", 4, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(5, "Camera", "https://images.pexels.com/photos/225157/pexels-photo-225157.jpeg", "40", 2, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(6, "Perfumes", "https://images.pexels.com/photos/264819/pexels-photo-264819.jpeg", "10", 2, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(7, "Bagpack", "https://images.pexels.com/photos/532803/pexels-photo-532803.jpeg", "20", 2, "https://www.insecureshopapp.com"))
        productList.add(ProductDetail(8, "Jacket", "https://images.pexels.com/photos/789812/pexels-photo-789812.jpeg", "20", 2, "https://www.insecureshopapp.com"))
        return productList
    }

    fun saveProductList(context: Context, productList: List<ProductDetail> = getProductList()) {
        val productJson = Gson().toJson(productList)
        Prefs.getInstance(context).productList = productJson
    }

    fun getProductsPrefs(context: Context): List<ProductDetail> {
        val products = Prefs.getInstance(context).productList
        return Gson().fromJson(products, object : TypeToken<List<ProductDetail>>() {}.type)
    }

    fun updateProductItem(context: Context, updateProductDetail: ProductDetail) {
        val productList = getProductsPrefs(context)
        for (productDetail in productList) {
            if (productDetail.id == updateProductDetail.id) {
                productDetail.qty = updateProductDetail.qty
            }
        }
        saveProductList(context, productList)
    }

    fun getCartProduct(context: Context): ArrayList<ProductDetail> {
        val cartList = arrayListOf<ProductDetail>()
        val productList = getProductsPrefs(context)
        for (productDetail in productList) {
            if (productDetail.qty > 0) {
                cartList.add(productDetail)
            }
        }
        return cartList
    }
}