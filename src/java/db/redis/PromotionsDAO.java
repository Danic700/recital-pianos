package db.redis;

import db.mysql.PianosDAO;
import db.valueObjects.Piano;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class PromotionsDAO {

    private static final String PRODUCT_PROMOTION_KEY = "product_promotion";

    protected final Jedis jedis;

    public PromotionsDAO() {
        this.jedis = RedisConn.getConnection();
    }

    public void init() {
        clear();
    }

    private void clear() {
        Set<String> pianosId = jedis.smembers(PRODUCT_PROMOTION_KEY);

        for (String pianoId : pianosId) {
            removePianoHash(pianoId);
        }

        jedis.del(PRODUCT_PROMOTION_KEY);
    }

    public List<Piano> getPianoPromotions() {
        List<Piano> pianos = new ArrayList<>();
        Set<String> pianosIds;
        Map<String, String> pianoHash;

        pianosIds = jedis.smembers(PRODUCT_PROMOTION_KEY);

        for (String pianoId : pianosIds) {
            pianoHash = jedis.hgetAll(getPianoHashKey(pianoId));

            if (!pianoHash.isEmpty()) {
                pianos.add(hashAsPiano(pianoHash));
            }
        }

        return pianos;
    }

    public void addPianoPromotion(Piano piano) {
        jedis.sadd(PRODUCT_PROMOTION_KEY, String.valueOf(piano.id));
        addPianoHash(piano);
    }

    public void removePianoPromotion(String pianoId) {
        jedis.srem(PRODUCT_PROMOTION_KEY, pianoId);
        removePianoHash(pianoId);
    }

    private void addPianoHash(Piano piano) {
        String hashKey = getPianoHashKey(piano.id);
        Map<String, String> pianoHash = pianoAsHash(piano);

        jedis.hmset(hashKey, pianoHash);
    }

    private void removePianoHash(String pianoId) {
        jedis.del(getPianoHashKey(pianoId));
    }

    private String getPianoHashKey(int pianoId) {
        return getPianoHashKey(String.valueOf(pianoId));
    }

    private String getPianoHashKey(String pianoId) {
        return PRODUCT_PROMOTION_KEY + ":" + pianoId;
    }

    private Map<String, String> pianoAsHash(Piano piano) {
        Map<String, String> pianoHash = new HashMap<>();

        pianoHash.put(PianosDAO.COL_PIANO_ID, String.valueOf(piano.id));
        pianoHash.put(PianosDAO.COL_SIZE, String.valueOf(piano.size));
        pianoHash.put(PianosDAO.COL_YEAR, String.valueOf(piano.year));
        pianoHash.put(PianosDAO.COL_IS_UPRIGHT, String.valueOf(piano.isUpright));
        pianoHash.put(PianosDAO.COL_IS_NEW, String.valueOf(piano.isNew));

        pianoHash.put(PianosDAO.COL_MODEL, piano.model);
        pianoHash.put(PianosDAO.COL_COLOR, piano.color);
        pianoHash.put(PianosDAO.COL_FINISH, piano.finish);
        pianoHash.put(PianosDAO.COL_IMG_URL, piano.img);

        pianoHash.put(PianosDAO.COL_MANUFACTURER, piano.manufacturer);
        pianoHash.put(PianosDAO.COL_COUNTRY, piano.country);

        return pianoHash;
    }

    private Piano hashAsPiano(Map<String, String> pianoHash) {
        Piano piano = new Piano();

        piano.id = Integer.valueOf(pianoHash.get(PianosDAO.COL_PIANO_ID));
        piano.size = Integer.valueOf(pianoHash.get(PianosDAO.COL_SIZE));
        piano.year = Integer.valueOf(pianoHash.get(PianosDAO.COL_YEAR));
        piano.isUpright = Boolean.valueOf(pianoHash.get(PianosDAO.COL_IS_UPRIGHT));
        piano.isNew = Boolean.valueOf(pianoHash.get(PianosDAO.COL_IS_NEW));

        piano.model = pianoHash.get(PianosDAO.COL_MODEL);
        piano.color = pianoHash.get(PianosDAO.COL_COLOR);
        piano.finish = pianoHash.get(PianosDAO.COL_FINISH);
        piano.img = pianoHash.get(PianosDAO.COL_IMG_URL);

        piano.manufacturer = pianoHash.get(PianosDAO.COL_MANUFACTURER);
        piano.country = pianoHash.get(PianosDAO.COL_COUNTRY);

        return piano;
    }
}
