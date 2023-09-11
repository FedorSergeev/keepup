package io.keepup.cms.core.datasource.resources;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Result of {@link StaticContentDeliveryService#store(File, String)} operation. Defines whether
 * it was successful and contains error message otherwise
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class TransferOperationResult<T> {

    private static final String OK_MESSAGE = "ok";

    /**
     * default code 0 is OK
     */
    private int code;

    /**
     * Success flag
     */
    private boolean success;

    /**
     * Description of what happened
     */
    private String message;

    /**
     * Custom response payload
     */
    private T payload;

    /**
     * Default constructor, initializes message as empty string.
     */
    public TransferOperationResult() {
        message = StringUtils.EMPTY;
    }

    /**
     * Get operation result code.
     *
     * @return opaeration result code, default code 0 is OK
     */
    public int getCode() {
        return code;
    }

    /**
     * Set operation result code.
     *
     * @param code  opaeration result code, default code 0 is OK
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Checks whether operation is successful.
     *
     * @return true in case of operation success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Defines whether operation is successful.
     *
     * @param success  true in case of operation success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Get the message with additional information about operation.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message with additional information about operation.
     *
     * @param message  message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get transfer payload.
     *
     * @return payload object
     */
    public T getPayload() {
        return payload;
    }

    /**
     * Set transfer payload.
     *
     * @param payload payload object
     */
    public void setPayload(T payload) {
        this.payload = payload;
    }

    /**
     * Create successful operation result.
     *
     * @return successful operation result
     */
    public TransferOperationResult<T> ok() {
        final TransferOperationResult<T> transferOperationResult = new TransferOperationResult<>();
        transferOperationResult.setMessage(OK_MESSAGE);
        transferOperationResult.setSuccess(true);
        return transferOperationResult;
    }

    /**
     * Create operation resultwith error.
     *
     * @param message error message
     * @return operation result with error
     */
    public TransferOperationResult<T> error(final String message) {
        final TransferOperationResult<T> transferOperationResult = new TransferOperationResult<>();
        transferOperationResult.setMessage(message);
        transferOperationResult.setCode(1);
        transferOperationResult.setSuccess(false);
        return transferOperationResult;
    }
}
