package com.paymentology.paymentreconciliation.exception;

import java.io.IOException;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String msg){
        super(msg);
    }
}
