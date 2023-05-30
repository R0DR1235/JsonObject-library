import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals

class TestsCase {

    val student1 = Student(92970, "Rodrigo",true, StudentType.Master)
    val student2 = Student(93145, "João",true, StudentType.Bachelor)
    val inscritos = mutableListOf<Student>(student1,student2)
    val myMap = mutableMapOf<String, Int>("first" to 1, "second" to 2)
    val uc = Uc("MEI", "PA", 6.0, inscritos, myMap)
    val jsonObject = toJson(uc)

    @Test
    fun GetPropertiesByNameTest() {
        val validResult = mutableListOf<JsonValue>(JsonNumber(92970),JsonNumber(93145))

        val gpn = GetPropertiesByName("number")
        jsonObject.accept(gpn)

        assertEquals(validResult, gpn.results)
    }

    @Test
    fun GetObjectsWithPropertiesTest() {
        val n = JsonNumber(92970)
        val s = JsonString("Rodrigo")
        val e = JsonEnum("Master")
        val map1 = mutableMapOf<String,JsonValue>()
        map1["number"] = n
        map1["name"] = s
        map1["Level"] = e
        val student1 = JsonObject(map1)

        val n2 = JsonNumber(93145)
        val s2 = JsonString("João")
        val e2 = JsonEnum("Bachelor")
        val map2 = mutableMapOf<String,JsonValue>()
        map2["number"] = n2
        map2["name"] = s2
        map2["Level"] = e2
        val student2 = JsonObject(map2)

        val validResult = mutableListOf<JsonValue>(student1,student2)

        val nno = GetObjectsWithProperties(mutableListOf("name","number"))
        jsonObject.accept(nno)
        assertEquals(validResult, nno.results)
    }

    @Test
    fun AllNumberPropertiesAreIntegersValuesTest() {
        val validResult = true

        val apiv = AllPropertiesAreIntegersValues("number")
        jsonObject.accept(apiv)
        assertEquals(validResult, apiv.result)
    }


    @Test
    fun AllArrayElementsSameStuctureTest(){
        val validResult = true

        val aaess = AllElementsArraySameStucture("inscritos")
        jsonObject.accept(aaess)
        assertEquals(validResult, aaess.result)

    }




}
