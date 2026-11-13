package com.examly.springapp.model;
import jakarta.persistence.*;
@Entity
public class VeganSnack {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private int id;
 private String snackName;
 private String snackType;
 private String quantity;
 private int price;
 private int expiryInMonths;
 public VeganSnack() {}
 public VeganSnack(String snackName,String snackType,String quantity,int price,int expiryInMonths) { this.snackName=snackName; this.snackType=snackType; this.quantity=quantity; this.price=price; this.expiryInMonths=expiryInMonths; }
 public int getId(){return id;} public void setId(int id){this.id=id;}
 public String getSnackName(){return snackName;} public void setSnackName(String v){snackName=v;}
 public String getSnackType(){return snackType;} public void setSnackType(String v){snackType=v;}
 public String getQuantity(){return quantity;} public void setQuantity(String v){quantity=v;}
 public int getPrice(){return price;} public void setPrice(int v){price=v;}
 public int getExpiryInMonths(){return expiryInMonths;} public void setExpiryInMonths(int v){expiryInMonths=v;}
}