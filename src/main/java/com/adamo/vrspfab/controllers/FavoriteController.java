package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @GetMapping
    public String getFavorites() {
        return "List of favorites";
    }

    @PostMapping
    public String createFavorite() {
        return "Favorite created";
    }
}
