/**
Copyright 2013 Smartsheet.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * @author kskeem
 * 
 * This is a sample application that demonstrates some basic operations on a Smartsheet using the API.
 * The intent here is to demonstrate what is possible, but does not necessarily represent a "Best Practices"
 * approach. Please see the README.md file in the Git repo to understand the story that accompanies this
 * short demonstration.
 *
 */
public class SheetStructure {

	private static final String BASE_URL = "https://api.smartsheet.com/1.1";

	private static final String ID = "{id}";

	private static final String GET_SHEETS_URL = BASE_URL + "/sheets";
	private static final String SHEET_COLUMNS_URL = BASE_URL + "/sheet/" + ID + "/columns";
	private static final String SHEET_ROWS_URL = BASE_URL + "/sheet/" + ID + "/rows";
	private static final String ROW_URL = BASE_URL + "/row/" + ID;
	private static final String COLUMN_URL = BASE_URL + "/column/" + ID;
	
	public static void main(String[] args) {
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		
		//We are using Jackson JSON parser to serialize and deserialize the JSON. See http://wiki.fasterxml.com/JacksonHome
		//Feel free to use which ever library you prefer.
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
		String accessToken = "";//Insert your access token here.
		
		try {
			
			System.out.println("Starting HelloSmartsheet2: Betty's Bake Sale...");
			//First Create a new sheet.
			String sheetName = "Betty's Bake Sale";
			//We will be using POJOs to represent the REST request objects. We will convert these to and from JSON using Jackson JSON.
			//Their structure directly relates to the JSON that gets passed through the API.
			//Note that these POJOs are included as static inner classes to keep this to one file. Normally they would be broken out.
			Sheet newSheet = new Sheet();
			newSheet.setName(sheetName);
			newSheet.setColumns(Arrays.asList(
					new Column("Baked Goods", 	"TEXT_NUMBER", 	null, 	true, 	null),
					new Column("Baker", 		"CONTACT_LIST", null, 	null, 	null),
					new Column("Price Per Item","TEXT_NUMBER", 	null, 	null, 	null),
					new Column("Gluten Free?", 	"CHECKBOX", 	"FLAG",	null, 	null),
					new Column("Status", 		"PICKLIST", 	null, 	null, 	Arrays.asList("Started", "Finished", "Delivered"))
					));
			connection = (HttpURLConnection) new URL(GET_SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newSheet);
			Result<Sheet> newSheetResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Sheet>>() {});
			newSheet = newSheetResult.getResult();
			System.out.println("Sheet " + newSheet.getName() + " created, id: "+ newSheet.getId());
			
			//Now add a column:
			String columnName = "Delivery Date";
			System.out.println("Adding column " + columnName + " to " + sheetName);
			Column newColumn = new Column(columnName, "DATE", 5);
			
			connection = (HttpURLConnection) new URL(SHEET_COLUMNS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newColumn);
			Result<Column> newColumnResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Column>>() {});
			
			System.out.println("Column " + newColumnResult.getResult().getTitle() + " added to " + newSheet.getName());
			
			//Next, we will get the list of Columns from the API. We could figure this out based on what the server has returned in the result, but we'll just ask the API for it.
			System.out.println("Fetching " + newSheet.getName() + " sheet columns...");
			connection = (HttpURLConnection) new URL(SHEET_COLUMNS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			List<Column> allColumns = mapper.readValue(connection.getInputStream(), new TypeReference<List<Column>>(){}); 
			System.out.println("Fetched.");
			
			//Now we will be adding rows
			System.out.println("Inserting rows into " + newSheet.getName());
			List<Row>rows = new ArrayList<Row>();
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Brownies"), 
					new Cell(allColumns.get(1).id, "julieann@example.com"), 
					new Cell(allColumns.get(2).id, "$1"), 
					new Cell(allColumns.get(3).id, Boolean.TRUE), 
					new Cell(allColumns.get(4).id, "Finished"))));
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Snickerdoodles"), 
					new Cell(allColumns.get(1).id, "stevenelson@example.com"), 
					new Cell(allColumns.get(2).id, "$1"), 
					new Cell(allColumns.get(3).id, Boolean.FALSE), 
					new Cell(allColumns.get(4).id, "Delivered"),
					new Cell(allColumns.get(5).id, "2013-09-04") )));
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Rice Krispy Treats"), 
					new Cell(allColumns.get(1).id, "rickthames@example.com"), 
					new Cell(allColumns.get(2).id, "$.50"), 
					new Cell(allColumns.get(3).id, Boolean.TRUE), 
					new Cell(allColumns.get(4).id, "Started") )));
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Muffins"), 
					new Cell(allColumns.get(1).id, "sandrassmart@example.com"), 
					new Cell(allColumns.get(2).id, "$1.50"), 
					new Cell(allColumns.get(3).id, Boolean.FALSE), 
					new Cell(allColumns.get(4).id, "Finished") )));
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Chocolate Chip Cookies"), 
					new Cell(allColumns.get(1).id, "janedaniels@example.com"), 
					new Cell(allColumns.get(2).id, "$1"), 
					new Cell(allColumns.get(3).id, Boolean.FALSE), 
					new Cell(allColumns.get(4).id, "Delivered"),
					new Cell(allColumns.get(5).id, "2013-09-05"))));
			rows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Ginger Snaps"), 
					new Cell(allColumns.get(1).id, "nedbarnes@example.com"), 
					new Cell(allColumns.get(2).id, "$.50"), 
					new Cell(allColumns.get(3).id, Boolean.TRUE), 
					new Cell(allColumns.get(4).id, "Unknown", false) ))); //Note that this one is strict=false. This is because "Unknown" was not one of the original options when the column was created.
			
			RowWrapper rowWrapper = new RowWrapper();
			rowWrapper.setToBottom(true);
			rowWrapper.setRows(rows);
			
			connection = (HttpURLConnection) new URL(SHEET_ROWS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), rowWrapper);
			Result<List<Row>> newRowsResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
			System.out.println("Added " + newRowsResult.getResult().size() + " rows to " + newSheet.getName());
			
			//Move a row to the top.
			System.out.println("Moving row 6 to the top.");
			RowWrapper moveToTop = new RowWrapper();
			moveToTop.setToTop(true);
			
			connection = (HttpURLConnection) new URL(ROW_URL.replace(ID, ""+newRowsResult.getResult().get(5).getId())).openConnection();
			connection.setRequestMethod("PUT");
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), moveToTop);
			mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
			
			System.out.println("Row 6 moved to top.");
			
			//Insert empty rows for spacing
			rows = new ArrayList<Row>();
			rows.add(new Row(Arrays.asList(new Cell(allColumns.get(0).id, ""))));
			rows.add(new Row(Arrays.asList(new Cell(allColumns.get(0).id, "Delivered"))));
			rowWrapper = new RowWrapper();
			rowWrapper.setToBottom(true);
			rowWrapper.setRows(rows);
			
			connection = (HttpURLConnection) new URL(SHEET_ROWS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), rowWrapper);
			Result<List<Row>> spacerRowsResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
			System.out.println("Added " + spacerRowsResult.getResult().size() + " rows to " + newSheet.getName());
			
			
			//Move Delivered rows to be children of the last spacer row.
			System.out.println("Moving delivered rows to Delivered section...");
			Long[] deliveredRowIds = new Long[]{newRowsResult.result.get(1).getId(), newRowsResult.result.get(4).getId()};
			RowWrapper parentRowLocation = new RowWrapper();
			parentRowLocation.setParentId(spacerRowsResult.getResult().get(1).getId());
			
			for (Long deliveredId: deliveredRowIds) {
				System.out.println("Moving " + deliveredId + " to Delivered.");
				connection = (HttpURLConnection) new URL(ROW_URL.replace(ID, ""+deliveredId)).openConnection();
				connection.setRequestMethod("PUT");
				connection.addRequestProperty("Authorization", "Bearer " + accessToken);
				connection.addRequestProperty("Content-Type", "application/json");
				connection.setDoOutput(true);
				mapper.writeValue(connection.getOutputStream(), parentRowLocation);
				mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
				System.out.println("Row id " + deliveredId + " moved.");
			}
			
			System.out.println("Appending additional rows to items in progress...");
			
			List<Row>siblingRows = new ArrayList<Row>();
			siblingRows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Scones"), 
					new Cell(allColumns.get(1).id, "tomlively@example.com"), 
					new Cell(allColumns.get(2).id, "$1.50"), 
					new Cell(allColumns.get(3).id, Boolean.TRUE), 
					new Cell(allColumns.get(4).id, "Finished") )));
			siblingRows.add(new Row(Arrays.asList(
					new Cell(allColumns.get(0).id, "Lemon Bars"), 
					new Cell(allColumns.get(1).id, "rickthames@example.com"), 
					new Cell(allColumns.get(2).id, "$1"), 
					new Cell(allColumns.get(3).id, Boolean.FALSE), 
					new Cell(allColumns.get(4).id, "Started") )));
			rowWrapper = new RowWrapper();
			rowWrapper.setSiblingId(newRowsResult.getResult().get(3).getId());
			rowWrapper.setRows(siblingRows);
			
			connection = (HttpURLConnection) new URL(SHEET_ROWS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), rowWrapper);
			Result<List<Row>> siblingRowsResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
			System.out.println("Added " + siblingRowsResult.getResult().size() + " rows to "+ newSheet.getName());
			
			System.out.println("Moving Status column to index 1...");
			Column statusColumn = allColumns.get(4);
			Column moveColumn = new Column();
			moveColumn.setIndex(1);
			moveColumn.setTitle(statusColumn.title);
			moveColumn.setSheetId(newSheet.getId());
			moveColumn.setType(statusColumn.getType());
			connection = (HttpURLConnection) new URL(COLUMN_URL.replace(ID, "" + statusColumn.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			
			mapper.writeValue(connection.getOutputStream(), moveColumn);
			Result<Column> movedColumnResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Column>>() {});
			System.out.println("Moved column " + movedColumnResult.getResult().getId());
			System.out.println("Completed Hellosmartsheet2: Betty's Bake Sale.");
		} catch (IOException e) {
			InputStream is = ((HttpURLConnection) connection).getErrorStream();
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				try {
					response = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					reader.close();
					Result<?> result = mapper.readValue(response.toString(), Result.class);
					System.err.println(result.message);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		
		} catch (Exception e) {
			System.out.println("Something broke: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * A simple object to represent a Sheet. Note that when this get serialized to JSON, it would look something like this:
	 * {
	 * 		"name" : "My Sheet Name",
	 * 		"id":	7389247298349,
	 * 		"columns" : [ ....]
	 * }
	 * 
	 * @author kskeem
	 *
	 */
	public static class Sheet {
		Long id;
		String name;
		List<Column> columns;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Column> getColumns() {
			return columns;
		}
		public void setColumns(List<Column> columns) {
			this.columns = columns;
		}
	}
	
	/**
	 * A simple object to represent a Column. Note that when this get serialized to JSON, it would look something like this:
	 * {
	 * 		"title" : "My Column Title",
	 * 		"id":	7389247298349,
	 * 		"type" : "TEXT_NUMBER",
	 * 		"primary" : true,
	 * 		...
	 * }
	 * 
	 * @author kskeem
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class Column {
		Long id;
		Long sheetId;
		String title;
		String type;
		String symbol;
		Boolean primary;
		List<String> options;
		Integer index;
		private Column(){
		}
		private Column(String title, String type, String symbol, Boolean primary, List<String> options) {
			super();
			this.title = title;
			this.type = type;
			this.symbol = symbol;
			this.primary = primary;
			this.options = options;
		}
		private Column(String title, String type, Integer index) {
			super();
			this.title = title;
			this.type = type;
			this.index = index;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public Boolean getPrimary() {
			return primary;
		}
		public void setPrimary(Boolean primary) {
			this.primary = primary;
		}
		public List<String> getOptions() {
			return options;
		}
		public void setOptions(List<String> options) {
			this.options = options;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Integer getIndex() {
			return index;
		}
		public void setIndex(Integer index) {
			this.index = index;
		}
		public Long getSheetId() {
			return sheetId;
		}
		public void setSheetId(Long sheetId) {
			this.sheetId = sheetId;
		}
		
	}	
	/**
	 * A simple object to represent a Row. 
	 * @author kskeem
	 *
	 */
	public static class Row {
		Long id;
		List<Cell>cells;
		
		public Row() {
		}

		public Row(List<Cell> cells) {
			this.cells = cells;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public List<Cell> getCells() {
			return cells;
		}
		public void setCells(List<Cell> cells) {
			this.cells = cells;
		}
	}
	/**
	 * A simple object to represent a Cell. 
	 * @author kskeem
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class Cell {
		Object value;
		String displayValue;
		Long columnId;
		Boolean strict;
		
		public Cell() {
		}
		
		public Cell(Long columnId, Object value) {
			this.columnId = columnId;
			this.value = value;
		}
		public Cell(Long columnId, Object value, boolean strict) {
			this.columnId = columnId;
			this.value = value;
			this.strict = strict;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getDisplayValue() {
			return displayValue;
		}
		public void setDisplayValue(String displayValue) {
			this.displayValue = displayValue;
		}

		public Long getColumnId() {
			return columnId;
		}

		public void setColumnId(Long columnId) {
			this.columnId = columnId;
		}

		public Boolean getStrict() {
			return strict;
		}

		public void setStrict(Boolean strict) {
			this.strict = strict;
		}
	}
	
	/**
	 * A Simple Class to represent a response from the server when a POST or PUT occurs. Note
	 * that the generic member will correlate to the object you create/modify through the API.
	 * @author kskeem
	 *
	 * @param <T>
	 */
	public static class Result<T> {
		String message;
		T result;
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}


		public void setResult(T result) {
			this.result = result;
		}

		public T getResult() {
			return result;
		}

	}
	/**
	 * This class serves as a wrapper to contain multiple rows when inserting them, as well as provides 
	 * a way to describe a row location. One is used for POSTs and the other for PUTs
	 * @author kskeem
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class RowWrapper {
		Boolean toBottom;
		Boolean toTop;
		Long parentId;
		Long siblingId;
		
		List<Row> rows;
		
		public Boolean getToBottom() {
			return toBottom;
		}

		public void setToBottom(Boolean toBottom) {
			this.toBottom = toBottom;
		}

		public List<Row> getRows() {
			return rows;
		}

		public void setRows(List<Row> rows) {
			this.rows = rows;
		}

		public Boolean getToTop() {
			return toTop;
		}

		public void setToTop(Boolean toTop) {
			this.toTop = toTop;
		}

		public Long getParentId() {
			return parentId;
		}

		public void setParentId(Long parentId) {
			this.parentId = parentId;
		}

		public Long getSiblingId() {
			return siblingId;
		}

		public void setSiblingId(Long siblingId) {
			this.siblingId = siblingId;
		}
	}
}
