package com.tortoise.page;

import java.io.Serializable;

/**
 * Created by tortoise on 16/11/9.
 */
public class Page implements Serializable{
    private static final long serialVersionUID = 1L;
    protected int pageSize = 10; // default 10 counts every page
    protected int currentPage = 1; // current page
    protected int totalPages = 0; // totalPages
    protected int totalRows = 0; // totalRows
    protected int pageStartRow = 0; // pageStartRow
    protected int pageEndRow = 0; // pageEndRow
    protected boolean pagination = false; //whether pagination
    boolean hasNextPage = false; // hasNextPage
    boolean hasPreviousPage = false; // hasPreviousPage
    protected String sortName;
    protected String sortOrder;

    protected String pagedView; // pagedView

    protected String pagedDiv;

    Object obj;

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getPagedDiv() {
        return pagedDiv;
    }



    public Page(int rows, int pageSize) {
        this.init(rows, pageSize);
    }

    public Page() {

    }

    /**
     * initalization page params : set totalRows first
     *
     */

    public void init(int rows, int pageSize) {

        this.pageSize = pageSize;

        this.totalRows = rows;

        if ((totalRows % pageSize) == 0) {
            totalPages = totalRows / pageSize;
        } else {
            totalPages = totalRows / pageSize + 1;
        }

    }

    public void init(int rows, int pageSize, int currentPage) {

        this.pageSize = pageSize;

        this.totalRows = rows;

        if ((totalRows % pageSize) == 0) {
            totalPages = totalRows / pageSize;
        } else {
            totalPages = totalRows / pageSize + 1;
        }
        if (currentPage != 0)
            gotoPage(currentPage);
//        setPagedView();
    }

    /**
     * calculate current pages ：pageStartRow & pageEndRow
     *
     */
    private void calculatePage() {
        hasPreviousPage = (currentPage - 1) > 0;

        hasNextPage = currentPage < totalPages;

        if (currentPage * pageSize < totalRows) { // judge is the last page
            pageEndRow = currentPage * pageSize;
            pageStartRow = pageEndRow - pageSize;
        } else {
            pageEndRow = totalRows;
            pageStartRow = pageSize * (totalPages - 1);
        }

    }

    /**
     * goto the page
     *
     * @param page
     */
    public void gotoPage(int page) {

        currentPage = page;

        calculatePage();

        // debug1();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getPageStartRow() {
        return pageStartRow;
    }

    public void setPageStartRow(int pageStartRow) {
        this.pageStartRow = pageStartRow;
    }

    public int getPageEndRow() {
        return pageEndRow;
    }

    public void setPageEndRow(int pageEndRow) {
        this.pageEndRow = pageEndRow;
    }

    public boolean isPagination() {
        return pagination;
    }

    public void setPagination(boolean pagination) {
        this.pagination = pagination;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public String getPagedView() {
        return pagedView;
    }

    public void setPagedView(String pagedView) {
        this.pagedView = pagedView;
    }

    public void setPagedDiv(String pagedDiv) {
        this.pagedDiv = pagedDiv;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
