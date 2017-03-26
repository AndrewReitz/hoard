package hoard.serialization

import com.squareup.moshi.Types
import hoard.test.model.Person
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class MoshiAdapterSpec extends Specification {

  @Rule TemporaryFolder dir

  @Unroll void "should write #intput to disk"() {
    given:
    def classUnderTest = new MoshiSerializer<>()
    def file = dir.newFile('testFile.json')
    def stream = new FileOutputStream(file)

    when:
    classUnderTest.serialize(type, input, stream)

    then:
    file.text == expected

    where:
    input                                                  | type                                     | expected
    'hello world'                                          | String                                   | '\"hello world\"'
    true                                                   | Boolean                                  | 'true'
    10 as Integer                                          | Integer                                  | '10'
    new Person(firstName: 'Test', lastName: 'Testerson')   | Person                                   | '{"firstName":"Test","lastName":"Testerson"}'
    [new Person(firstName: 'Test', lastName: 'Testerson')] | Types.newParameterizedType(List, Person) | '[{"firstName":"Test","lastName":"Testerson"}]'
  }

  @Unroll void "should read #readValue from disk"() {
    given:
    def classUnderTest = new MoshiSerializer<>()
    def file = dir.newFile('testFile.json')
    def stream = new FileInputStream(file)

    file << readValue

    when:
    def returnValue = classUnderTest.deserialize(type, stream)

    then:
    returnValue == expected


    where:
    readValue                                       | type                                     | expected
    '\"hello world\"'                               | String                                   | 'hello world'
    'true'                                          | Boolean                                  | true
    '10'                                            | Integer                                  | 10 as Integer
    '{"firstName":"Test","lastName":"Testerson"}'   | Person                                   | new Person(firstName: 'Test', lastName: 'Testerson')
    '[{"firstName":"Test","lastName":"Testerson"}]' | Types.newParameterizedType(List, Person) | [new Person(firstName: 'Test', lastName: 'Testerson')]
  }
}
