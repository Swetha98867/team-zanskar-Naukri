package com.adi.naukri.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class TemplateBuilder {

    private static final String[][] ROWS = {
        {"email", "remarks"},
        {"user1@example.com", "primary account"},
        {"user2@example.com", ""},
        {"user3@example.com", "backup"},
        {"user4@example.com", ""},
        {"user5@example.com", "senior profile"}
    };

    public byte[] build() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Emails");
            Font bold = wb.createFont(); bold.setBold(true);
            CellStyle hs = wb.createCellStyle(); hs.setFont(bold);
            for (int r = 0; r < ROWS.length; r++) {
                Row row = sh.createRow(r);
                for (int c = 0; c < ROWS[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(ROWS[r][c]);
                    if (r == 0) cell.setCellStyle(hs);
                }
            }
            sh.autoSizeColumn(0);
            sh.autoSizeColumn(1);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build template", e);
        }
    }
}
