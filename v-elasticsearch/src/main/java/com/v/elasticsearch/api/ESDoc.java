package com.v.elasticsearch.api;

import lombok.*;

/**
 * Created by angry_beard on 2021/5/13.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ESDoc<T> {
    private String id;
    private T data;
}
