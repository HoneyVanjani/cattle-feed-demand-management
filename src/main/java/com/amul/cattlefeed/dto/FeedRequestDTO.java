package com.amul.cattlefeed.dto;

import lombok.Data;

@Data
public class FeedRequestDTO {

    private Long feedId;
    private Integer quantity;
    private String cycle;

}