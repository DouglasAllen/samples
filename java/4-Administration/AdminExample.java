import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


public class AdminExample {
	
	private static final String BASE_URL = "https://api.smartsheet.com/1.1";

	private static final String ID = "{id}";

	private static final String USERS_URL = BASE_URL + "/users";
	private static final String USER_URL = BASE_URL + "/user/" + ID;
	private static final String SHEETS_URL = BASE_URL + "/sheets";
	private static final String USERS_SHEETS_URL = USERS_URL + "/sheets";
	
	public static void main(String[] args) {
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		
		//We are using Jackson JSON parser to serialize and deserialize the JSON. See http://wiki.fasterxml.com/JacksonHome
		//Feel free to use which ever library you prefer.
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
		String accessToken = "";//Insert your access token here. Note this must be from an account that is an Admin of an account.
		String user1Email = ""; //You need access to these two email account.
		String user2Email = ""; //Note Gmail and Hotmail allow email aliasing. 
							    //joe@gmail.com will get email sent to joe+user1@gmail.com 
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Adding user " + user1Email);

			//Add the users:
			User user = new User();
			user.setEmail(user1Email);
			user.setAdmin(false);
			user.setLicensedSheetCreator(true);
			
			connection = (HttpURLConnection) new URL(USERS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), user);
			Result<User> newUser1Result = mapper.readValue(connection.getInputStream(), new TypeReference<Result<User>>() {});
			System.out.println("User " + newUser1Result.result.email + " added with userId " + newUser1Result.result.getId());
			
			user = new User();
			user.setEmail(user2Email);
			user.setAdmin(true);
			user.setLicensedSheetCreator(true);
			
			connection = (HttpURLConnection) new URL(USERS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), user);
			Result<User> newUser2Result = mapper.readValue(connection.getInputStream(), new TypeReference<Result<User>>() {});
			System.out.println("User " + newUser2Result.result.email + " added with userId " + newUser2Result.result.getId());
			System.out.println("Please visit the email inbox for the users " + user1Email + " and  " + user2Email +" and confirm membership to the account.");
			System.out.print("Press Enter to continue"); in.readLine();
			
			//List all the users of the org
			connection = (HttpURLConnection) new URL(USERS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			List<User> users = mapper.readValue(connection.getInputStream(), new TypeReference<List<User>>() {});
			
			System.out.println("The following are members of your account: ");
			
			for (User orgUser : users) {
				System.out.println("\t" + orgUser.getEmail());
			}
			
			//Create a sheet as the admin
			Sheet newSheet = new Sheet();
			newSheet.setName("Admin's Sheet");
			newSheet.setColumns(Arrays.asList(
					new Column("Column 1", 	"TEXT_NUMBER", 	null, 	true, 	null),
					new Column("Column 2", 	"TEXT_NUMBER", null, 	null, 	null),
					new Column("Column 3",	"TEXT_NUMBER", 	null, 	null, 	null)
					));
			connection = (HttpURLConnection) new URL(SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newSheet);
			mapper.readValue(connection.getInputStream(), new TypeReference<Result<Sheet>>() {});
			
			//Create a sheet as user1
			newSheet = new Sheet();
			newSheet.setName("Admin's Sheet");
			newSheet.setColumns(Arrays.asList(
					new Column("Column 1", 	"TEXT_NUMBER", 	null, 	true, 	null),
					new Column("Column 2", 	"TEXT_NUMBER", null, 	null, 	null),
					new Column("Column 3",	"TEXT_NUMBER", 	null, 	null, 	null)
					));
			connection = (HttpURLConnection) new URL(SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			//Here is where the magic happens - Any action performed in this call will be on behalf of the
			//user provided. Note that this person must be a confirmed member of your org. 
			//Also note that the email address is url-encoded.
			connection.addRequestProperty("Assume-User", URLEncoder.encode(user1Email, "UTF-8")); 
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newSheet);
			mapper.readValue(connection.getInputStream(), new TypeReference<Result<Sheet>>() {});

			//Create a sheet as user2
			newSheet = new Sheet();
			newSheet.setName("Admin's Sheet");
			newSheet.setColumns(Arrays.asList(
					new Column("Column 1", 	"TEXT_NUMBER", 	null, 	true, 	null),
					new Column("Column 2", 	"TEXT_NUMBER", null, 	null, 	null),
					new Column("Column 3",	"TEXT_NUMBER", 	null, 	null, 	null)
					));
			connection = (HttpURLConnection) new URL(SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Assume-User", URLEncoder.encode(user2Email, "UTF-8")); 
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			mapper.writeValue(connection.getOutputStream(), newSheet);
			mapper.readValue(connection.getInputStream(), new TypeReference<Result<Sheet>>() {});
			
			//List all the sheets in the org:
			System.out.println("The following sheets are owned by members of your account: ");
			connection = (HttpURLConnection) new URL(USERS_SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			List<Sheet> allSheets = mapper.readValue(connection.getInputStream(), new TypeReference<List<Sheet>>() {});
			
			for (Sheet orgSheet : allSheets) {
				System.out.println("\t" + orgSheet.getName() + " - " + orgSheet.getOwner());
			}
			
			//Now delete user1 and transfer their sheets to user2
			connection = (HttpURLConnection) new URL(USER_URL.replace(ID, newUser1Result.getResult().getId() + "") + "?transferTo=" + newUser2Result.getResult().getId()).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Assume-User", URLEncoder.encode(user2Email, "UTF-8")); 
			connection.addRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("DELETE");
			Result<Object> resultObject = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Object>>() {});
			
			System.out.println("Sheets transferred : " + resultObject.getSheetsTransferred());
			
			
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
	 * 		"owner" : "anemail@address.com",
	 * 		"ownerId": 849245949345
	 * }
	 * 
	 * @author kskeem
	 *
	 */
	public static class Sheet {
		Long id;
		String name;
		Long ownerId;
		String owner;
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
		public Long getOwnerId() {
			return ownerId;
		}
		public void setOwnerId(Long ownerId) {
			this.ownerId = ownerId;
		}
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
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
	 * A simple object to represent a User. Note that when this get serialized to JSON, it would look something like this:
	 * {
	 * 		"name" : "My Sheet Name",
	 * 		"id":	7389247298349,
	 * 		"columns" : [ ....]
	 * }
	 * 
	 * @author kskeem
	 *
	 */
	@JsonInclude(Include.NON_NULL)
	public static class User {
		Long id;
		String firstName;
		String lastName;
		String fullName;
		String email;
		Boolean admin;
		Boolean licensedSheetCreator;
		String status;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		public String getFullName() {
			return fullName;
		}
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public Boolean getAdmin() {
			return admin;
		}
		public void setAdmin(Boolean admin) {
			this.admin = admin;
		}
		public Boolean getLicensedSheetCreator() {
			return licensedSheetCreator;
		}
		public void setLicensedSheetCreator(Boolean licensedSheetCreator) {
			this.licensedSheetCreator = licensedSheetCreator;
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
		Integer sheetsTransferred;
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

		public Integer getSheetsTransferred() {
			return sheetsTransferred;
		}

		public void setSheetsTransferred(Integer sheetsTransferred) {
			this.sheetsTransferred = sheetsTransferred;
		}

	}
}