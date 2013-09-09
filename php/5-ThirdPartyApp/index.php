<?php 
    session_start(); 
    // initialize the session
    session_unset();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Smartsheet Third Party App Sample - PHP</title>

    <link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>

<?php
    // Use a value that matches your development configuration.
    $localDomain = "";

    // Set these values to match your Third Party App configuration in Smartsheet
    $_SESSION['clientId'] = "";
    $_SESSION['appSecret'] = "";
    $_SESSION['redirectURI'] = $localDomain ."";

    $smartSheetURI = "https://www.smartsheet.com/b/authorize?response_type=code";
    $smartSheetURI .= "&client_id=". $_SESSION['clientId'] ;
    $smartSheetURI .= "&redirect_uri=". $_SESSION['redirectURI'];
    $smartSheetURI .= "&scope=READ_SHEETS,WRITE_SHEETS";
?>

<div id="container">
    <h1>PHP OAuth2 Flow Example</h1>

    <div id="body">
        <p>
            <img src="http://www.smartsheet.com/files/haymaker/smartsheetLogo-1.png" alt="Smartsheet Logo"/>
        </p>
        
        <p>
            <a href="<?php echo $smartSheetURI ?>">Login to Smartsheet</a>
        </p>
    </div>
</div>

</body>
</html>