package hoard.serialization

import com.squareup.moshi.Types
import hoard.test.model.Person
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class ObjectStreamAdapterSpec extends Specification {

  @Rule TemporaryFolder dir

  @Unroll void "should write #expected from disk and then read it back"() {
    given:
    def classUnderTest = new ObjectStreamSerializer<>()
    def file = dir.newFile('testFile.dat')
    def fileInputStream = new FileInputStream(file)
    def fileOutputStream = new FileOutputStream(file)

    when:
    classUnderTest.serialize(type, expected, fileOutputStream)
    def returnValue = classUnderTest.deserialize(type, fileInputStream)

    then:
    returnValue == expected


    where:
    type                                     | expected
    String                                   | 'hello world'
    Boolean                                  | true
    Integer                                  | 10 as Integer
    Person                                   | new Person(firstName: 'Test', lastName: 'Testerson')
    Types.newParameterizedType(List, Person) | [new Person(firstName: 'Test', lastName: 'Testerson')]
  }
}
