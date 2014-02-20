import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.AccessLevel;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.ColumnType;
import com.smartsheet.api.models.Comment;
import com.smartsheet.api.models.Discussion;
import com.smartsheet.api.models.Folder;
import com.smartsheet.api.models.Home;
import com.smartsheet.api.models.MultiShare;
import com.smartsheet.api.models.ObjectInclusion;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.RowWrapper;
import com.smartsheet.api.models.Share;
import com.smartsheet.api.models.Sheet;
import com.smartsheet.api.models.SheetPublish;
import com.smartsheet.api.models.User;
import com.smartsheet.api.models.Workspace;
import com.smartsheet.api.oauth.AuthorizationResult;
import com.smartsheet.api.oauth.OAuthFlow;
import com.smartsheet.api.oauth.OAuthFlowBuilder;
import com.smartsheet.api.oauth.Token;

public class JavaSDKSample {

	/**
	 * This provides an example of how to use OAuth to generate a Token from a third party application. It handles
	 * requesting the authorization code, sending the user to a specific website to request access and then getting 
	 * the access token to use for all future requests.
	 */
	public static void OAuthExample() throws SmartsheetException, UnsupportedEncodingException, URISyntaxException, 
		NoSuchAlgorithmException {
		
		// Setup the information that is necessary to request an authorization code
		OAuthFlow oauth = new OAuthFlowBuilder().setClientId("YOUR_CLIENT_ID").setClientSecret("YOUR_CLIENT_SECRET").
			setRedirectURL("https://YOUR_DOMAIN.com/").build();
		
		// Create the URL that the user will go to grant authorization to the application
		String url = oauth.newAuthorizationURL(EnumSet.of(com.smartsheet.api.oauth.AccessScope.CREATE_SHEETS, 
				com.smartsheet.api.oauth.AccessScope.WRITE_SHEETS), "key=YOUR_VALUE");
		
		// Take the user to the following URL
		System.out.println(url);

		// After the user accepts or declines the authorization they are taken to the redirect URL. The URL of the page
		// the user is taken to can be used to generate an authorization Result object.
		String authorizationResponseURL = "https://yourDomain.com/?code=l4csislal82qi5h&expires_in=239550&state=key%3D12344";
		
		// On this page pass in the full URL of the page to create an authorizationResult object  
		AuthorizationResult authResult = oauth.extractAuthorizationResult(authorizationResponseURL);
		
		// Get the token from the authorization result
		Token token = oauth.obtainNewToken(authResult);
		
		// Save the token or use it.
	}

	public static void main(String[] args) throws SmartsheetException, UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException {
		// Use the Smartsheet Builder to create a Smartsheet
		Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken("YOUR_ACCESS_TOKEN").build();

		// Get home
		Home home = smartsheet.home().getHome(EnumSet.of(ObjectInclusion.TEMPLATES));
		
		// List home folders
		List<Folder> homeFolders = home.getFolders();
		for(Folder folder : homeFolders){
		    System.out.println("folder:"+folder.getName());
		}
		
		// List Sheets
		List<Sheet> homeSheets = smartsheet.sheets().listSheets();
		for(Sheet sheet : homeSheets){
		    System.out.println("sheet:"+sheet.getName());
		}

		// Create folder in home
		Folder folder = new Folder();
		folder.setName("New Folder");
		folder = smartsheet.home().folders().createFolder(folder);
		System.out.println("Folder ID:"+folder.getId()+", Folder Name:"+folder.getName());

		// Setup checkbox Column Object
		Column checkboxColumn = new Column.AddColumnToSheetBuilder().setType(ColumnType.CHECKBOX).setTitle("Finished").build();
		// Setup text Column Object
		Column textColumn = new Column.AddColumnToSheetBuilder().setPrimary(true).setTitle("To Do List").setType(ColumnType.TEXT_NUMBER).build();
		// Add the 2 Columns (flag & text) to a new Sheet Object
		Sheet sheet = new Sheet.CreateSheetBuilder().setName("New Sheet").setColumns(Arrays.asList(checkboxColumn, textColumn)).build();
		// Send the request to create the sheet @ Smartsheet
		sheet = smartsheet.sheets().createSheet(sheet);

		// Update two cells on a row
		List<Cell> cells = new Cell.UpdateRowCellsBuilder().addCell(12655504427181956L, "test11").
				addCell(17159104054552452L, "test22").build();
		smartsheet.rows().updateCells(11712752478709636L, cells);
		//=========================================
		
		
		// Create a row and sheet level discussion with an initial comment
		Comment comment = new Comment.AddCommentBuilder().setText("Hello World").build();
		Discussion discussion = new Discussion.CreateDiscussionBuilder().setTitle("New Discussion").
			setComment(comment).build();
		smartsheet.rows().discussions().createDiscussion(7342252012922756L, discussion);
		smartsheet.sheets().discussions().createDiscussion(921940333488004L, discussion);
		//=========================================
		
		
		// Update a folder name
		folder = new Folder.UpdateFolderBuilder().setName("A Brand New New Folder").setId(2984938485114756L).build();
		smartsheet.folders().updateFolder(folder);
		//=========================================
		
		
		// Create 3 users to share a sheet with
		List<User> users = new ArrayList<User>();
		
		User user = new User();
		user.setEmail("brett@batie.com");
		users.add(user);
		
		User user1 = new User();
		user1.setEmail("bbatie@gmail.com");
		users.add(user1);
		
		User user2 = new User();
		user2.setEmail("brett.batie@smartsheet.com");
		users.add(user2);
		
		// Add the message, subject & users to share with
		MultiShare multiShare = new MultiShare.ShareToManyBuilder().setMessage("Here is the sheet I am sharing with you").
			setAccessLevel(AccessLevel.VIEWER).setSubject("Sharing a Smartsheet with you").setUsers(users).build();
		
		// Share the specified sheet with the users.
		smartsheet.sheets().shares().shareTo(921940333488004L, multiShare, true);
		//=========================================
		
		
		// Create a single share to a specified email address with the specified access level
		Share share = new Share.ShareToOneBuilder().setEmail("bbatie@gmail.com").setAccessLevel(AccessLevel.EDITOR)
				.build();
		// Add the share to a specific sheet
		smartsheet.sheets().shares().shareTo(921940333488004L, share);
		//=========================================
		
		
		// Create a share with the specified access level
		share = new Share.UpdateShareBuilder().setAccessLevel(AccessLevel.VIEWER).build();
		// Update the share permission on the specified sheet for the specified user.
		smartsheet.sheets().shares().updateShare(921940333488004L, 8166691168380804L, share);
		//=========================================
		
		
		// Create 3 cells
		Cell cell = new Cell();
		cell.setValue("Cell1");
		cell.setColumnId(4907304240867204L);
		Cell cell2 = new Cell();
		cell2.setValue("Cell2");
		cell2.setColumnId(2655504427181956L);
		Cell cell3 = new Cell();
		cell3.setValue("cell3");
		cell3.setColumnId(7159104054552452L);
		
		// Store the cells in a list
		List<Cell> cells1 = new ArrayList<Cell>();
		cells1.add(cell);
		cells1.add(cell2);
		cells1.add(cell3);
		
		// Create a row and add the list of cells to the row
		Row row = new Row();
		row.setCells(cells1);
		
		// Add two rows to a list of rows.
		List<Row> rows = new ArrayList<Row>();
		rows.add(row);
		rows.add(row);
		
		// Add the rows to the row wrapper and set the location to insert the rows
		RowWrapper rowWrapper = new RowWrapper.InsertRowsBuilder().setRows(rows).setToBottom(true).build();
		
		
		// Add the rows to the specified sheet
		smartsheet.sheets().rows().insertRows(921940333488004L, rowWrapper);
		

		// Setup a row to be moved to the top of a sheet
		RowWrapper rowWrapper1 = new RowWrapper.MoveRowBuilder().setToTop(true).build();
		// Move the specified row
		smartsheet.rows().moveRow(6089772474099588L, rowWrapper1);
		//=========================================
		
		
		// Create a sheet that is a copy of a template
		Sheet sheet1 = new Sheet.CreateFromTemplateOrSheetBuilder().setFromId(4852037272790916L).
				setName("Copy of a Template").build();
		// Create the new sheet from the template
		smartsheet.sheets().createSheetFromExisting(sheet1, EnumSet.allOf(ObjectInclusion.class));
		//=========================================
		
		
		// Setup a sheet with a new name
		Sheet sheet2 = new Sheet.UpdateSheetBuilder().setName("TESTING").setId(921940333488004L).build();
		
		// Update the sheet with the new name
		smartsheet.sheets().updateSheet(sheet2);
		//=========================================
		
		
		// Setup a publishing status to give a rich version of the sheet as read only 
		SheetPublish publish = new SheetPublish.PublishStatusBuilder().setReadOnlyFullEnabled(true).
				setReadOnlyLiteEnabled(false).setIcalEnabled(false).setReadWriteEnabled(false).build();
		// Setup the specified sheet with the new publishing status
		smartsheet.sheets().updatePublishStatus(921940333488004L, publish);
		//=========================================
		
		// Setup a user with an email address and full permission
		User user3 = new User.AddUserBuilder().setEmail("newUser@batie.com").setAdmin(true).
				setLicensedSheetCreator(true).build();
		// Create the user account
		smartsheet.users().addUser(user3);
		//=========================================
		
		// Setup a user with new privileges
		User user4 = new User.UpdateUserBuilder().setAdmin(false).setLicensedSheetCreator(false).
				setId(2033602423744388L).build();
		// Send the request to update the users account with the new privileges 
		smartsheet.users().updateUser(user4);
		//=========================================
		
		// Create a workspace with a specific name and ID
		Workspace workspace = new Workspace.UpdateWorkspaceBuilder().setName("Workspace Name1").
				setId(8394116129154948L).build();
		// Update the workspace with the new name.
		smartsheet.workspaces().updateWorkspace(workspace);
		//=========================================
		
//		// Update sheet
//		sheet1.setName("Sheet1-Updated");
//		sheet1 = smartsheet.sheets().updateSheet(sheet1);
//
//		// Add column to sheet
//		Column column2 = new Column();
//		column2.setTitle("Column2");
//		column2.setPrimary(false);
//		column2.setType(ColumnType.PICKLIST);
//		column2 = smartsheet.sheets().columns().addColumn(sheet1.getId(), column2);
//
//		// Insert rows
//		RowWrapper rowWrapper = new RowWrapper();
//		rowWrapper.setToTop(true);
//		Row row1 = new Row();
//		Cell cell11 = new Cell();
//		cell11.setColumnId(column1.getId());
//		cell11.setValue("ABC");
//		cell11.setStrict(true);
//		row1.setCells(Arrays.asList(cell11));
//		rowWrapper.setRows(Arrays.asList(row1));
//		List<Row> rows = smartsheet.sheets().rows().insertRow(sheet1.getId(), rowWrapper);
//
//		// Add discussion
//		Discussion discussion1 = new Discussion();
//		discussion1.setTitle("title");
//		smartsheet.sheets().discussions().createDiscussion(sheet1.getId(), discussion1);
//
//		// Get sheet
//		Sheet sheet = smartsheet.sheets().getSheet(sheet1.getId(), EnumSet.of(ObjectInclusion.DISCUSSIONS));
//
//		// Search sheet
//		SearchResult searchResult = smartsheet.search().searchSheet(sheet1.getId(), "title");
//
//		// Share sheet
//		Share share = new Share();
//		share.setEmail("sss@example.com");
//		share.setAccessLevel(AccessLevel.VIEWER);
//		smartsheet.sheets().shares().shareTo(sheet1.getId(), share);
//
//		// Delete sheet
//		smartsheet.sheets().deleteSheet(sheet1.getId());
	}

}
