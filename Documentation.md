# JSON Library
Este repositório contém um conjunto de classes e interfaces que implementam o padrão Visitor para processamento de objetos JSON. O objetivo é fornecer uma estrutura flexível e extensível para lidar com a serialização de objetos JSON em Kotlin.

## Classes de Representação de Objetos JSON

### 'JsonValue'
Classe abstrata base para todos os tipos de valores JSON. Fornece um método accept para aceitar um visitante.

Métodos:
- **accept(visitor: JsonVisitor)**: Aceita um visitante para processamento.

### 'JsonString'
Representa um valor de string JSON.

Propriedades:
- **value: String?**: O valor da string.

### 'JsonNumber'
Representa um valor numérico JSON.

Propriedades:
- **value: Number?**: O valor numérico.

### 'JsonBoolean'
Representa um valor booleano JSON.

Propriedades:
- **value: Boolean?**: O valor booleano.

### 'JsonEnum'
Representa um valor de enumeração JSON.

Propriedades:
- **value: String?**: O valor da enumeração.

### 'JsonArray'
Representa um array JSON, contendo uma lista de JsonValue.

Propriedades:
- **items: List<JsonValue?>**: A lista de valores JSON.

### 'JsonMap'
Representa um mapa JSON, onde as chaves são strings e os valores são JsonValue.

Propriedades:
- **items: Map<String, JsonValue?>**: O mapa de valores JSON.

### 'JsonObject'
Representa um objeto JSON, com propriedades representadas como um mapa de strings para JsonValue.

Propriedades:
- **properties: Map<String, JsonValue?>**: O mapa de propriedades JSON.

## Observadores em JsonObject
A classe JsonObject representa um objeto JSON e fornece funcionalidades para observar as alterações feitas em suas propriedades. Observadores podem ser registrados em uma instância de JsonObject para receber notificações quando elementos são adicionados ou removidos do objeto.

## Interface JsonVisitor
Interface que define os métodos de visita para cada tipo de valor JSON.

Métodos:
- **visitString(jsonString: JsonString)**: Visita um valor de string JSON.
- **visitNumber(jsonNumber: JsonNumber)**: Visita um valor numérico JSON.
- **visitBoolean(jsonBoolean: JsonBoolean)**: Visita um valor booleano JSON.
- **visitEnum(jsonEnum: JsonEnum)**: Visita um valor de enumeração JSON.
- **visitArray(jsonArray: JsonArray)**: Visita um array JSON.
- **endvisitArray(jsonArray: JsonArray)**: Finaliza a visita a um array JSON.
- **visitMap(jsonMap: JsonMap)**: Visita um mapa JSON.
- **endvisitMap(jsonMap: JsonMap)**: Finaliza a visita a um mapa JSON.
- **visitObject(jsonObject: JsonObject)**: Visita um objeto JSON.
- **endvisitObject(jsonObject: JsonObject)**: Finaliza a visita a um objeto JSON.

O **Visitor** permite executar operações específicas em cada tipo de valor JSON sem a necessidade de modificar a estrutura das classes que representam esses valores. Por exemplo, ao implementar a interface **JsonVisitor** e fornecer a implementação desses métodos, é possível executar diferentes ações ao visitar um objeto JSON, como realizar a serialização em JSON, realizar validações ou qualquer outra operação desejada, isto vai ser muito usado mais à frente nas **Classes de Funcionalidades**.

## Função Serialização
O código também inclui algumas funções auxiliares para lidar com a serialização de objetos em JSON.

- **toJson(obj: Any)**
Realiza a serialização de um objeto para um objeto JSON do tipo JsonObject. Essa função percorre as propriedades do objeto e os valores correspondentes são convertidos em instâncias apropriadas de JsonValue e adicionados ao objeto JSON resultante.

Parâmetros: 

**obj: Any**: O objeto a ser serializado em JSON.

Retorno: 

**JsonObject**: O objeto JSON resultante da serialização do objeto.

- Exemplo de utilização:
```kotlin
// Definindo uma classe de modelo
data class Person(val name: String, val age: Int)

fun main() {
    // Criando uma instância de Person
    val person = Person("John Doe", 30)

    // Serializando o objeto em JSON
    val jsonObject = toJson(person)
}
``` 

## Uso das Anotações
Neste código, são utilizadas anotações para adicionar metadados às propriedades das classes. Esses metadados podem influenciar o processo de serialização para JSON. Abaixo, você encontrará uma explicação de cada anotação e como utilizá-las em suas classes.

### Anotação Exclude
A anotação **Exclude** é utilizada para marcar uma propriedade que deve ser excluída durante o processo de serialização para JSON. Isso significa que o valor dessa propriedade não será incluído no objeto JSON resultante. Para utilizar essa anotação, basta adicionar **@Exclude** acima da declaração da propriedade que deseja excluir. Por exemplo:
```kotlin
class MyClass {
    @Exclude
    val excludedProperty: String = "Excluded value"
}
``` 
Nesse caso, a propriedade **'excludedProperty'** será excluída durante a serialização para JSON.

### Anotação ToString
A anotação **ToString** é utilizada para serializar uma propriedade como uma representação em string do seu valor. Em outras palavras, o valor da propriedade será convertido em uma string antes de ser incluído no objeto JSON resultante. Para utilizar essa anotação, basta adicionar **@ToString** acima da declaração da propriedade que deseja serializar como string. Por exemplo:
```kotlin
class MyClass {
    @ToString
    val stringProperty: Int = 42
}
``` 
Nesse caso, a propriedade **'stringProperty'** será convertida em uma string durante a serialização para JSON.

### Anotação Identifier
A anotação **Identifier** é utilizada para definir um identificador personalizado para uma propriedade durante o processo de serialização para JSON. Por padrão, o nome da propriedade é utilizado como identificador no objeto JSON resultante. No entanto, com a anotação Identifier, você pode fornecer um nome alternativo para o identificador. Para utilizar essa anotação, basta adicionar **@Identifier("nome_do_identificador")** acima da declaração da propriedade que deseja alterar o identificador. Por exemplo:
```kotlin
class MyClass {
    @Identifier("customIdentifier")
    val propertyWithCustomIdentifier: String = "Value"
}
``` 
Nesse caso, a propriedade **'propertyWithCustomIdentifier'** será incluída no objeto JSON com o identificador **'customIdentifier'**.


## Classes de Funcionalidades
O código também inclui algumas classes de funcionalidades que podem ser aplicadas aos objetos JSON.

A classe **GetPropertiesByName(propretieName: String)** implementa a interface JsonVisitor e é usada para obter as propriedades de um objeto JSON com base em um nome de propriedade específico. Ela armazena os resultados em uma lista de JsonValue chamada **results**.

A classe **GetObjectsWithProperties(propretiesNames: List<String>)** também implementa a interface JsonVisitor e é usada para obter objetos JSON que possuem um conjunto específico de propriedades. Ela armazena os resultados em uma lista de JsonValue chamada **results**.

A classe **AllPropertiesAreIntegerValues** implementa a interface JsonVisitor e é usada para verificar se todas as propriedades de um objeto JSON que possuem um nome específico são todos valores numéricos inteiros. Ela armazena o resultado em uma variável booleana chamada **result**.

A classe **AllArrayElementsSameStucture** implementa a interface JsonVisitor e é usada para verificar se todos os elementos de um determinado array JSON possuem a mesma estrutura (mesmas propriedades). Ela armazena o resultado em uma variável booleana chamada **result**.

A classe **getJsonFormat** implementa a interface JsonVisitor e é usada para criar uma representação em formato JSON formatada e legível para o objeto JSON visitado. 

- Exemplo de utilização da funcionalidade **GetPropertiesByName** (uso das outras é similar):
```kotlin
val gpn = GetPropertiesByName("number")
jsonObject.accept(gpn)
println(gpn.results)
``` 
- Output:
```kotlin
[92970, 93145]
``` 
Isto criará uma instância de **GetPropertiesByName** para buscar as propriedades com o nome **"number"**. Em seguida, o objeto JSON jsonObject será visitado pela instância de **GetPropertiesByName**, que armazenará os resultados na lista **results**. Por fim, os resultados serão impressos na consola. Neste caso existiam duas propriedades com o nome **"number"** e eram dois inteiros, o **92970** e **93145**.




