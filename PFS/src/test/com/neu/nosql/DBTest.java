package test.com.neu.nosql;

import com.neu.nosql.BitMap;
import com.neu.nosql.DB;
import com.neu.nosql.Utils;
import org.junit.Test;

import java.util.ArrayList;

import static com.neu.nosql.DB.BLOCK_CNT;

public class DBTest {

    /**
     * Tests the open() method of the DB class.
     * It opens a database with the name "test" and prints the resulting DB object.
     */
    @Test
    public void testOpen() throws Exception {
        DB db = DB.open("test");
        System.out.println(db);
    }

    /**
     * Tests the serializeBitmap() and deserializeBitmap() methods of the BitMap class.
     * It creates a bitmap array, sets the second element to true, serializes the bitmap,
     * and then deserializes the serialized bytes back into a bitmap array.
     * The original and deserialized bitmaps are printed.
     */
    @Test
    public void testSerializeBitmap() {
        boolean[] bitmap = new boolean[BLOCK_CNT];
        bitmap[1] = true;

        byte[] bytes = BitMap.serializeBitmap(bitmap);
        System.out.println(bytes);

        boolean[] deserializedBitmap = BitMap.deserializeBitmap(bytes);
        System.out.println(deserializedBitmap);
    }
    
    /**
     * Tests the validatePut() method of the Utils class.
     * It checks if the file "movies.csv" is valid for putting into the database.
     * The result of the validation is printed.
     */
    @Test
    public void testValidatePut() {
        boolean ans = Utils.validatePut("movies.csv");
        System.out.println(ans);
    }

    /**
     * Tests the selectDBFile() method of the DB class.
     * It selects a database file for the database "test" and the file "movies.csv".
     * The selected DB object is printed.
     */
    @Test
    public void testSelectDBFile() throws Exception {
        DB db = DB.selectDBFile("test", "movies.csv");
        System.out.println(db);
    }

    /**
     * Tests the put() method of the DB class.
     * It selects a database file for the database "test" and the file "movies.csv",
     * and then puts the file into the selected database.
     */
    @Test
    public void testPut() throws Exception {
        DB db = DB.selectDBFile("test", "movies.csv");
        db.put("movies.csv");
    }

    /**
     * Tests the locateDB() method of the DB class.
     * It locates the database file for the database "test" and the file "movies.csv".
     * The located DB object is printed.
     */
    @Test
    public void testLocateDB() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        System.out.println(db);
    }

    /**
     * Tests the find() method of the DB class.
     * It locates the database file for the database "test" and the file "movies.csv",
     * and then finds the value associated with the key 7 in the file.
     * The found value is printed.
     */
    @Test
    public void testFind() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        String ans = db.find("movies.csv", 7);
        System.out.println(ans);
    }

    /**
     * Tests the get() method of the DB class.
     * It locates the database file for the database "test" and the file "movies.csv",
     * and then retrieves the file from the database.
     */
    @Test
    public void testGet() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        db.get("movies.csv");
    }

    /**
     * Tests the dir() method of the DB class.
     * It locates the database file for the database "test" and the file "movies.csv",
     * and then retrieves the list of files in the database directory.
     * The list of files is printed.
     */
    @Test
    public void testDir() throws Exception {
        DB db = DB.locateDB("test", "movies.csv");
        ArrayList<String> ans = db.dir();
        System.out.println(ans);
    }
}
