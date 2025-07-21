package com.adamo.vrspfab.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    @GetMapping
    public String getBookmarks() {
        return "List of bookmarks";
    }

    @PostMapping
    public String createBookmark() {
        return "Bookmark created";
    }
}
