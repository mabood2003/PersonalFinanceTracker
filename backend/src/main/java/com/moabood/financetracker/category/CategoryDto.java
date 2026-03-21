package com.moabood.financetracker.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private boolean isDefault;
}
