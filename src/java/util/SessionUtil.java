package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
    
    public static boolean isAdmin(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("admin").equals("true");
    }
    
    public static boolean isUser(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        return session != null;
    }
    
    public static String getUserame(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        return session == null ? "" : (String)session.getAttribute("username");
    }
    
    public static void createSession(HttpServletRequest request, String username){
        createSessionHelper(request, username, false);
    }
    
    public static void createAdminSession(HttpServletRequest request, String username){
        createSessionHelper(request, username, true);
    }
    
    //setting session to not expire unless user logs out. if he's an admin then session is 2 hours
    private static void createSessionHelper(HttpServletRequest request, String username, boolean isAdmin){
        HttpSession session = request.getSession();
        session.setAttribute("username", username);
        int sessionExpirarationTime = isAdmin ? 60*120 : -1; 
        session.setMaxInactiveInterval(sessionExpirarationTime);
        session.setAttribute("admin", String.valueOf(isAdmin));
    }
    
    public static void invalidateSession(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session != null) {
            String name = (String)session.getAttribute("username");
            session.invalidate();
            System.out.println("logout user: " + name);
        }
    }
}
