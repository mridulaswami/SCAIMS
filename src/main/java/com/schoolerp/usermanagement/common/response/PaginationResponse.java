package com.schoolerp.usermanagement.common.response;

public class PaginationResponse<T> extends ApiResponse<T> {
    private long totalElements;
    private int page;
    private int size;

    public PaginationResponse() {}

    public PaginationResponse(T data, long totalElements, int page, int size) {
        super(true, null, data);
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
    }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
