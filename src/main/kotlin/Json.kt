import kotlin.reflect.KProperty1
import kotlin.reflect.full.*


//---------------------------------------------------------------------------------------------------------------------
//---------------------------------------- Classe Abstrata ------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------


abstract class JsonValue {
    abstract fun accept(visitor: JsonVisitor)
}


//---------------------------------------------------------------------------------------------------------------------
//---------------------------------- Classes de representação de objetos Json -----------------------------------------
//---------------------------------------------------------------------------------------------------------------------


data class JsonString(val value: String?) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitString(this)
    }

    override fun toString(): String {
        return if (value == null) "null" else "$value"
    }
}

data class JsonNumber(val value: Number?) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitNumber(this)
    }

    override fun toString(): String {
        return if (value == null) "null" else "$value"
    }
}

data class JsonBoolean(val value: Boolean?) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitBoolean(this)
    }

    override fun toString(): String {
        return if (value == null) "null" else "$value"
    }
}

data class JsonEnum(val value: String?) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitEnum(this)
    }

    override fun toString(): String {
        return if (value == null) "null" else "$value"
    }
}


data class JsonArray(val items: MutableList<JsonValue>) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitArray(this)
        items.forEach {
            it?.accept(visitor)
        }
        visitor.endvisitArray(this)
    }

    override fun toString(): String {
        val propertiesString = items.joinToString(", \n", "[ \n", "\n]") { value ->
            " ${value ?: "null"}"
        }
        return propertiesString
    }

    fun getItem(value:JsonValue):JsonValue?{
        items.forEach{it->
            if(it==value){return it}
        }
        return null
    }
    fun getIndex(value:JsonValue):Int?{
        var i = 0
        items.forEach{it->
            if(it==value){return i}
            i++
        }
        return null
    }
}

data class JsonMap(val items: MutableMap<String, JsonValue>) : JsonValue() {

    override fun accept(visitor: JsonVisitor) {
        visitor.visitMap(this)
        items.values.forEach {
            it?.accept(visitor)
        }
        visitor.endvisitMap(this)
    }

    override fun toString(): String {
        val propertiesString = items.entries.joinToString(", \n", "[ \n", "\n]") { (name, jsonValue) ->
            "  \"$name\": ${jsonValue ?: "null"}"
        }
        return propertiesString
    }

}

data class JsonObject(val properties: MutableMap<String, JsonValue>) : JsonValue() {
    private val observers: MutableList<JsonObjectObserver> = mutableListOf()
    fun addObserver(observer: JsonObjectObserver) {
        observers.add(observer)
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visitObject(this)
        properties.values.forEach {
            it?.accept(visitor)
        }
        visitor.endvisitObject(this)
    }

    override fun toString(): String {
        val propertiesString = properties.entries.joinToString(", \n", "{ \n", "\n}") { (name, jsonValue) ->
            "  \"$name\": ${jsonValue ?: "null"}"
        }
        return propertiesString
    }

    fun addItem(name: String, value: JsonValue) {
        properties[name] = value
    }
    fun removeItem(name: String, value: JsonValue) {
        properties.remove(name,value)
    }
    fun getItem(name: String): JsonValue? {
        return properties[name]
    }
    fun addArrayItem(s:String, value: JsonValue){
        properties.forEach { pair ->
            if (pair.key == s && pair.value is JsonArray) {
                (pair.value as JsonArray).items.add(value)
            }
        }
    }
    fun removeArrayItem(s:String, value: JsonValue){
        properties.forEach { pair ->
            if (pair.key == s && pair.value is JsonArray) {
                (pair.value as JsonArray).items.remove(value)
            }
        }
    }
    fun modifyArrayItem(s:String, value: JsonValue,newValue: JsonValue){
        properties.forEach { pair ->
            if (pair.key == s && pair.value is JsonArray) {
                val index = (pair.value as JsonArray).getIndex(value)
                (pair.value as JsonArray).items[index!!]=newValue
            }
        }
    }
    fun addElementToObject(s: String, value:JsonValue,array:String,obj:JsonObject) {
        properties.forEach { pair ->
            if (pair.key == array && pair.value is JsonArray) {
                ((pair.value as JsonArray).getItem(obj) as JsonObject).addItem(s,value)
            }
        }
    }
    fun removeElementToObject(s: String, value:JsonValue,array:String, obj:JsonObject) {
        properties.forEach { pair ->
            if (pair.key == array && pair.value is JsonArray) {
                ((pair.value as JsonArray).getItem(obj) as JsonObject).removeItem(s,value)
            }
        }
    }
}

interface JsonObjectObserver {
    fun elementAdded(s:String,js:JsonString) {}
    fun elementRemoved(s:String,js:JsonString,e:TableView.ElementComponent) {}
    fun elementModified(s:String,js:JsonString,e: TableView.ElementComponent) {}
    fun arrayAdded(s:String,ja:JsonArray) {}
    fun arrayRemoved(s:String,ja:JsonArray,a: TableView.ArrayComponent) {}
    fun arrayObjectAdded(s:String,parent: TableView.ArrayComponent) {}
    fun arrayObjectRemoved(s:String,parent: TableView.ArrayComponent,obj:JsonObject,o:TableView.ObjectComponent) {}
    fun simpleElementAdded(s:String, js:JsonString,parent: TableView.ArrayComponent) {}
    fun simpleElementRemoved(parent: TableView.ArrayComponent, e: TableView.SimpleElementComponent) {}
    fun simpleElementModified(s:String, js:JsonString, newValue:JsonString,se:TableView.SimpleElementComponent) {}
    fun objectElementAdded(s:String,js:JsonString, o: TableView.ObjectComponent,array:String,obj:JsonObject) {}
    fun objectElementRemoved(s: String, js:JsonString,array:String, obj:JsonObject,parent: TableView.ObjectComponent, e: TableView.ObjectElementComponent) {}
    fun objectElementModified(s:String,js:JsonString,array:String,obj:JsonObject,e: TableView.ObjectElementComponent) {}
    fun mapAdded(s:String,jo:JsonObject) {}
    fun mapRemoved(s: String, jo: JsonObject,mapComponent: TableView.MapComponent) {}
    fun mapElementAdded(jo:JsonObject,s:String, js:JsonString,parent: TableView.MapComponent) {}
    fun mapElementRemoved(parent:TableView.MapComponent,e: TableView.ElementComponent,s:String,js:JsonString) {}
    fun mapElementModified(parent:TableView.MapComponent,s:String, js:JsonString,e: TableView.ElementComponent) {}
}

//---------------------------------------------------------------------------------------------------------------------
//---------------------------------------- Interface Visitor ----------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------


interface JsonVisitor {
    fun visitString(jsonString: JsonString){}
    fun visitNumber(jsonNumber: JsonNumber){}
    fun visitBoolean(jsonBoolean: JsonBoolean){}
    fun visitEnum(jsonEnum: JsonEnum){}
    fun visitArray(jsonArray: JsonArray){}
    fun endvisitArray(jsonArray: JsonArray){}
    fun visitMap(jsonMap: JsonMap){}
    fun endvisitMap(jsonMap: JsonMap){}
    fun visitObject(jsonObject: JsonObject){}
    fun endvisitObject(jsonObject: JsonObject){}
}

//---------------------------------------------------------------------------------------------------------------------
//-------------------------------------- Função Objeto para Modelo Json -----------------------------------------------
//---------------------------------------------------------------------------------------------------------------------
fun dealWithAnotations(property: KProperty1<out Any, *>, jsonMap: MutableMap<String,JsonValue>, obj: Any): Boolean{
    val propertyName = property.name
    val propertyValue = property.getter.call(obj)
    if(property.hasAnnotation<Exclude>()) return true
    if(property.hasAnnotation<ToString>()){
        jsonMap[propertyName] = JsonString(propertyValue.toString())
        return true
    }
    return false
}


fun handleListOrArray(propertyValue: Iterable<*>): JsonArray {
    val jsonArray = propertyValue.map { arrayValue ->
        when (arrayValue) {
            is String -> JsonString(arrayValue)
            is Number -> JsonNumber(arrayValue)
            is Boolean -> JsonBoolean(arrayValue)
            is Enum<*> -> JsonEnum(arrayValue.name)
            is Any -> toJson(arrayValue)
            else -> throw IllegalArgumentException("Unsupported property type for JSON serialization")
        }
    }
    return JsonArray(jsonArray.toMutableList())
}

fun handleMap(propertyValue: Map<*, *>): JsonMap {
    val jsonMap = mutableMapOf<String,JsonValue>()
    propertyValue.map { (key,value) ->
        when (value) {
            is String -> jsonMap[key as String] = JsonString(value)
            is Number -> jsonMap[key as String] = JsonNumber(value)
            is Boolean -> jsonMap[key as String] = JsonBoolean(value)
            is Enum<*> -> jsonMap[key as String] = JsonEnum(value.name)
            is List<*>, is Array<*> -> jsonMap[key as String] = handleListOrArray(value as Iterable<*>)
            is Any -> jsonMap[key as String] = toJson(value)
            else -> throw IllegalArgumentException("Unsupported property type for JSON serialization")
        }
    }
    return JsonMap(jsonMap)
}

fun toJson(obj: Any): JsonObject {
    val jsonObject = mutableMapOf<String,JsonValue>()
    val clazz = obj::class

    clazz.memberProperties.forEach { property ->
        if(dealWithAnotations(property,jsonObject, obj)){
            return@forEach
        }
        val propertyName = property.findAnnotation<Identifier>()?.id ?: property.name

        when (val propertyValue = property.getter.call(obj)) {
            is String -> jsonObject[propertyName] = JsonString(propertyValue)
            is Number -> jsonObject[propertyName] = JsonNumber(propertyValue)
            is Boolean -> jsonObject[propertyName] = JsonBoolean(propertyValue)
            is List<*>, is Array<*> -> jsonObject[propertyName] = handleListOrArray(propertyValue as Iterable<*>)
            is Map<*,*> -> jsonObject[propertyName] = handleMap(propertyValue as Map<*, *>)
            is Enum<*> -> jsonObject[propertyName] = JsonEnum(propertyValue.name)
            is Any -> jsonObject[property.name] = toJson(propertyValue)
            else -> throw IllegalArgumentException("Unsupported property type for JSON serialization")
        }
    }

    return JsonObject(jsonObject)
}


//---------------------------------------------------------------------------------------------------------------------
//------------------------------------- Classes de procura com Visitor ------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------

class GetPropertiesByName(propertieName: String) : JsonVisitor { //------------------------------------------------------------------------------
    val results = mutableListOf<JsonValue>()
    val propertieName = propertieName

    override fun visitObject(jsonObject: JsonObject) {
        jsonObject.properties.forEach { (name, jsonValue) ->
            if (name == propertieName) {
                if (jsonValue != null) {
                    results.add(jsonValue)
                }
            }
        }
    }
    override fun visitMap(jsonMap: JsonMap) {
        jsonMap.items.forEach { (name, jsonValue) ->
            if (name == propertieName) {
                if (jsonValue != null) {
                    results.add(jsonValue)
                }
            }
        }
    }
}

class GetObjectsWithProperties(names: List<String>): JsonVisitor{ //----------------------------------------------------------------------------
    val results = mutableListOf<JsonValue>()
    var counter = 0
    val names = names

    override fun visitObject(jsonObject: JsonObject) {
        jsonObject.properties.forEach { (name, jsonValue) ->
            if (names.contains(name)) {
                counter++
            }
        }
        if(names.size == counter)
            results.add(jsonObject)
    }

    override fun endvisitObject(jsonObject: JsonObject) {
        counter = 0
    }
}


class AllPropertiesAreIntegersValues(name: String): JsonVisitor { //-----------------------------------------------------------
    var result = true
    val currentObject = mutableListOf<JsonObject>()
    val currentMap = mutableListOf<JsonMap>()
    val propretieName = name

    override fun visitObject(jsonObject: JsonObject){
        currentObject.add(jsonObject)
    }

    override fun endvisitObject(jsonObject: JsonObject) {
        currentObject.removeLast()
    }

    override fun visitMap(jsonMap: JsonMap) {
        currentMap.add(jsonMap)
    }

    override fun endvisitMap(jsonMap: JsonMap) {
        currentMap.removeLast()
    }

    override fun visitBoolean(jsonBoolean: JsonBoolean) {
        if(currentMap.size>0){
            val name = getKeyFromValue(jsonBoolean,currentMap.last().items as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }else{
            val name = getKeyFromValue(jsonBoolean,currentObject.last().properties as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }
    }

    override fun visitNumber(jsonNumber: JsonNumber) {
        if(currentMap.size>0){
            val name = getKeyFromValue(jsonNumber,currentMap.last().items as Map<String, JsonValue>)
            if(name == propretieName && jsonNumber.value !is Int)
                result = false
        }else{
            val name = getKeyFromValue(jsonNumber,currentObject.last().properties as Map<String, JsonValue>)
            if(name == propretieName && jsonNumber.value !is Int)
                result = false
        }
    }
    override fun visitString(jsonString: JsonString) {
        if(currentMap.size>0){
            val name = getKeyFromValue(jsonString,currentMap.last().items as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }else{
            val name = getKeyFromValue(jsonString,currentObject.last().properties as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }
    }
    override fun visitEnum(jsonEnum: JsonEnum) {
        if(currentMap.size>0){
            val name = getKeyFromValue(jsonEnum,currentMap.last().items as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }else{
            val name = getKeyFromValue(jsonEnum,currentObject.last().properties as Map<String, JsonValue>)
            if(name == propretieName)
                result = false
        }
    }

}

class AllElementsArraySameStucture(arrayName: String): JsonVisitor {
    var result = false
    val currentObject = mutableListOf<JsonObject>()
    val arrayName = arrayName

    override fun visitArray(jsonArray: JsonArray) {
        val name = getKeyFromValue(jsonArray, currentObject.last().properties as Map<String, JsonValue>)
        if (name == arrayName) {
            val firstItem = jsonArray.items.firstOrNull() as? JsonObject
            if (firstItem != null) {
                val propertiesEqual = jsonArray.items.all { item ->
                    item is JsonObject && item.properties.keys == firstItem.properties.keys
                            && item.properties.values.map { it?.javaClass } == firstItem.properties.values.map { it?.javaClass }
                }
                if (propertiesEqual) {
                    result = true
                }
            }
        }
    }

    override fun visitObject(jsonObject: JsonObject){
        currentObject.add(jsonObject)
    }

    override fun endvisitObject(jsonObject: JsonObject) {
        currentObject.removeLast()
    }
}


fun getKeyFromValue(value: JsonValue, myMap: Map<String,JsonValue>): String? { //--------------------------------------
    for (entry in myMap.entries) {
        if (entry.value == value) {
            return entry.key
        }
    }
    return null
}

class getJsonFormat: JsonVisitor{
    var isInArray = false
    var list = mutableListOf<String>()
    var depth = 0
    val paddingWidth = 2
    val currentObject = mutableListOf<JsonObject>()
    val currentMap = mutableListOf<JsonMap>()

    override fun visitObject(jsonObject: JsonObject){
        var padding = " ".repeat(paddingWidth * depth)
        list.add("$padding{")
        depth++
        currentObject.add(jsonObject)
    }

    override fun endvisitObject(jsonObject: JsonObject) {
        makeAdjustementsClosingObjects()
        val padding = " ".repeat(paddingWidth * depth)
        if(isInArray)
            list.add("$padding},")
        else
            list.add("$padding}")
        currentObject.removeLast()
    }

    override fun visitString(jsonString: JsonString) {
        val padding = " ".repeat(paddingWidth * depth)
        writeSimpleJsonValue(jsonString, padding)
    }

    override fun visitNumber(jsonNumber: JsonNumber) {
        val padding = " ".repeat(paddingWidth * depth)
        writeSimpleJsonValue(jsonNumber, padding)
    }

    override fun visitBoolean(jsonBoolean: JsonBoolean) {
        val padding = " ".repeat(paddingWidth * depth)
        writeSimpleJsonValue(jsonBoolean, padding)
    }

    override fun visitEnum(jsonEnum: JsonEnum) {
        val padding = " ".repeat(paddingWidth * depth)
        writeSimpleJsonValue(jsonEnum, padding)
    }

    override fun visitArray(jsonArray: JsonArray) {
        isInArray = true
        val padding = " ".repeat(paddingWidth * depth)
        val name = getKeyFromValue(jsonArray,currentObject.last().properties as Map<String, JsonValue>)
        list.add("$padding$name : [")
        depth++
    }

    override fun endvisitArray(jsonArray: JsonArray) {
        isInArray = false
        makeAdjustementsClosingObjects()
        val padding = " ".repeat(paddingWidth * depth)
        list.add("$padding],")
    }

    override fun visitMap(jsonMap: JsonMap) {
        val padding = " ".repeat(paddingWidth * depth)
        val name = getKeyFromValue(jsonMap,currentObject.last().properties as Map<String, JsonValue>)
        list.add("$padding$name : [")
        currentMap.add(jsonMap)
        depth++
    }

    override fun endvisitMap(jsonMap: JsonMap) {
        makeAdjustementsClosingObjects()
        val padding = " ".repeat(paddingWidth * depth)
        list.add("$padding],")
        currentMap.removeLast()
    }

    fun writeSimpleJsonValue(jsonValue: JsonValue, padding: String){
        if(isInArray && currentObject.size<=1){
            list.add("$padding\"${jsonValue}\",")
        }else if(currentMap.size!=0){
            val name = getKeyFromValue(jsonValue,currentMap.last().items as Map<String, JsonValue>)
            list.add("$padding\"$name\" : \"${jsonValue}\",")
        }else{
            val name = getKeyFromValue(jsonValue,currentObject.last().properties as Map<String, JsonValue>)
            list.add("$padding\"$name\" : \"${jsonValue}\",")
        }
    }

    fun makeAdjustementsClosingObjects(){
        var str = list.last()
        list.removeLast()
        list.add(str.dropLast(1))
        depth--
    }

    fun printJson() {
        val l = list.joinToString("\n")
        println(l)
    }

    fun returnJson():String {
        return list.joinToString("\n")
    }
}


//---------------------------------------------------------------------------------------------------------------------
//------------------------------------------------ TESTES -------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------


fun main(){

    val student1 = Student(92970, "Rodrigo",true, StudentType.Master)
    val student2 = Student(93145, "João",true, StudentType.Bachelor)
    val inscritos = mutableListOf<Student>()
    inscritos.add(student1)
    inscritos.add(student2)
    val notas = mutableMapOf<String, Int>()
    notas["Rodrigo"] = 19
    notas["João"] = 15
    val uc = Uc("MEI", "PA", 6.0, inscritos, notas)
    val jsonObject = toJson(uc)
    /*val gpn = GetPropertiesByName("number")
    jsonObject.accept(gpn)
    println(gpn.results)*/
    /*val gowp = GetObjectsWithProperties(mutableListOf("name","number"))
    jsonObject.accept(gowp)
    println("All objects that contain the variables name and number: " + gowp.results)*/
    /*val anpiv = AllPropertiesAreIntegersValues("number")
    jsonObject.accept(anpiv)
    println("All number propreties are integers values? " + anpiv.result)*/
    /*val aiss = AllElementsArraySameStucture("inscritos")
    jsonObject.accept(aiss)
    println("All objects from inscritos array have the same structure? " + aiss.result)*/


    val json = getJsonFormat()
    jsonObject.accept(json)
    json.printJson()


}