package com.trivadis.mze.elytrondemo.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trivadis.mze.elytrondemo.ejb.remote.SecuredEJBRemote;

@WebServlet(name = "AuthenticateServlet", urlPatterns = {"/authenticateServlet"})
public class AuthenticateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static String PAGE_HEADER = "<html><head><title>elytron-custom-security</title></head><body>";

    private static String PAGE_FOOTER = "</body></html>";

    @EJB
    private SecuredEJBRemote securedEJB;
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
        	if (!request.authenticate(response)) {
        		return;
        	}
	        Principal principal = request.getUserPrincipal();
	        String securityInformation = null;
	        String authType = null;
	        String remoteUser = null;
	
	        writer.println(PAGE_HEADER);
	
	        if (principal == null) {
	        	writer.println("<p>" + "No user principal!? " + "</p>");
	        } else {
	        	writer.println("<p>" + "Authenticate Successful: " + principal.getClass().getName() + ": " + principal + "</p>");
	        }
	        

	        try {
	        // Get security principal
	        securityInformation = securedEJB.getSecurityInformation();
	        writer.println("<h1>" + "Successfully called EJB " + "</h1>");
	        } catch (Exception e) {
		        writer.println("<h1>" + "Error calling EJB: " + e.getMessage() + "</h1>");
			}

	        // Get user name from login principal
	        remoteUser = request.getRemoteUser();
	        // Get authentication type
	        authType = request.getAuthType();
	
	        boolean hasAdminPermission = false;
	        try {
	            hasAdminPermission = securedEJB.administrativeMethod();
	        } catch (Exception e) {
	        }
	
	        writer.println("<p>" + "EJB Principal  : " + securityInformation + "</p>");
	        writer.println("<p>" + "Remote User : " + remoteUser + "</p>");
	        writer.println("<p>" + "Has admin permission : " + hasAdminPermission + "</p>");
	        writer.println("<p>" + "Authentication Type : " + authType + "</p>");
	
	        writer.println(PAGE_FOOTER);
        }
        
    }
}
