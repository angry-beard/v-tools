package com.v.core.base.exception;

/**
 * @author angry_beard
 * @date 2021/7/7 3:22 下午
 */
public class VClientException extends RuntimeException {

    public VClientException(Exception exception) {
        super(exception);
    }

    public VClientException(String msg) {
        super(msg);
    }
}
