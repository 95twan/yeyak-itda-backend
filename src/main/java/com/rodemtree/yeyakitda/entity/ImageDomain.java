package com.rodemtree.yeyakitda.entity;

import lombok.Getter;

public enum ImageDomain {
    REVIEW("reviews"),
    RESTAURANT("restaurants"),
    MENU("menus");

    @Getter
    private final String path;

    ImageDomain(String path) {
        this.path = path;
    }

}
