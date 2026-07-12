package com.milicastvnvc.transactionmanagement.exception;

import java.util.Map;

public record ValidationErrorResponse(int status,
                                      String message,
                                      Map<String, String> fieldErrors) {

}