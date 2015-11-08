package util;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class ResponseUtil {

    public static String SendError(HttpServletResponse response, int statusCode, String message) {
        response.setStatus(statusCode);
        try {
            response.flushBuffer();
        } catch (IOException ex) {
            System.out.println("failed to send message to client");
        }
        return message;
    }
    
    public static String SendSuccess(HttpServletResponse response, String message) {
        return "{" + message + "}";
    }

    public static String sendAdminPermissionError(HttpServletResponse response) {
        return SendError(response, HttpServletResponse.SC_UNAUTHORIZED, "You have no permission to perform this action");
    }

    public static String SendSuccessMessage(HttpServletResponse response, String string) {
        return SendSuccess(response, "\"message\":\"" + string + "\"");
    }
}
