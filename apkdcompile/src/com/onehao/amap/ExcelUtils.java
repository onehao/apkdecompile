/*
 * ################################################################################
 * 
 *    Copyright (c) 2015 Baidu.com, Inc. All Rights Reserved
 *
 *  version: v1
 *  
 *  
 * ################################################################################
 */
package com.onehao.amap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/***
 * Utility class that encapsulate the logic for test cases construction,
 * execution.
 * 
 * @author wanhao01
 * 
 *
 */
public class ExcelUtils {
	private static final Logger logger = LogManager.getLogger(Thread
			.currentThread().getStackTrace()[1].getClassName());
	private static final String GETTER_PREFIX = "get";
	private static final String SETTER_PREFIX = "set";
	private static String category = "";
	private static final String CATEGORY_HEADER = "CATEGORY";

	/**
	 * Check whether the reading of the excel need to be finished. in this story
	 * use two blank line to determine stop.
	 * 
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public static boolean isEnd(Sheet sheet, char columnChar, int rowNum) {
		// as description for a test case is a required field, we use it for
		// checking end.
		// when reach 2 continuous line in hierarchy is blank or null then stop
		// parsing test cases.
		Row row = sheet.getRow(rowNum);
		if (null == row) {
			return true;
		}
		int column = columnChar - 'A';
		Cell cell = row.getCell(column);
		Cell nextCell;
		if (cell.getStringCellValue() == null
				|| cell.getStringCellValue().length() <= 0) {
			nextCell = sheet.getRow(rowNum + 1).getCell(column);
			if (nextCell.getStringCellValue() == null
					|| nextCell.getStringCellValue().length() <= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Construct the Excel Workbook entity from the specific excel location.
	 * 
	 * @param excelFileLocation
	 * @return
	 */
	public static Workbook getWorkbook(String excelFileLocation) {
		InputStream in = null;
		Workbook wb = null;
		Throwable exception = null;
		File file = new File(excelFileLocation);
		// 判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			in = new FileInputStream(excelFileLocation);
			try {
				wb = WorkbookFactory.create(in);
			} catch (EncryptedDocumentException | InvalidFormatException
					| IOException e) {
				exception = e;
			}
		} catch (FileNotFoundException e) {
			exception = e;
		} finally {
			if (null != exception) {
				logger.error("{}\n{}", exception.getMessage(),
						exception.getStackTrace());
			}
		}
		return wb;
	}

	/**
	 * Check whether the reading of the excel need to be finished. in this story
	 * use two blank line to determine stop.
	 * 
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public static boolean isEnd(Sheet sheet, int rowNum, char columnChar) {
		// as description for a test case is a required field, we use it for
		// checking end.
		// when reach 2 continuous line in hierarchy is blank or null then stop
		// parsing test cases.
		Row row = sheet.getRow(rowNum);
		int column = columnChar - 'A';
		Cell cell = row.getCell(column);
		Cell nextCell;
		if (cell.getStringCellValue() == null
				|| cell.getStringCellValue().length() <= 0) {
			nextCell = sheet.getRow(rowNum + 1).getCell(column);
			if (nextCell.getStringCellValue() == null
					|| nextCell.getStringCellValue().length() <= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the string value cellID is a valid excel cell id. if this
	 * function we used a brute way to deal with the value, the fast fail is
	 * when the row isn't a string, then we consider this value isn't a cell id,
	 * and we don't support the column value that greater than Z, as for excel
	 * we support "AA", "AAA", but in the test we usually don't have that much
	 * properties, and the framework is currently used internally, will add this
	 * support only when needed.
	 * 
	 * @param cellID
	 * @return
	 */
	public static boolean isCell(String cellID) {
		boolean isCell = true;
		try {
			Integer.parseInt(cellID.substring(1));

		} catch (NumberFormatException e) {
			isCell = false;
		}
		return isCell;
	}

	/**
	 * Getting value as string from excel cell, currently we only support values
	 * of <String, numeric, boolean>
	 * 
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	public static String getCellValueAsString(Sheet sheet, Row row, int column) {
		Cell cell = row.getCell(column);
		String value = "";
		if (cell == null) {
			return value;
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			// System.out.println(cell.getRichStringCellValue().getString());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				value = cell.getDateCellValue().toString();
			} else {
				value = cell.getNumericCellValue() + "";
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = cell.getBooleanCellValue() + "";
			break;
		// case Cell.CELL_TYPE_FORMULA:
		// System.out.println(cell.getCellFormula());
		// break;
		default:
			try {
				value = cell.getStringCellValue();
			} catch (Exception e) {
				logger.error("{}\n{}\n{}",
						"the cell value type is not currently supported.",
						e.getMessage(), e.getStackTrace());
			}
		}
		return value;
	}

	/**
	 * Getting value as string from excel cell, currently we only support values
	 * of <String, numeric, boolean>
	 * 
	 * @param sheet
	 * @param row
	 * @param column
	 * @return
	 */
	public static String getCellValueAsString(Sheet sheet, int row, int column) {
		Cell cell = sheet.getRow(row).getCell(column);

		String value = "";
		if (cell == null) {
			return value;
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			// System.out.println(cell.getRichStringCellValue().getString());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				value = cell.getDateCellValue().toString();
			} else {
				value = cell.getNumericCellValue() + "";
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = cell.getBooleanCellValue() + "";
			break;
		// case Cell.CELL_TYPE_FORMULA:
		// System.out.println(cell.getCellFormula());
		// break;
		default:
			try {
				value = cell.getStringCellValue();
			} catch (Exception e) {
				logger.error("{}\n{}\n{}",
						"the cell value type is not currently supported.",
						e.getMessage(), e.getStackTrace());
			}
		}
		return value;
	}

	/**
	 * Retrieve the Getter method for the specific field.
	 * 
	 * @param field
	 * @return
	 */
	private static String retriveGetterForField(Field field) {
		return retrieveMethodForField(field, GETTER_PREFIX);
	}

	/**
	 * Retrieve the Setter method for the specific field.
	 * 
	 * @param field
	 * @return
	 */
	private static String retriveSetterForField(Field field) {
		return retrieveMethodForField(field, SETTER_PREFIX);
	}

	/**
	 * Retrieve the method with the specific prefix.
	 * 
	 * @param field
	 * @param prefix
	 * @return
	 */
	private static String retrieveMethodForField(Field field, String prefix) {
		String fieldName = field.getName();
		String method = prefix + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
		return method;
	}
}