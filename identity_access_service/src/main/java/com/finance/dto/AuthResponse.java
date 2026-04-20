package com.finance.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    // This is the "Key" the user will send back in the header of every future request.
    private String token;             

    // A friendly message to show the user (e.g., "Login Successful!").
    private String message;           

    // We send the role so the frontend can hide or show buttons 
    // (like the 'Admin Panel' button) based on who is logged in.
    private String role;              

    // This tells the frontend exactly which page to load first 
    // (e.g., /admin/dashboard vs /user/home).
    private String endpoint;          
}