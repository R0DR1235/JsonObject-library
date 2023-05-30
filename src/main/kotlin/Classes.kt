import java.util.*

@Target(AnnotationTarget.CLASS,AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class DbName(val value: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Exclude

@Target(AnnotationTarget.PROPERTY)
annotation class Identifier(val id: String)

@Target(AnnotationTarget.PROPERTY)
annotation class ToString


@DbName("STUDENT")
data class Student(
    val number: Int,
    val name: String,
    @Exclude
    val active: Boolean,
    @Identifier("Level")
    val type: StudentType
)

enum class StudentType {
    Bachelor, Master, Doctoral
}


data class Uc(
    val course: String,
    val uc: String,
    @ToString
    val etcs: Double,
    val inscritos: List<Student>,
    val notas: Map<String,Int>
)