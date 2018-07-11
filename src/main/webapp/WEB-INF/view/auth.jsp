<%@page import="org.coody.framework.util.RequestUtil"%>
<%@page import="com.imxss.web.controller.xss.XssController"%>
<%@page import="org.coody.framework.util.SpringContextHelper"%>
<%@page import="org.coody.framework.util.StringUtil"%>
<%@page import="sun.misc.BASE64Decoder"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.PrintWriter"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String sessionAuth = (String) request.getSession().getAttribute("auth");

	if (sessionAuth == null) {
		String auth = request.getHeader("Authorization");
		if (!StringUtil.isNullOrEmpty(auth) && auth.length() > 6) {
			response.setStatus(401);
			response.setHeader("Cache-Control", "no-store");
			response.setDateHeader("Expires", 0);
			response.setHeader("WWW-authenticate", "Basic Realm=\"ImXSS\"");
			Map<String,String> paraMap=new HashMap<String,String>();
			paraMap.put("Authorization", auth);
			XssController xssController=SpringContextHelper.getBean(XssController.class);
			Integer projectId=(Integer)request.getAttribute("projectId");
			Integer moduleId=(Integer)request.getAttribute("moduleId");
			String referer=(String)request.getSession().getAttribute("referer");
			String ip=RequestUtil.getIpAddr(request);
			String basePath=(String)request.getAttribute("basePath");
			xssController.doApi(projectId, referer, paraMap, basePath, ip, moduleId);
			return;
		}
	}
	PrintWriter pw = response.getWriter();
	pw.println("<html> next step, authentication is : " + request.getSession().getAttribute("auth") + "<br>");
	pw.println("<br></html>");
%>
