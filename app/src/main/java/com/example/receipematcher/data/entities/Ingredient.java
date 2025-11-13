package com.example.receipematcher.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredients")
public class Ingredient {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double quantity; // Changed to double for decimal quantities
    public String unit; // measurement unit, e.g., g, kg, ml, pcs
    public String expiryDate; // store as String or use TypeConverter for Date
}
