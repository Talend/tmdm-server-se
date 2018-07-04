<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.Locale"%>
<%@page import="com.amalto.core.util.Util"%>
<%@page import="com.amalto.core.util.Version"%>
<%@page import="com.amalto.webapp.core.util.Webapp"%>
<%
    String contextPath = request.getContextPath();
    Locale locale = request.getLocale();
    String language=locale.getLanguage();
    
    String _USERNAME_;
    String _PASSWORD_;
    String _LOGIN_;
    String _ERROR_;
    
    if ("fr".equals(language)) {
         _USERNAME_ = "Identifiant";
         _PASSWORD_ = "Mot&nbsp;de&nbsp;passe";
         _LOGIN_= "Connexion";
         _ERROR_ = "Mauvais identifiant ou mot de passe";
    } else if ("ru".equals(language)) {
         _USERNAME_ = "Имя&nbsp;пользователя";
         _PASSWORD_ = "Пароль";
         _LOGIN_= "Войти";
         _ERROR_ = "Неверное имя пользователя или пароль";       
    } else if ("sk".equals(language)) {
         _USERNAME_ = "Prihlásenie";
         _PASSWORD_ = "Heslo";
         _LOGIN_= "Prihlasovacie meno";
         _ERROR_ = "Prihlásenie zlyhalo. Prosím, skontrolujte si Vaše meno a heslo";
    } else { 
         language="en"; //default language
         _USERNAME_ = "Login";
         _PASSWORD_ = "Password";
         _LOGIN_ = "Login";
         _ERROR_ = "Login failed. Please check your login and password";
    }
    
    String editionTitle=Webapp.INSTANCE.getProductInfo();
    String version=Version.getSimpleVersionAsString(this.getClass());
%>
<html>
<head>
<title>Talend MDM</title>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<meta name="description" content="Talend MDM login page"/>
<link rel="shortcut icon" href="<%=contextPath%>/auth/favicon.ico" />
<link rel='stylesheet' type='text/css' href='<%= contextPath %>/auth/googleapi/fonts.css'/>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/auth/loginPage.css"/>

</head>
<body onload="document.loginform.j_username.focus();">
<table width="100%" class="header1" border="0">
      <tr><td height="128" width="50%" class="logo"><img src="<%= contextPath %>/auth/logo.png"></td><td class="version" id="loginVersion"><%=version%></td></tr>
      <tr><td class="suiteName" id="suiteName" colspan="2" height="56">Talend MDM</td></tr>
    </table>

    <table width="100%"  class="header2" border="0">
      <tr><td class="appName" id="appName">
      <%=editionTitle%>
      </td></tr>
    </table>
    <table width="100%" height="65%" cellpadding="0" cellspacing="1">
        <tr>
            <td valign="middle" align="center">
                <form method="POST" action="<%= contextPath %>/auth/j_security_check" name="loginform" autocomplete="off" id="loginform">
                    <br>
                    <table width="300" height="150" cellpadding="0" cellspacing="0"
                        class="form">
                        <tr>
                            <td colspan="2">
                                <c:if test="${'fail' eq param.auth}">
                                    <div style="text-align:center;color:red;font-size:13px;"><%= _ERROR_ %></div>
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td align="right" width="120" style="padding-right:10px;"><%= _USERNAME_ %></td>
                            <td align="left"><input type="text" name="j_username" value="" onKeyDown="if(event.keyCode==13){document.all.login.click()}"/></td>
                        </tr>
                        <tr>
                            <td align="right" width="120" style="padding-right:10px;"><%= _PASSWORD_ %></td>
                            <td align="left"><input type="password" name="j_password" value="" onKeyDown="if(event.keyCode==13){document.all.login.click()}"/>
                            </td>
                        </tr>
                        <tr>
                            <td/>
                            <td align="left"><input type="button" name="login" value="<%=_LOGIN_%>" onclick="document.getElementById('loginform').submit();"/></td>
                        </tr>
                    </table>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                </form>
            </td>
        </tr>
    </table>
</body>
</html>
