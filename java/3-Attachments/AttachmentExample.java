import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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


public class AttachmentExample {
	private static final String BASE_URL = "https://api.smartsheet.com/1.1";
	
	private static final String ID = "{id}";

	private static final String GET_SHEETS_URL = BASE_URL + "/sheets";
	private static final String SHEET_ROWS_URL = BASE_URL + "/sheet/" + ID + "/rows";
	private static final String ROW_ATTACHMENT_URL = BASE_URL + "/row/" + ID +"/attachments";
	private static final String ATTACHMENT_URL = BASE_URL + "/attachment/" + ID;
	
	public static void main(String[] args) {
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		
		//We are using Jackson JSON parser to serialize and deserialize the JSON. See http://wiki.fasterxml.com/JacksonHome
		//Feel free to use which ever library you prefer.
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
		String accessToken = "";//Insert your access token here.
		
		try {
			System.out.println("Starting HelloSmartsheet3: Attachments...");

			//Create A Sheet
			Sheet newSheet = new Sheet();
			newSheet.setName("Attachment Example");
			newSheet.setColumns(Arrays.asList(
					new Column("Column 1", 	"TEXT_NUMBER", 	null, 	true, 	null),
					new Column("Column 2", 	"TEXT_NUMBER", 	null, 	null, 	null),
					new Column("Column 3",	"TEXT_NUMBER", 	null, 	null, 	null)
					));
			connection = (HttpURLConnection) new URL(GET_SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newSheet);
			Result<Sheet> newSheetResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Sheet>>() {});
			newSheet = newSheetResult.getResult();
			System.out.println("Success! Sheet " + newSheet.getName() + " created, id: " + newSheet.getId());

			//Add a row. This will be the row we attach the document to.
			List<Row>rows = new ArrayList<Row>();
			rows.add(new Row(Arrays.asList(
					new Cell(newSheet.getColumns().get(0).id, "Value1"), 
					new Cell(newSheet.getColumns().get(1).id, "Value2"), 
					new Cell(newSheet.getColumns().get(2).id, "Value3"))));
			RowWrapper rowWrapper = new RowWrapper();
			rowWrapper.setToBottom(true);
			rowWrapper.setRows(rows);
			
			connection = (HttpURLConnection) new URL(SHEET_ROWS_URL.replace(ID, ""+newSheet.getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), rowWrapper);
			Result<List<Row>> newRowsResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<List<Row>>>() {});
			System.out.println("Added " + newRowsResult.getResult().size() + " rows of data to " + newSheet.getName());

			String pathToFile = "SampleFile.pdf"; //Provide a path to a file here.
			String downloadedPathToFile = "SampleFile-Downloaded.pdf";
			File file = new File(pathToFile);
			FileInputStream fis = new FileInputStream(file);
			long fileSize = fis.getChannel().size();
			
			connection = (HttpURLConnection) new URL(ROW_ATTACHMENT_URL.replace(ID, ""+newRowsResult.getResult().get(0).getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/pdf");
			connection.addRequestProperty("Content-Length", "" + fileSize);
			connection.addRequestProperty("Content-Disposition", "attachment; filename=\"" + file.getName() +"\"");
			connection.setDoOutput(true);
			//Note that the SmartsheetAPI does not currently support Multi-part/chunked uploads.
			copy(fis, connection.getOutputStream());
			Result<Attachment> attachmentResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Attachment>>() {});
			System.out.println("Attached " + file.getName() + " file to row " + newRowsResult.getResult().get(0).getId());

			//get the Attachment, which will now have a short-lived url for downloading the actual file.
			connection = (HttpURLConnection) new URL(ATTACHMENT_URL.replace(ID, ""+attachmentResult.getResult().getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			Attachment attachment = mapper.readValue(connection.getInputStream(), Attachment.class); 

			//Now download the file.
			FileOutputStream fos = new FileOutputStream(downloadedPathToFile); //Provide a path for the output file here
			connection = (HttpURLConnection) new URL(attachment.getUrl()).openConnection();
			copy(connection.getInputStream(), fos);
			fos.close();

			System.out.println("File " + downloadedPathToFile + " downloaded and saved locally.");

			//Attach a URL to a row
			Attachment urlAttachment = new Attachment();
			urlAttachment.setName("My link to Google");
			urlAttachment.setUrl("http://www.google.com");
			urlAttachment.setAttachmentType("LINK");
			
			connection = (HttpURLConnection) new URL(ROW_ATTACHMENT_URL.replace(ID, ""+newRowsResult.getResult().get(0).getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), urlAttachment);
			Result<Attachment> urlAttachmentResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Attachment>>() {});
			System.out.println("Attached the " + urlAttachmentResult.getResult().getName() + " URL to row " + newRowsResult.getResult().get(0).getId());
			//Now delete the attachment.
			connection = (HttpURLConnection) new URL(ATTACHMENT_URL.replace(ID, ""+urlAttachmentResult.getResult().getId())).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.setRequestMethod("DELETE");
			Result<Object> deleteResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Object>>(){});
			System.out.println(deleteResult.getMessage());

			System.out.println("All done");
			
		} catch (IOException e) {
			
			InputStream is = connection == null ? null :  ((HttpURLConnection) connection).getErrorStream();
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
	
	public static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		input.close();
		return count;
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
		Column(){
		}
		Column(String title, String type, String symbol, Boolean primary, List<String> options) {
			super();
			this.title = title;
			this.type = type;
			this.symbol = symbol;
			this.primary = primary;
			this.options = options;
		}
		Column(String title, String type, Integer index) {
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

	/**
	 * This class serves as a wrapper to contain multiple rows when inserting them, as well as provides 
	 * a way to describe a row location. One is used for POSTs and the other for PUTs
	 * @author kskeem
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class Attachment {
		Long id;
		String name;
		String url;
		String attachmentType;
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
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getAttachmentType() {
			return attachmentType;
		}
		public void setAttachmentType(String attachmentType) {
			this.attachmentType = attachmentType;
		}
	}
}
