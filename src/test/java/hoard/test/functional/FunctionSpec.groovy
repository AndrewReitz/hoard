package hoard.test.functional

import hoard.Hoard
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class FunctionSpec extends Specification {

  @Rule TemporaryFolder dir

  void "should work with defaults"() {
    given: 'default build of hoard'
    def hoard = Hoard.builder().with {
      rootDirectory(dir.newFolder())
      build()
    }

    and: 'regular depositor created'
    def depositor = hoard.createDepositor('test', String)

    when: 'delete called and nothing has been saved'
    depositor.delete()

    then: 'no exceptions should be thrown'
    noExceptionThrown()

    when: 'retrieve has been called and nothing saved'
    def retrievedValue = depositor.retrieve()

    then: 'the retrieved value should be null'
    retrievedValue == null

    when: 'value is saved then retrieved'
    depositor.save('test')
    retrievedValue = depositor.retrieve()

    then: 'the retrieved value should be the same as the saved value'
    retrievedValue == 'test'

    when: 'delete is called and there was a value saved'
    depositor.delete()
    retrievedValue = depositor.retrieve()

    then: 'retrieve value is null'
    retrievedValue == null

    when: 'null value is saved'
    depositor.save(null)
    retrievedValue = depositor.retrieve()

    then:
    retrievedValue == null
    noExceptionThrown()
  }
}
