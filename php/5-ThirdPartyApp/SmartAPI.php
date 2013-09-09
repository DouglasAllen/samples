<?php
    class SmartAPI {
        const BASE_API_URL = "https://api.smartsheet.com/1.1/";

        function getToken($authCode, $secret, $clientId, $redirectURI){
            $tokenURL = self::BASE_API_URL. "token";
            $hashedSecret = hash("sha256", $secret. "|" .$authCode);
            
            $headers = array(
                "Content-Type: application/x-www-form-urlencoded"
             );

            $postfields = array(
                "grant_type"=>"authorization_code",
                "code"=>$authCode,
                "client_id"=>$clientId,
                "hash"=>$hashedSecret,
                "redirect_uri"=>urlencode($redirectURI) 
            );

            $postString = "";

            foreach($postfields as $key=>$value){
                $postString .= $key. "=" .$value."&";
            }

            $postString = rtrim($postString, "&");

            // Connect to Smartsheet API to get Access Token
            $tokenCurl = curl_init($tokenURL);
            curl_setopt($tokenCurl, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($tokenCurl, CURLOPT_POST, 1);
            curl_setopt($tokenCurl, CURLOPT_POSTFIELDS, $postString);
            curl_setopt($tokenCurl, CURLOPT_RETURNTRANSFER, TRUE);

            $tokenResponse = curl_exec($tokenCurl);

            if (curl_errno($tokenCurl)) { 
                $apiResponse = "Oh No! Error: " . curl_error($tokenCurl); 
            } else { 
                // Assign response to variable 
                $apiResponse = json_decode($tokenResponse);

                curl_close($tokenCurl);
            }
            
            return $apiResponse;
        }

        function getUser($token){
            $userURL = self::BASE_API_URL ."user/me";
            
            $userCurl = curl_init($userURL);
            $headers = array(
                "Authorization: Bearer ". $token,
                "Content-Type: application/json"
            );

            // Connect to Smartsheet API to get current user info
            curl_setopt($userCurl, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($userCurl, CURLOPT_RETURNTRANSFER, TRUE);

            $userResponse = curl_exec($userCurl);

            if (curl_errno($userCurl)) { 
                $apiResponse = "Whoops! Error: " . curl_error($userCurl); 
            } else { 
                // Load UserObj
                $apiResponse = json_decode($userResponse);

                // close userCurl 
                curl_close($userCurl); 
            }

            return $apiResponse;
        }
    }
?>