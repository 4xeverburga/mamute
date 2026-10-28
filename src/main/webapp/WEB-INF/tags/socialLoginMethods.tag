<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ attribute name="buttonContent" required="true"%>

<c:set var="buttonContentText" value="${buttonContent}.button.content"/>

<%-- Facebook sign-up/login removed: the app credentials no longer work
     and the legacy OAuth flow is unmaintained. Google sign-in remains. --%>

<c:if test="${env.supports('feature.google.login')}">
	<a href="${googleUrl}" class="social-button-wraper">
		<c:set var="buttonContentLabel" value="${buttonContent}.google_button.label"/>
		<p>${t[buttonContentLabel]}</p>
		<span class="google-button">${t[buttonContentText]}</span>
	</a>
</c:if>
