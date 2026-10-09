/**
Alexander Baldree
Max Jankowski
Aftabur Rahman
Jordan Dardar

Green team - critique fixes 10-9-26 
Added by Max 
*/
package com.moffatbaymarina.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// correction to a critique we had. The LOGOUT links only cleared browser storage, so the server session stayed 'alive'. '/logout' now actually ends it, and the shared nav script
// (js/session.js :  created yesterday)  calls this. All good to call when nobody is logged in.
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", "Logged out.");
        JSON.writeValue(response.getWriter(), body);
    }
}
