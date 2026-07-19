package com.adi.naukri.excel;

public record ParsedEmailRow(int rowNumber, String email, String remarks, boolean valid, String error) {}
