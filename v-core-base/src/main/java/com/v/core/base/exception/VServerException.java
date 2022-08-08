package com.v.core.base.exception;

/**
 * @author angry_beard
 * @date 2021/7/7 10:46 上午
 */
public class VServerException extends RuntimeException {

    public VServerException(Exception exception) {
        super(exception);
    }

    public VServerException(String msg) {
        super(msg);
    }
}
