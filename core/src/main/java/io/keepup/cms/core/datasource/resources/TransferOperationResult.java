package io.keepup.cms.core.datasource.resources;

import java.io.File;

/**
 * Result of {@link StaticContentDeliveryService#store(File, String)} operation. Defines whether
 * it was successful and contains error message otherwise
 *
 * @since 1.8
 */
public class TransferOperationResult<T> {

    private  static final String OK = "ok";

    /**
     * default code 0 is OK
     */
    private int code;

    /**
     * Success flag
     */
    private boolean isSuccess;

    /**
     * Description of what happened
     */
    private String message;

    /**
     * Custom response payload
     */
    private T payload;

    public TransferOperationResult() {
        message = "";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public TransferOperationResult<T> ok() {
        final TransferOperationResult<T> transferOperationResult = new TransferOperationResult<>();
        transferOperationResult.setMessage(OK);
        transferOperationResult.setSuccess(true);
        return transferOperationResult;
    }

    public TransferOperationResult<T> error(String message) {
        final TransferOperationResult<T> transferOperationResult = new TransferOperationResult<>();
        transferOperationResult.setMessage(message);
        transferOperationResult.setCode(1);
        transferOperationResult.setSuccess(false);
        return transferOperationResult;
    }
}
