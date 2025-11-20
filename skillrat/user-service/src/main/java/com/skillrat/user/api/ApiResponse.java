package com.skillrat.user.api;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object error;
    private Object meta;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, Object error, Object meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, null, data, null, meta);
    }

    public static <T> ApiResponse<T> fail(String message, Object error) {
        return new ApiResponse<>(false, message, null, error, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }

    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }
}
