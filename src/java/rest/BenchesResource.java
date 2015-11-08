package rest;

import com.google.gson.Gson;
import db.mysql.BenchesDAO;
import db.valueObjects.Bench;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import util.ResponseUtil;
import util.SessionUtil;

@Path("/catalog/benches")
public class BenchesResource {

    private static final Gson gson = new Gson();
    private static final BenchesDAO benchDAO;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    static {
        benchDAO = new BenchesDAO();
    }

    @GET
    @Produces("application/json")
    public String getBenches() {
        try {
            List<Bench> benches = benchDAO.getBenchList();
            return gson.toJson(benches);
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json")
    public String addBench(Bench bench) {
        if (SessionUtil.isAdmin(request)) {
            try {
                benchDAO.insert(bench);
                return ResponseUtil.SendSuccessMessage(response, "Bench successfully added.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.SendError(response, HttpServletResponse.SC_UNAUTHORIZED, "You have no permission to perform this action");
    }

    @DELETE
    @Path("/{id}")
    public String removeBench(@PathParam("id") String id) {
        if (SessionUtil.isAdmin(request)) {
            try {
                if (benchDAO.remove(id)) {
                    return ResponseUtil.SendSuccessMessage(response, "Bench (id = " + id + ")  successfully remove.");
                }

                return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Bench (id = " + id + ")  has an invalid id.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.SendError(response, HttpServletResponse.SC_UNAUTHORIZED, "You have no permission to perform this action");
    }
}
