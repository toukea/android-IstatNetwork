package istat.android.network;


import org.junit.Test;

import istat.android.base.utils.AbsListWrapper;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        MyList list = new MyList();
        String toJson = list.toJson();
        System.out.println(toJson);
    }

    @Test
    public void matches() throws Exception {
        String pattern = ".*";
        String text = "{\"Pharmacies\":{";//"{\n\t\"Pharmacies\":{\n\t";
        boolean matches = text.matches(pattern);
        assertTrue(matches);
    }

    /*
    {
				"id_pharmacie":"15",
				"nom_pharmacie":"PHARMACIE SAINT HERMANN",
				"adresse":"Dr AGBASSI N'SOUA HIPPOL \nBASE CIE - ENTREE FACE STATION PETRO IVOIRE",
				"idville":"1",
				"idcommune":"1",
				"ville":"ABIDJAN",
				"commune":"YOPOUGON",
				"bons":"",
				"latitude":"5.335111",
				"longitude":"-4.090890",
				"contacts":"23 50 72 77 - 03 38 74 91",
				"status":"0",
				"distance":"-1"
			}
     */
    @Test
    public void TestFinder() throws Exception {
        TextFinder finder = new TextFinder("\\{.*\\}");
    }

    class MyList extends AbsListWrapper<String> {
        public MyList() {
            super();
            add("Hello");
            add("world");
            add("dady");
            add("polo");
        }

        public String toJson() {
            return null;
        }
    }
}