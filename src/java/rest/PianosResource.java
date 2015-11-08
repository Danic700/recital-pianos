package rest;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import db.mysql.PianosDAO;
import db.redis.PromotionsDAO;
import db.valueObjects.Piano;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import util.ResponseUtil;
import util.SessionUtil;

@Path("/catalog/pianos")
public class PianosResource {

    private static final Gson gson;
    private static final PianosDAO pianosDAO;
    private static final PromotionsDAO promotionsDAO;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    static {
        gson = new Gson();
        pianosDAO = new PianosDAO();
        promotionsDAO = new PromotionsDAO();

        promotionsDAO.init();
    }

    /**
     * @return all pianos from MySQL database
     */
    @GET
    @Produces("application/json")
    public String getAllPianos() {
        try {
            List<Piano> allPianos = pianosDAO.getAllPianosList();
            return gson.toJson(allPianos);
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
    
    /**
     * @return all upright pianos from MySQL database
     */
    @GET
    @Path("/upright")
    @Produces("application/json")
    public String getUprightPianos() {
        try {
            List<Piano> uprightPianos = pianosDAO.getUprightPianosList();
            return gson.toJson(uprightPianos);
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * @return all grand pianos from MySQL database
     */
    @GET
    @Path("/grand")
    @Produces("application/json")
    public String getGrandPianos() {
        try {
            List<Piano> grandPianos = pianosDAO.getGrandPianosList();
            return gson.toJson(grandPianos);
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * only admin can add an item to MySQL database
     *
     * @param piano to add
     * @return SQL insertion feedback
     */
    @POST
    @Consumes("application/json")
    public String addPiano(Piano piano) {
        if (SessionUtil.isAdmin(request)) {
            try {
                pianosDAO.insert(piano);
                return ResponseUtil.SendSuccessMessage(response, "Piano successfully added.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    /**
     * only admin can remove an item from MySQL database
     *
     * @param id of the piano to be removed
     * @return SQL delete feedback
     */
    @DELETE
    @Path("/{id}")
    public String removePiano(@PathParam("id") String id) {
        if (SessionUtil.isAdmin(request)) {
            try {
                if (pianosDAO.remove(id)) {
                    return ResponseUtil.SendSuccessMessage(response, "Piano (id = " + id + ")  successfully remove.");
                } else {
                    return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Piano (id = " + id + ")  has an invalid id.");
                }
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    /**
     * @return promotion pianos, both upright and grand, from Redis database
     */
    @GET
    @Path("/promotions")
    @Produces("application/json")
    public String getPromotions() {
        List<Piano> promotionPianos = promotionsDAO.getPianoPromotions();
        return gson.toJson(promotionPianos);
    }

    @POST
    @Path("/promotions/{id}")
    @Produces("application/json")
    public String addPromotion(@PathParam("id") String id) {
        if (SessionUtil.isAdmin(request)) {
            try {
                List<Piano> pianoList = pianosDAO.getPianoById(Integer.valueOf(id));
                if (!pianoList.isEmpty()) {
                    promotionsDAO.addPianoPromotion(pianoList.get(0));
                    return ResponseUtil.SendSuccessMessage(response, "Piano (id = " + id + ")  successfully added to promotion list.");
                } else {
                    return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Piano (id = " + id + ")  has an invalid id.");
                }
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    @DELETE
    @Path("/promotions/{id}")
    @Produces("application/json")
    public String removePromotion(@PathParam("id") String id) {
        if (SessionUtil.isAdmin(request)) {
            try {
                List<Piano> pianoList = pianosDAO.getPianoById(Integer.valueOf(id));
                if (!pianoList.isEmpty()) {
                    promotionsDAO.removePianoPromotion(id);
                    return ResponseUtil.SendSuccessMessage(response, "Piano (id = " + id + ")  successfully removed from promotion list.");
                } else {
                    ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Piano (id = " + id + ")  has an invalid id.");
                }
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }
}
