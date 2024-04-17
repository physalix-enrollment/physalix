<%
    response.setStatus(301);
    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomain");
    response.setHeader("Location", "/UserGui/");
    response.setHeader("Connection", "close");
%>