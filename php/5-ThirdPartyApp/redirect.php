<?php
    session_start();
    include 'SmartAPI.php';

    if(isset($_REQUEST['code'])){
        $authCode = $_REQUEST['code'];
        $theSmartAPI = new SmartAPI();
        $tokenObj = $theSmartAPI->getToken($authCode, $_SESSION['appSecret'], $_SESSION['clientId'], $_SESSION['redirectURI']);

        $_SESSION['accessToken'] = $tokenObj->access_token;
        $_SESSION['refreshToken'] = $tokenObj->refresh_token;

        $userObj = $theSmartAPI->getUser($_SESSION['accessToken']);
    }
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Smartsheet Third Party App Sample - PHP</title>

    <link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>
<div id="container">
    <h1>PHP OAuth2 Flow Example</h1>

    <div id="body">
        <p>
            <img src="http://www.smartsheet.com/files/haymaker/smartsheetLogo-1.png" alt="Smartsheet Logo"/>
        </p>
        <?php
            if(isset($_REQUEST['code'])){
         ?>       
            <p>
                Hello, <?php echo $userObj->firstName ?>! You have authenticated and logged into a Smartsheet account using OAuth2.
            </p>
            <p>
                You are logged in as <?php echo $userObj->firstName . " " .$userObj->lastName ?>. With an email address
                of <?php echo $userObj->email ?>
            </p>
            <p>
                <a href="index.php">Logout</a>
            </p>
        <?php
            } elseif(isset($_REQUEST['error'])) {
         ?>
            <p class="error">
                <?php echo $_REQUEST['error'] ?>
            </p>
            <p>
                <a href="index.php">Start Over</a>
            </p>
        <?php    
            }
        ?>
    </div>
</div>

</body>
</html>
