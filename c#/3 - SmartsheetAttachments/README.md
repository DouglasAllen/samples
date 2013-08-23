Working with File Upload, Download and Deletion (C#)
===
See our <b>Hellosmartsheet</b> and <b>SheetStructure</b> scripts for a hands-on introduction to the Smartsheet API.  The third in the series, this non-interactive script walks you through the more advanced Smartsheet API calls and capabilities by attaching files and a URL to a row, downloading the file attachments, and deleting one of the file attachments from the row.

Smartsheet API
---
Familiarize yourself with the Smartsheet API. For information on the Smartsheet APi, please see the [Smartsheet Developer Portal](http://smartsheet.com/developers).

.NET Framework
---
This script was built on .NET Framework 4.5. To set the target framework of the project in VS Express 2012, click Project → Console Application Properties → Application, and make a selection from the Target Framework drop-down list. Make sure it is not set to use the Client Profile.

References and Using Directives
---

The body of requests made to Smartsheet API are expected to be in JSON, and the response body data is also returned as JSON. This program uses the built-in JavaScriptSerializer class to serialize and deserialize JSON. Third-party libraries are available for this, but the built-in option is rather straight forward and easy to work with.

In order to use the JavaScriptSerializer class, add a reference to the program. In VS Express 2012, click Project → Add Reference → Framework. Select System.Web.Extensions to use JavaScriptSerializer in the application.

The following namespaces are used in the program to save time when typing out methods:

System.IO - for the StreamReader and StreamWriter classes

System.Net - for the WebRequest class

System.Web.Script.Serialization - for the JavaScriptSerializer class

Usage
---
Generate a Smartsheet API access token and insert it into the Smartsheet Request class at the bottom of the script:

             string token = "insert token here";

You will also want to replace the example paths in the code with the paths to the actual files you'd like to upload:


             string[] paths = new string[] { @"C:\MyAttachment.jpg", @"C:\MyAttachment2.jpg" };

Code
---
This walkthrough highlights only some parts of the code.  For the full code, please see the complete <b>SmartsheetAttachments.cs</b> script.

The goal of this walkthrough to help you understand how to attach files and URLs to data containers in Smartsheet, and then access these attachments. In Smartsheet, users can attach files to workspaces (not supported via the API as of 2013 07 22), sheets, rows, and discussion comments. 

<b>IMPORTANT</b>: Please note that as of this writing the Smartsheet API does not support multipart or chunked file upload. 
	 
First, create a sheet with a couple of rows so that we have something to work with.  Now that the rows are in place, let's upload a file to the top row. A number of headers, including <code>Content-Length</code>, are required. Use the built-in FileInfo class to determine key factors about the file based on its path, such as its size (in kB).

            FileInfo file = new FileInfo(path);

Load the actual file, and set the request headers:

            byte[] data = File.ReadAllBytes(path);

            WebRequest postRequest = WebRequest.Create(baseURL + callURL);
            postRequest.ContentType = contentType;
            postRequest.Method = "POST";
            postRequest.ContentLength = file.Length;
            //Timeout property is set to infinite, to accomodate larger files
            postRequest.Timeout = System.Threading.Timeout.Infinite;
            postRequest.Headers.Add("Authorization: Bearer " + token);
            postRequest.Headers.Add("Content-Disposition: attachment; filename=\"" + file.Name + "\"");

Upload the file to the request stream:

            Stream stream = postRequest.GetRequestStream();
            stream.Write(data, 0, data.Length);
            stream.Close();


Attaching a URL is a simpler process. Simply include the info about the URL in the JSON body of your POST request.

            string urlInfo = "{\"name\":\"" + URLname + "\", \"description\":\"A great distraction from coding\", \"attachmentType\":\"LINK\", \"url\":\"http://pinterest.com/julilj/pictures-of-cupcakes\"}";
            SmartsheetRequest uploadURL = new SmartsheetRequest { callURL = "/row/" + urlRowId + "/attachments/", method = "POST", contentType = "" };
            uploadURL.MakeRequest(urlInfo);


You can attach a file/URL to a row, sheet, or discussion based on the end point used in the callURL. See the [Posting an Attachment](http://www.smartsheet.com/developers/api-documentation#h.qnd2uxrrygyz) section of the API documentation to review the different URLs.

To download the attached file, first fetch the attachment object which contains the URL to the downloadable file:

    		SmartsheetRequest getAttachment = new SmartsheetRequest { callURL = "/attachment/" + [REPLACE WITH ATTACHMENT ID], method = "GET" };
	
Extract the URL, size and name from the JSON response. Pass them to the DownloadAttachment method to create a new file based on those parameters:

           downloadAttachment.DownloadAttachment(jsonGetAttachment["url"], jsonGetAttachment["sizeInKb"], jsonGetAttachment["name"]);

  			public void DownloadAttachment(string attachURL, int size, string name)
        	{
	            string attachName = new FileInfo(name).Name;
	            WebRequest getAttachRequest = WebRequest.Create(attachURL);
	            getAttachRequest.Method = WebRequestMethods.File.DownloadFile;
	
	            //Open the Response stream containing the file, write it to a byte array
	            Stream responseStream = getAttachRequest.GetResponse().GetResponseStream();
	
	            byte[] data = new byte[size];
	
	            FileStream fs = new FileStream(attachName, FileMode.Create);
	            int readCount = responseStream.Read(data, 0, data.Length);
	            while (readCount > 0)
	            {
	                fs.Write(data, 0, readCount);
	                readCount = responseStream.Read(data, 0, data.Length);
	            }
	
	            fs.Close();
	            responseStream.Close();
        	}
	
Deleting an attachment is easy - just change the request method to DELETE (all caps), and pass in the AttachmentId that you want to delete.

           string idToDelete = fileAttachmentIds[0];
           SmartsheetRequest deleteAttachment = new SmartsheetRequest { callURL = "/attachment/" + idToDelete, method = "DELETE" };

	
Congratulations! You just completed your third Smartsheet API C# walkthrough. We encourage you to play with the script, change it around, and enhance it to get better acquainted with the Smartsheet API. Ping us at api@smartsheet.com with any questions or suggestions.

The Smartsheet Platform team. 

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8682c8fc5c6618bcdad0698d2832b639 "githalytics.com")](http://githalytics.com/smartsheet-platform/samples)
