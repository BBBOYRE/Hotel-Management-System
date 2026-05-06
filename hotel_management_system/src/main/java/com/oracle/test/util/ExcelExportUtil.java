package com.oracle.test.util;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class ExcelExportUtil {

    private ExcelExportUtil() {}

    /**
     * 将一个 Map 列表导出为 .xlsx 写入 HTTP 响应流。
     *
     * @param fileName 浏览器看到的文件名（不带后缀）
     * @param headers  表头与列对应关系：key=数据 Map 的字段名, value=展示列名
     * @param rows     数据行
     */
    public static void export(HttpServletResponse response,
                              String fileName,
                              Map<String, String> headers,
                              List<Map<String, Object>> rows) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(fileName);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headRow = sheet.createRow(0);
            int col = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Cell cell = headRow.createCell(col);
                cell.setCellValue(entry.getValue());
                cell.setCellStyle(headerStyle);
                col++;
            }

            int r = 1;
            for (Map<String, Object> row : rows) {
                Row dataRow = sheet.createRow(r++);
                int c = 0;
                for (String key : headers.keySet()) {
                    Cell cell = dataRow.createCell(c++);
                    Object v = row.get(key);
                    if (v == null) {
                        cell.setCellValue("");
                    } else if (v instanceof Number) {
                        cell.setCellValue(((Number) v).doubleValue());
                    } else {
                        cell.setCellValue(v.toString());
                    }
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }

            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + encoded + ".xlsx");
            try (OutputStream os = response.getOutputStream()) {
                wb.write(os);
                os.flush();
            }
        }
    }
}
