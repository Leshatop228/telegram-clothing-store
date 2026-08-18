package com.labubu.telegramclothingstore.catalog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CreateCategoryRequest {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}