package com.xk.usercenter.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageRequest extends TeamQuery{
    int current;
    int PageSize;
}
