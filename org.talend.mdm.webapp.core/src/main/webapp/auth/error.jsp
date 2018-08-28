<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page isErrorPage="true"%>
<%@page import="java.util.Locale"%>
<%@page import="com.amalto.core.util.LocaleUtil"%>
<%@page import="com.amalto.core.util.Messages"%>
<%@page import="com.amalto.core.util.MessagesFactory"%>
<%
    String contextPath = request.getContextPath();
    Locale locale = LocaleUtil.getLocale(request);
    Messages MESSAGES = MessagesFactory.getMessages("org.talend.mdm.webapp.general.server.servlet.messages",MessagesFactory.class.getClassLoader());
    String errorTitle = MESSAGES.getMessage(locale, "error.title");
    String errorMessage = MESSAGES.getMessage(locale, request.getParameter("message"));
%>
<html>
<head>
    <title>Talend MDM</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <link rel="shortcut icon" href="<%=contextPath%>/auth/favicon.ico" />
    <link rel='stylesheet' type='text/css' href='<%= contextPath %>/auth/googleapi/fonts.css'/>
    <link rel="stylesheet" type="text/css" href="<%= contextPath %>/auth/loginPage.css"/>
<head>
<body style='text-align: center;'>

    <table width="100%" class="header1" border="0">
        <tr>
            <td height="128" width="50%" class="logo"><img src="<%=contextPath%>/auth/logo.png" height="60px"></td>
        </tr>
        <tr>
            <td class="suiteName" id="suiteName" colspan="2" height="56">Talend MDM</td>
        </tr>
    </table>

    <h3><%=errorTitle%></h3>
    <p><font size='4' color='red'><%=errorMessage%></font></p>
</body>
</html>