<%
    response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; font-src 'self'; frame-ancestors 'self'");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error</title>
    <style>
        body, html {
            height: 100%;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: Arial, sans-serif;
            background-color: #f3f3f3;
        }
        .container {
            text-align: center;
        }
        h1 {
            font-size: 40px;
            font-weight: normal;
        }
        p {
            color: #757575;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Oops!</h1>
    <p>We're sorry, but something went wrong.</p>
</div>
</body>
</html>
