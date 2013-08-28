Working with File Upload, Download and Deletion (Java)
===
See our <b>Hellosmartsheet</b> and <b>SheetStructure</b> scripts for a hands-on introduction to the Smartsheet API.  The third in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by attaching files and a URL to a row, downloading the file attachments, and deleting one of the file attachments from the row.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet API, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).


Usage
---
Generate a Smartsheet API access token and insert it into main method at the top:

             String accessToken = "insert token here";

You will also want to replace the example paths in the code with the path to the actual files you'd like to upload:

             String pathToFile = "/SampleFile.pdf"; //Provide a path to a file here.

And the path for the downloaded file:

             FileOutputStream fos = new FileOutputStream("SampeFile-Dowloaded.pdf"); //Provide a path for the output file here

Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>AttachmentsExample.java</b> script.

The goal of this walkthrough to help you understand how to attach files and URLs to data containers in Smartsheet, and then access these attachments. In Smartsheet, users can attach files to workspaces (not supported via the API as of 2013 07 22), sheets, rows, and discussion comments. 

<b>IMPORTANT</b>: Please note that as of this writing the Smartsheet API does not support multipart or chunked file upload. 
	 
First, create a sheet with a couple of rows so that we have something to work with.  Now that the rows are in place, let's upload a file to the top row. A number of headers, including <code>Content-Length</code>, are required. Use the built-in FileInputStream class to determine key factors about the file based on its path, such as its size (in kB).

            File file = new File(pathToFile);
            FileInputStream fis = new FileInputStream(file);
            long fileSize = fis.getChannel().size();FileInfo file = new FileInfo(path);

Load the actual file, and set the request headers:

            connection = (HttpURLConnection) new URL(ROW_ATTACHMENT_URL.replace(ID, ""+newRowsResult.getResult().get(0).getId())).openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + accessToken);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Content-Length", "" + fileSize);
            connection.addRequestProperty("Content-Disposition", "attachment; filename=\"" + file.getName() +"\"");
            connection.setDoOutput(true);

Move the bits - Note that the Smartsheet API does not support multipart/chunked uploads

            copy(fis, connection.getOutputStream());
            Result<Attachment> attachmentResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Attachment>>() {});
            

To download the attached file, first fetch the attachment object which contains the URL to the downloadable file:
            
            connection = (HttpURLConnection) new URL(ATTACHMENT_URL.replace(ID, ""+attachmentResult.getResult().getId())).openConnection();
            connection.addRequestProperty("Authorization", "Bearer " + accessToken);
            Attachment attachment = mapper.readValue(connection.getInputStream(), Attachment.class); 
            
Now, using the short-lived url, download the file to local file system:

            //Download the file again.
            FileOutputStream fos = new FileOutputStream("SampeFile-Dowloaded.pdf"); //Provide a path for the output file here
            connection = (HttpURLConnection) new URL(attachment.getUrl()).openConnection();
            copy(connection.getInputStream(), fos);
            fos.close();

Attaching a URL is a simpler process. Simply include the info about the URL in the JSON body of your POST request. 

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


You can attach a file/URL to a row, sheet, or discussion based on the end point used in the callURL. See the [Posting an Attachment](http://www.smartsheet.com/developers/api-documentation#h.qnd2uxrrygyz) section of the API documentation to review the different URLs.


Deleting an attachment is easy - just change the request method to DELETE (all caps), and pass in the AttachmentId that you want to delete.

           connection = (HttpURLConnection) new URL(ATTACHMENT_URL.replace(ID, ""+urlAttachmentResult.getResult().getId())).openConnection();
           connection.addRequestProperty("Authorization", "Bearer " + accessToken);
           connection.setRequestMethod("DELETE");
           Result<Object> deleteResult = mapper.readValue(connection.getInputStream(), new TypeReference<Result<Object>>(){});

	
Congratulations! You just completed your third Smartsheet API Java walkthrough. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
