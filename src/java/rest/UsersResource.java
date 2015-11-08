package rest;

import db.mysql.AdminsDAO;
import util.SessionUtil;
import db.mysql.UsersDAO;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import util.ResponseUtil;

@Path("/users")
public class UsersResource {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    private static final UsersDAO usersDAO;
    private static final AdminsDAO adminsDAO;
    private static final String DEAFULT_ADMIN_MAIL = "admin@admin.com";
    private static final String DEAFULT_ADMIN_PASSWORD = "adminADMIN123";

    static {
        usersDAO = new UsersDAO();
        adminsDAO = new AdminsDAO();

        // Create deafult admin
        try {
            usersDAO.insert(DEAFULT_ADMIN_MAIL, DEAFULT_ADMIN_PASSWORD);
            adminsDAO.insert(DEAFULT_ADMIN_MAIL);
        } catch (SQLException ex) {
            System.out.println("Failed to create deafult admin!");
        }
    }

    @GET
    @Path("/user/{email}/{password}")
    public String loginUser(@PathParam("email") String username, @PathParam("password") String password) {
        try {
            if (usersDAO.isValid(username, password)) {
                if (adminsDAO.isAdmin(username)) {
                    SessionUtil.createAdminSession(request, username);
                    return ResponseUtil.SendSuccessMessage(response, "Successfuly logged in as admin");
                } else {
                    SessionUtil.createSession(request, username);
                    return ResponseUtil.SendSuccessMessage(response, "Successfuly logged in");
                }
            }
            return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email or old password don't match, couldn't login");
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @POST
    @Path("/user/{email}/{password}")
    public String addUser(@PathParam("email") String email, @PathParam("password") String password) {
        try {
            usersDAO.insert(email, password);
            return ResponseUtil.SendSuccessMessage(response, "User ( " + email + " ) successfully signup.");
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PUT
    @Path("/user/{email}/{password}/{newpassword}")
    public String updateUser(@PathParam("email") String username, @PathParam("password") String password, @PathParam("newpassword") String newPassword) {
        try {
            if (usersDAO.isValid(username, password)) {
                usersDAO.updatePassword(username, newPassword);
                return ResponseUtil.SendSuccessMessage(response, "Password was changed");
            }
            return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email or old password don't match, password wasn't changed");
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GET
    @Path("/login")
    public String isLoggedIn() {
        return ResponseUtil.SendSuccess(response, "\"Login\":\"" + (request.getSession(false) != null) + "\"");
    }

    @GET
    @Path("/logout")
    public String logoutUser() {
        SessionUtil.invalidateSession(request);
        return isLoggedIn();
    }

    // Admin API
    //for debug
    @GET
    @Path("/admin")
    public String isAdmin() {
        return ResponseUtil.SendSuccess(response, "\"isAdmin\":\"" + SessionUtil.isAdmin(request) + "\"");
    }

    @POST
    @Path("/admin/{email}")
    public String addAdmin(@PathParam("email") String email) {
        if (SessionUtil.isAdmin(request)) {
            try {
                if (adminsDAO.insert(email)) {
                    return ResponseUtil.SendSuccessMessage(response, "User (" + email + ") successfully set to admin.");
                }
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    @DELETE
    @Path("/admin/{email}")
    public String removeAdmin(@PathParam("email") String email) {
        if (SessionUtil.isAdmin(request)) {
            try {
                adminsDAO.remove(email);
                return ResponseUtil.SendSuccessMessage(response, "Admin (" + email + ") successfully removed.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }
    
    @DELETE
    @Path("/user/{email}")
    public String removeUser(@PathParam("email") String email) {
        if (SessionUtil.isAdmin(request)) {
            try {
                adminsDAO.remove(email);
                usersDAO.remove(email);
                return ResponseUtil.SendSuccessMessage(response, "User removed: " + email);
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }
}
