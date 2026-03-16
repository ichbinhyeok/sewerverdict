package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageTableRow {

	private List<String> cells = new ArrayList<>();

	public List<String> getCells() {
		return cells;
	}

	public void setCells(List<String> cells) {
		this.cells = cells == null ? new ArrayList<>() : new ArrayList<>(cells);
	}
}
