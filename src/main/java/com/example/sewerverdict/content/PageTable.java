package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageTable {

	private String title;
	private String intro;
	private String note;
	private List<String> columns = new ArrayList<>();
	private List<PageTableRow> rows = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns == null ? new ArrayList<>() : new ArrayList<>(columns);
	}

	public List<PageTableRow> getRows() {
		return rows;
	}

	public void setRows(List<PageTableRow> rows) {
		this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
	}
}
