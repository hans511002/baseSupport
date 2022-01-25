package com.sobey.jcg.support.test;

/**
 * 分页对象
 *
 * @author zhangwei
 *
 * @date 2011-7-15 16:20:48
 */
public class Page {

	private int currentPage = 1; // 当前页码

	private int rowCount = 15;// 每页行数,默认10行

	private int posStart = -1;// 数据起始索引

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * easyUi 分页参数传递绑定
	 *
	 */
	public void setRows(int rows) {
		this.rowCount = rows;
	}

	public void setPage(int page) {
		this.currentPage = page;
	}

	/**
	 * dhtmlX分页参数绑定。
	 */
	/**
	 * 起始索引。
	 * 
	 * @param posStart
	 */
	public void setPosStart(int posStart) {
		this.posStart = posStart;
	}

	public void setStart(int start) {
		this.posStart = start;
	}

	/**
	 * 获取起始索引
	 * 
	 * @return
	 */
	public int getPosStart() {
		if (this.posStart >= 0)
			return posStart;
		else {
			return (this.getCurrentPage() - 1) * this.getRowCount() + 1;
		}
	}

	/**
	 * 获取结束索引
	 * 
	 * @return
	 */
	public int getPosEnd() {
		if (this.posStart >= 0) {
			return this.getPosStart() + this.getRowCount() - 1;
		} else {
			return this.getCurrentPage() * this.getRowCount();
		}
	}

	public void setCount(int count) {
		this.rowCount = count;
	}

}
