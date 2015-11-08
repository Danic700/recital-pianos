package rest;

import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import db.valueObjects.Post;
import db.mysql.PostsDAO;
import db.redis.RecentPostsDAO;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import util.ResponseUtil;
import util.SessionUtil;

@Path("/posts")
public class PostsResource {

    private static final Gson gson;
    private static final PostsDAO postDAO;
    private static final RecentPostsDAO recentPostDAO;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    static {
        gson = new Gson();
        postDAO = new PostsDAO();
        recentPostDAO = new RecentPostsDAO();

        List<Post> posts;
        try {
            posts = postDAO.getPosts(RecentPostsDAO.RECENT_POSTS_MAX_LENGTH, 0);
        } catch (SQLException ex) {
            posts = new ArrayList<>();
        }
        recentPostDAO.init(posts);
    }

    @GET
    @Produces("application/json")
    public String getPosts() {
        try {
            List<Post> posts = postDAO.getAllPosts();
            return gson.toJson(posts);
        } catch (SQLException ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GET
    @Produces("application/json")
    @Path("/my")
    public String getPostsByUsername(@PathParam("username") String username) {
        if (SessionUtil.isUser(request)) {
            try {
                List<Post> posts;
                posts = postDAO.getPostByUsername(SessionUtil.getUserame(request));
                return gson.toJson(posts);
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    @GET
    @Path("/recent")
    @Produces("application/json")
    public String getRecentPosts() {
        try {
            List<Post> posts = recentPostDAO.getRecentPosts();
            return gson.toJson(posts);
        } catch (Exception ex) {
            return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @POST
    @Consumes("application/json")
    public String addPost(Post post) {
        if (SessionUtil.isUser(request)) {
            try {
                post.username = SessionUtil.getUserame(request);
                post = postDAO.insert(post);
                recentPostDAO.pushPost(post);
                return ResponseUtil.SendSuccessMessage(response, "Post successfully added.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    @DELETE
    @Path("/{id}")
    public String removePost(@PathParam("id") String id) {
        if (SessionUtil.isUser(request)) {
            try {
                if (SessionUtil.isAdmin(request)) {
                    if (postDAO.removePostByID(id)) {
                        onRemovePost(id);
                    } else {
                        return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to remove post, Invalid id (" + id + ").");
                    }
                } else {
                    if (postDAO.removePostByID(id, SessionUtil.getUserame(request))) {
                        onRemovePost(id);
                    } else {
                        return ResponseUtil.SendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to remove post, Invalid id (" + id + ") or username.");
                    }
                }

                return ResponseUtil.SendSuccessMessage(response, "Post (id = " + id + ")  successfully removed.");
            } catch (SQLException ex) {
                return ResponseUtil.SendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        return ResponseUtil.sendAdminPermissionError(response);
    }

    private void onRemovePost(String id) throws SQLException {
        recentPostDAO.removePost(id);
        List<Post> posts = postDAO.getPosts(1, RecentPostsDAO.RECENT_POSTS_MAX_LENGTH - 1);
        if (!posts.isEmpty()) {
            recentPostDAO.pushPost(posts.get(0));
        }
    }
}
