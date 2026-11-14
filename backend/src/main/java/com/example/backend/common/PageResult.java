package com.example.backend.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果，对应 PageHelper PageInfo 结构。
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize <= 0 ? 0 : (int) ((total + pageSize - 1) / pageSize));
        return result;
    }

    /** 页码归一化：从 1 开始 */
    public static int normalizePage(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    /** 每页条数归一化：默认 10，最大 50 */
    public static int normalizeSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 50);
    }
}
