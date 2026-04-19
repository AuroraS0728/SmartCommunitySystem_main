package com.smartcommunity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageData<T> {
    private List<T> items;
    private Long total;
    private Integer page;
    private Integer size;
}

