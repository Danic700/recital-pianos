package db.redis;

import db.mysql.PostsDAO;
import db.valueObjects.Post;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class RecentPostsDAO {

    public static final int RECENT_POSTS_MAX_LENGTH = 3;
    public static final int RECENT_POSTS_LAST_ELEMENT = -1;
    private static final String RECENT_POSTS_KEY = "recent_posts";
    private static final String RECENT_POSTS_LENGTH_KEY = "recent_posts_length";

    private final Jedis jedis;

    public RecentPostsDAO() {
        this.jedis = RedisConn.getConnection();
    }

    public void init(List<Post> posts) {
        clear();
        pushPosts(posts);
    }

    public List<Post> getRecentPosts() {
        List<Post> recentPosts = new ArrayList<>();
        Set<String> recentPostsIds;
        Map<String, String> postHash;

        recentPostsIds = jedis.zrevrange(RECENT_POSTS_KEY, 0, RECENT_POSTS_LAST_ELEMENT);

        for (String postId : recentPostsIds) {
            postHash = jedis.hgetAll(getPostHashKey(postId));

            if (!postHash.isEmpty()) {
                recentPosts.add(hashAsPost(postHash));
            }
        }

        return recentPosts;
    }

    private void clear() {
        Set<String> recentPostsIds = jedis.zrange(RECENT_POSTS_KEY, 0, RECENT_POSTS_LAST_ELEMENT);

        for (String postId : recentPostsIds) {
            removePostHash(postId);
        }

        jedis.del(RECENT_POSTS_KEY);
        jedis.del(RECENT_POSTS_LENGTH_KEY);
    }

    public void pushPosts(List<Post> posts) {
        if (RECENT_POSTS_MAX_LENGTH < posts.size()) {
            posts = posts.subList(0, RECENT_POSTS_MAX_LENGTH);
        }

        for (Post post : posts) {
            pushPost(post);
        }
    }

    public void pushPost(Post post) {
        Set<String> oldPost = null;

        // Handle Posts List
        if (RECENT_POSTS_MAX_LENGTH < jedis.incr(RECENT_POSTS_LENGTH_KEY)) {
            jedis.decr(RECENT_POSTS_LENGTH_KEY);
            oldPost = jedis.zrange(RECENT_POSTS_KEY, 0, 0);
            jedis.zremrangeByRank(RECENT_POSTS_KEY, 0, 0);
        }
        jedis.zadd(RECENT_POSTS_KEY, post.date.getTime(), String.valueOf(post.id));

        // Handle Posts Hashes
        addPostHash(post);
        if (oldPost != null && !oldPost.isEmpty()) {
            removePostHash(oldPost.iterator().next());
        }
    }

    public void removePost(String postId) {
        jedis.decr(RECENT_POSTS_LENGTH_KEY);
        long removedElements = jedis.zrem(RECENT_POSTS_KEY, postId);

        if (removedElements != 0) {
            removePostHash(postId);
        }
    }

    private void addPostHash(Post post) {
        String hashKey = getPostHashKey(post.id);
        Map<String, String> postHash = postAsHash(post);

        jedis.hmset(hashKey, postHash);
    }

    private void removePostHash(String postId) {
        jedis.del(getPostHashKey(postId));
    }

    private String getPostHashKey(int postId) {
        return getPostHashKey(String.valueOf(postId));
    }

    private String getPostHashKey(String postId) {
        return RECENT_POSTS_KEY + ":" + postId;
    }

    private Map<String, String> postAsHash(Post post) {
        Map<String, String> postHash = new HashMap<>();

        postHash.put(PostsDAO.COL_ID, String.valueOf(post.id));
        postHash.put(PostsDAO.COL_DATE, String.valueOf(post.date));
        postHash.put(PostsDAO.COL_NAME, post.name);
        postHash.put(PostsDAO.COL_IMG_URL, post.img);
        postHash.put(PostsDAO.COL_TEXT, post.text);
        postHash.put(PostsDAO.COL_USERNAME, post.username);

        if (post.email != null) {
            postHash.put(PostsDAO.COL_EMAIL, post.email);
        }
        if (post.phone != null) {
            postHash.put(PostsDAO.COL_PHONE, post.phone);
        }

        return postHash;
    }

    private Post hashAsPost(Map<String, String> postHash) {
        Post post = new Post();

        post.id = Integer.valueOf(postHash.get(PostsDAO.COL_ID));
        post.date = Timestamp.valueOf(postHash.get(PostsDAO.COL_DATE));
        post.name = postHash.get(PostsDAO.COL_NAME);
        post.img = postHash.get(PostsDAO.COL_IMG_URL);
        post.text = postHash.get(PostsDAO.COL_TEXT);
        post.username = postHash.get(PostsDAO.COL_USERNAME);
        post.email = postHash.get(PostsDAO.COL_EMAIL);
        post.phone = postHash.get(PostsDAO.COL_PHONE);

        return post;
    }
}
