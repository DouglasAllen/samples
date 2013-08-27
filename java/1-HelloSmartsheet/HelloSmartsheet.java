/**
Copyright 2013 Smartsheet, Inc.

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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



public class HelloSmartsheet {

	private static final String BASE_URL = "https://api.smartsheet.com/1.1";
	private static final String GET_SHEETS_URL = BASE_URL + "/sheets";
	
	private static final String SHEET_ID = "{sheetId}";
	private static final String SHARE_SHEET_URL = BASE_URL + "/sheet/" + SHEET_ID + "/shares";
	
	public static void main(String[] args) {
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		
		//We are using Jackson JSON parser to deserialize the JSON. See http://wiki.fasterxml.com/JacksonHome
		//Feel free to use which ever library you prefer.
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
		
		try {
			
			System.out.println("STARTING HelloSmartsheet...");
			//Create a BufferedReader to read user input.
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			System.out.print("Enter Smartsheet API access token:");
			String accessToken = in.readLine();
			System.out.println("Fetching list of your sheets...");
			//Create a connection and fetch the list of sheets
			connection = (HttpURLConnection) new URL(GET_SHEETS_URL).openConnection();
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			//Read the response line by line.
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			//Use Jackson to convert the JSON string to a List of Sheets
			List<Sheet> sheets = mapper.readValue(response.toString(), new TypeReference<List<Sheet>>() {});
			if (sheets.size() == 0) {
				System.out.println("You don't have any sheets.  Goodbye!");
				return;
			}
			System.out.println("Total sheets: " + sheets.size());
			int i = 1;
			for (Sheet sheet : sheets) {
				System.out.println( i++ + ": " + sheet.name);
			}
			System.out.print("Enter the number of the sheet you want to share: ");
			
			//Prompt the user to provide the sheet number, the email address, and the access level
			Integer sheetNumber = Integer.parseInt(in.readLine().trim()); //NOTE: for simplicity, error handling and input validation is neglected.
			Sheet chosenSheet = sheets.get(sheetNumber - 1);
			
			System.out.print("Enter an email address to share " + chosenSheet.getName() + " to: ");
			String email = in.readLine();
			
			System.out.print("Choose an access level (VIEWER, EDITOR, EDITOR_SHARE, ADMIN) for " + email + ": " );
			String accessLevel = in.readLine();
			
			//Create a share object
			Share share = new Share();
			share.setEmail(email);
			share.setAccessLevel(accessLevel);
			
			System.out.println("Sharing " + chosenSheet.name + " to " + email + " as " + accessLevel + ".");
			
			
			//Create a connection. Note the SHARE_SHEET_URL uses /sheet as opposed to /sheets (with an 's')
			connection = (HttpURLConnection) new URL(SHARE_SHEET_URL.replace(SHEET_ID, "" + chosenSheet.getId())).openConnection();
			connection.setDoOutput(true);
			connection.addRequestProperty("Authorization", "Bearer " + accessToken);
			connection.addRequestProperty("Content-Type", "application/json");
			
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			//Serialize the Share object
			writer.write(mapper.writeValueAsString(share));
			writer.close();
			
			//Read the response and parse the JSON
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			response = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			
			Result result = mapper.readValue(response.toString(), Result.class);
			System.out.println("Sheet shared successfully, share ID " + result.result.id);
			System.out.println("Press any key to quit.");
			in.read();
			
		} catch (IOException e) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream()));
			String line;
			try {
				response = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
				Result result = mapper.readValue(response.toString(), Result.class);
				System.out.println(result.message);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		} catch (Exception e) {
			System.out.println("Something broke: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	public static class Sheet {
		Long id;
		String name;
		
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
	}
	
	public static class Share {
		String email;
		String accessLevel;
		Long id;
		
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getAccessLevel() {
			return accessLevel;
		}
		public void setAccessLevel(String accessLevel) {
			this.accessLevel = accessLevel;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
	}
	
	public static class Result {
		String message;
		Share result;
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}

		public Share getResult() {
			return result;
		}

		public void setResult(Share result) {
			this.result = result;
		}

	}
}
