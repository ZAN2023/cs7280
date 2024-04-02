package test.com.neu.nosql;

import com.neu.nosql.BitMap;
import com.neu.nosql.DB;
import com.neu.nosql.Utils;
import org.junit.Test;

import java.util.ArrayList;

import static com.neu.nosql.DB.BLOCK_CNT;

public class DBTest {

    @Test
    public void testOpen() throws Exception {
        DB db = DB.open("test");
        System.out.println(db);
    }

    @Test
    public void testSerializeBitmap() {
        boolean[] bitmap = new boolean[BLOCK_CNT];
        bitmap[1] = true;

        byte[] bytes = BitMap.serializeBitmap(bitmap);
        System.out.println(bytes);

        boolean[] deserializedBitmap = BitMap.deserializeBitmap(bytes);
        System.out.println(deserializedBitmap);
    }

    @Test
    public void testValidatePut() {
        boolean ans = Utils.validatePut("movies.csv");
        System.out.println(ans);
    }

    @Test
    public void testSelectDBFile() throws Exception {
        DB db = DB.selectDBFile("test", "movies.csv");
        System.out.println(db);
    }

    @Test
    public void testPut() throws Exception {
        DB db = DB.selectDBFile("test", "movies.csv");
        db.put("movies.csv");
    }

    @Test
    public void testLocateDB() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        System.out.println(db);
    }

    @Test
    public void testFind() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        String ans = db.find("movies.csv", 7);
        System.out.println(ans);
    }

    @Test
    public void testGet() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        db.get("movies.csv");
    }

    @Test
    public void testDir() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        ArrayList<String> ans = db.dir();
        System.out.println(ans);
    }
}
