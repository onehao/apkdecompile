package com.onehao.amap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ApkMarketShareAnalyse {
	public static void main(String[] args) throws Exception {
		String targetDirPath = "apkdownload";
		File targetDir = new File(targetDirPath);
		List<File> allFiles = new ArrayList<File>();
		allFiles = ApkMarketShareAnalyse.filterTargetFiles(targetDir,
				ApkMarketShareAnalyse.DEFAULT_APK_FILTER, allFiles);
		Workbook excel = ExcelUtils
				.getWorkbook("E:\\AliDrive\\amap\\竞品\\excel.xls");
		Sheet sheet = excel.getSheet("download");
//		 List<String> urlList = getApkUrls(sheet, 'B');
//		 downloadApkFiles(urlList, "apkdownload");
		ExecutorService service = Executors.newFixedThreadPool(8);

		String unzipFolder = "E:\\apks\\apkdownload";

		// Write the output to a file
		String resultexcelPath = "E:\\AliDrive\\amap\\竞品\\test.xls";
		Workbook excelout = ExcelUtils.getWorkbook(resultexcelPath);
		Sheet sheetout = excelout.getSheet("download");
		FileOutputStream fileOut = new FileOutputStream(resultexcelPath);
		BufferedReader reader = null;
		int i = 0;
		for (File file : allFiles) {
			System.out.println(String.format("parsing ----------------------%d-----------------",i++));
//			File folder = null;
//			try{
//			folder = new File(file.getName().substring(0, file.getName().length() - 4));
//			}catch(Exception e){
//				System.out.println("ERROR ------------------");
//			}
//			
//			if(null != folder && folder.exists()){
//				continue;
//			}

			try {
//				

//				service.execute(new Runnable() {
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						try {
//							System.out.println("------parsing " + file.getName());
//							Main.main(new String[] {
//									"d",
//									"-d",
//									targetDirPath + File.separator
//											+ file.getName(), "-f" /*
//																	 * ,"-o",
//																	 * "result"
//																	 */});
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							System.out.println("--------------" + e.getMessage());
//						}
//					}
//
//				});
			
			MapUsed mapUsed = new MapUsed();
				 String folderName = file.getName().replaceAll(".apk", "");
				 reader = new BufferedReader(new FileReader(unzipFolder +
				 File.separator + folderName
				 + File.separator + "AndroidManifest.xml"));
				 String line;
				 while ((line = reader.readLine()) != null) {
				 if (line.contains("com.baidu.location.f")) {
				 System.out.println("baidu");
				 mapUsed.setUseBaidu(true);
				 } else if (line
				 .contains("com.amap.api.location.APSService")) {
				 System.out.println("gaode");
				 mapUsed.setUseAmap(true);
				 } else if (line.contains("TencentGeoLocationSDK")) {
				 System.out.println("tencent");
				 mapUsed.setUseTencent(true);
				 }
				 }
				
				 List<File> javaFiles = new ArrayList<File>();
				 javaFiles = ApkMarketShareAnalyse.filterTargetFiles(new
				 File(unzipFolder + File.separator +
				 folderName), ApkMarketShareAnalyse.DEFAULT_JAVA_FILTER,
				 javaFiles);
				 
				 List<File> xmlFiles = ApkMarketShareAnalyse.filterTargetFiles(new
						 File(unzipFolder + File.separator +
						 folderName), ApkMarketShareAnalyse.DEFAULT_XML_FILTER,
						 javaFiles);
				
				 identifyLocationUsedInJavaFiles(javaFiles, mapUsed);
				 identifyLocationUsedInJavaFiles(xmlFiles, mapUsed);
				 
				
				 System.out.println(String.format("----------------------------%s-------------------",
				 folderName));
				 Cell cell = findCell(sheet, folderName);
				 writeMapUsage(sheetout,sheetout.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex()),mapUsed);
				 System.out.println(String.format("%%%%%%%%%% column: %s, row: %s",cell.getColumnIndex(),
				 cell.getRowIndex()));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			} finally {
				if (null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}

		 excelout.write(fileOut);
		 fileOut.flush();
		 
		 fileOut.close();
		System.exit(0);
	}

	private static void writeMapUsage(Sheet sheet, Cell cell, MapUsed mapUsed) {
		Row row = cell.getRow();
		setCellValue(cell, mapUsed, row, 1, mapUsed.isUseAmap());
		setCellValue(cell, mapUsed, row, 2, mapUsed.isUseBaidu());
		setCellValue(cell, mapUsed, row, 3, mapUsed.isUseTencent());
		setCellValue(cell, mapUsed, row, 4, mapUsed.isUseGoogle());
		setCellValue(cell, mapUsed, row, 5, mapUsed.isUseMapView());
	}

	private static void setCellValue(Cell cell, MapUsed mapUsed, Row row,
			int column, boolean value) {
		Cell result;
		int newColumn = cell.getColumnIndex() + column;
		result = row.getCell(newColumn);
		if (result == null) {
			result = row.createCell(newColumn);
		}
		result.setCellType(Cell.CELL_TYPE_BOOLEAN);
		result.setCellValue(value);
	}

	private static Cell findCell(Sheet sheet, String folderName) {
		for (int i = 1; !isEnd(sheet, 1, i); i++) {
			if (ExcelUtils.getCellValueAsString(sheet, i, 1).contains(
					folderName)) {
				return sheet.getRow(i).getCell(1);
			}
		}
		return null;
	}

	/**
	 * Check whether the reading of the excel need to be finished. in this story
	 * use two blank line to determine stop.
	 * 
	 * @param sheet
	 * @param rowNum
	 * @return
	 */
	public static boolean isEnd(Sheet sheet, int columnNum, int rowNum) {
		// as description for a test case is a required field, we use it for
		// checking end.
		// when reach 2 continuous line in hierarchy is blank or null then stop
		// parsing test cases.
		Row row = sheet.getRow(rowNum);
		if (null == row) {
			return true;
		}
		Cell cell = row.getCell(columnNum);
		Cell nextCell;
		if (cell.getStringCellValue() == null
				|| cell.getStringCellValue().length() <= 0) {
			nextCell = sheet.getRow(rowNum + 1).getCell(columnNum);
			if (nextCell.getStringCellValue() == null
					|| nextCell.getStringCellValue().length() <= 0) {
				return true;
			}
		}
		return false;
	}

	private static void identifyLocationUsedInJavaFiles(List<File> javaFiles,
			MapUsed mapUsed) {
		for (File file : javaFiles) {
			try {
				identifyLocationUsedInJavaFile(file, mapUsed);
			} catch (IOException e) {
			}
		}

	}

	private static void identifyLocationUsedInJavaFile(File file,
			MapUsed mapUsed) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("com/baidu/location")
					|| line.contains("com/baidu/mapapi/map/Location")
					|| line.contains("com//baidu//location")) {
				mapUsed.setUseBaidu(true);
			} else if (line.contains("com/amap/api/location")
					|| line.contains("com/autonavi/wtbt/Location")
					|| line.contains("com/autonavi/sdk/location")
					|| line.contains("com/autonavi/indoor/onlinelocation")
					|| line.contains("com/amap/api/service/Location")
					|| line.contains("com/amap/api/service/Ilocation")
					|| line.contains("com/amap/android/ams/location")
					|| line.contains("com/amap/api/maps/Location")) {
				System.out.println("gaode");
				mapUsed.setUseAmap(true);
			} else if (line.contains("com/tencent/open/Location")
					|| line.contains("com/tencent/msdk/lbs/Location")
					|| line.contains("com/tencent/tauth/LocationApi")
					|| line.contains("com/tencent/qq/location")) {
				System.out.println("tencent");
				mapUsed.setUseTencent(true);
			} else if (line.contains("com/google/android/gms/maps") ||
					line.contains("android/location/Location")){
				mapUsed.setUseGoogle(true);
			} else if(line.contains("MapView")){
				mapUsed.setUseMapView(true);
			}
		}
		reader.close();
	}

	static class MapUsed {
		private boolean useAmap = false;

		public boolean isUseAmap() {
			return useAmap;
		}
		
		private boolean useGoogle = false;

		public boolean isUseGoogle() {
			return useGoogle;
		}
		
		private boolean useMapView = false;

		public boolean isUseMapView() {
			return useMapView;
		}

		public void setUseMapView(boolean useMapView) {
			this.useMapView = useMapView;
		}

		public void setUseGoogle(boolean useGoogle) {
			this.useGoogle = useGoogle;
		}

		public void setUseAmap(boolean useAmap) {
			this.useAmap = useAmap;
		}

		public boolean isUseBaidu() {
			return useBaidu;
		}

		public void setUseBaidu(boolean useBaidu) {
			this.useBaidu = useBaidu;
		}

		public boolean isUseTencent() {
			return useTencent;
		}

		public void setUseTencent(boolean useTencent) {
			this.useTencent = useTencent;
		}

		private boolean useBaidu = false;
		private boolean useTencent = false;

		public boolean isFullMatch() {
			return useBaidu && useAmap && useTencent;
		}
	}

	private static void downloadApkFiles(List<String> urls, String location) {

		ExecutorService service = Executors.newFixedThreadPool(8);
		for (String url : urls) {
			// String fileUrl =
			// "http://android-apps.25pp.com//fs03/2015/11/02/1/1_5b4ad380ad61fad8cdad5867e95f0684.apk";
			// try {
			service.execute(new Runnable() {

				@Override
				public void run() {
					try {
						HttpUtil.downloadFile(
								String.format("./%s/%s", location,
										url.substring(url.lastIndexOf("/"))),
								url);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}

			});

			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

	}

	private static List<String> getApkUrls(Sheet sheet, char column) {

		List<String> urlList = new ArrayList<String>();
		String url = "";
		for (int rowNum = 1; !ExcelUtils.isEnd(sheet, column, rowNum); rowNum++) {
			url = getUrlFromLine(sheet, rowNum, column);
			if (null != url) {
				urlList.add(url);
			}
		}

		return urlList;
	}

	private static String getUrlFromLine(Sheet sheet, int rowNum, char column) {

		return ExcelUtils.getCellValueAsString(sheet, sheet.getRow(rowNum),
				column - 'A');
	}

	/**
	 * Provide default file filters
	 */
	public static FileFilter DEFAULT_JAR_FILTER = new JARFileFilter();
	public static FileFilter DEFAULT_APK_FILTER = new APKFileFilter();
	public static FileFilter DEFAULT_CLASS_FILTER = new CLASSFileFilter();
	public static FileFilter DEFAULT_JAVA_FILTER = new JAVAFileFilter();
	public static FileFilter DEFAULT_XML_FILTER = new  XMLFileFilter();

	/**
	 * This method will get all the target files under a specific directory
	 * 
	 * @param targetDir
	 * @param filter
	 * @param resultFiles
	 * @return
	 */
	public static List<File> filterTargetFiles(File targetDir,
			FileFilter filter, List<File> resultFiles) {

		/**
		 * listFiles : only list the files in the current directory, not include
		 * the files in the sub directories.
		 */
		File[] files = targetDir.listFiles(filter);
		for (File file : targetDir.listFiles()) {
			if (file.isDirectory()) {
				filterTargetFiles(file, filter, resultFiles);
			}
		}

		for (File file : files) {

			if (file.isDirectory()) {

				filterTargetFiles(file, filter, resultFiles);
			} else {

				resultFiles.add(file);
			}
		}

		return resultFiles;

	}

	private static class JARFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {

			if (file.isDirectory()) {

				return true;
			}
			String fileName = file.getName();

			return fileName.matches("(?i).+jar$");
		}

	}

	private static class APKFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {

			if (file.isDirectory()) {

				return true;
			}
			String fileName = file.getName();

			return fileName.matches("(?i).+apk$");
		}

	}

	private static class CLASSFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {

			if (file.isDirectory()) {

				return true;
			}
			String fileName = file.getName();

			return fileName.matches("(?i).+class$");
		}

	}

	private static class JAVAFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {

			if (file.isDirectory()) {

				return true;
			}
			String fileName = file.getName();

			return fileName.matches("(?i).+java$");
		}

	}
	
	private static class XMLFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {

			if (file.isDirectory()) {

				return true;
			}
			String fileName = file.getName();

			return fileName.matches("(?i).+xml$");
		}

	}

	/**
	 * Copy the jar files to the specific folder.
	 * 
	 * @param allFiles
	 * @throws Exception
	 */
	private static void copyJar(String destFolder, List<File> allFiles)
			throws Exception {
		for (File file : allFiles) {
			String pathname = destFolder + File.separator + file.getName();
			File dest = new File(pathname);
			File destPar = dest.getParentFile();
			destPar.mkdirs();
			if (!dest.exists()) {
				dest.createNewFile();
			}
			copyFile(file, dest);
		}

	}

	/***
	 * * copy file * * @param src * @param dest * @param status * @throws
	 * IOException
	 */
	private static void copyFile(File src, File dest) throws Exception {
		FileInputStream input = null;
		FileOutputStream outstrem = null;
		try {
			input = new FileInputStream(src);
			outstrem = new FileOutputStream(dest);
			outstrem.getChannel().transferFrom(input.getChannel(), 0,
					input.available());
		} catch (Exception e) {
			throw e;
		} finally {
			outstrem.flush();
			outstrem.close();
			input.close();
		}
	}

	/**
	 * Get all the files in the target directory, here we could conduct any
	 * logic for the files
	 * 
	 * @param filesList
	 */
	private static StringBuilder cascadePath(List<File> filesList) {

		StringBuilder cascadePathBuilder = new StringBuilder();
		cascadePathBuilder.append("$classpath;");

		for (File file : filesList) {

			cascadePathBuilder.append(String.format("%s;", file.getPath()));
		}
		return cascadePathBuilder;
	}
}
