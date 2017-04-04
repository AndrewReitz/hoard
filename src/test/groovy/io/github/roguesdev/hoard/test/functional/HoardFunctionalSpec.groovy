package io.github.roguesdev.hoard.test.functional

import com.squareup.moshi.Types
import io.github.roguesdev.hoard.Hoard
import io.github.roguesdev.hoard.test.model.Person
import io.reactivex.subscribers.TestSubscriber
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class HoardFunctionalSpec extends Specification {

  @Rule TemporaryFolder dir

  void "should work with defaults"() {
    given: 'default build of hoard'
    def hoard = Hoard.builder().with {
      rootDirectory(dir.root)
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
    depositor.store('test')
    retrievedValue = depositor.retrieve()

    then: 'the retrieved value should be the same as the saved value'
    retrievedValue == 'test'

    when: 'delete is called and there was a value saved'
    depositor.delete()
    retrievedValue = depositor.retrieve()

    then: 'retrieve value is null'
    retrievedValue == null

    when: 'null value is saved'
    depositor.store(null)
    retrievedValue = depositor.retrieve()

    then:
    retrievedValue == null
    noExceptionThrown()
  }

  void "should delete all values"() {
    given:
    def hoard = Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    }

    hoard.createDepositor('test1', String).store('Foo')
    hoard.createDepositor('test2', String).store('Bar')
    hoard.createDepositor('test3', String).store('Baz')

    when:
    hoard.deleteAll()

    then:
    dir.root.listFiles().toList() == []
  }

  void "should work with reactive defaults"() {
    given: 'default build of hoard'
    def hoard = Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    }

    def testSubscriber1 = new TestSubscriber()
    def testSubscriber2 = new TestSubscriber()

    and: 'regular depositor created'
    def depositor = hoard.createReactiveDepositor('test', String)

    when: 'delete called and nothing has been saved'
    depositor.delete().subscribe(testSubscriber1)

    then: 'no exceptions should be thrown'
    testSubscriber1.assertNoValues()
    testSubscriber1.onComplete()

    when: 'retrieve has been called and nothing saved'
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'the retrieved value should be null'
    testSubscriber2.assertValue(null)
    testSubscriber2.assertComplete()

    when: 'value is saved then retrieved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.store('test').subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'the retrieved value should be the same as the saved value'
    testSubscriber1.assertNoValues()
    testSubscriber1.assertComplete()
    testSubscriber2.assertValue('test')
    testSubscriber2.assertComplete()

    when: 'delete is called and there was a value saved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.delete().subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'retrieve value is null'
    testSubscriber1.assertNoValues()
    testSubscriber1.assertComplete()
    testSubscriber2.assertValue(null)
    testSubscriber2.assertComplete()

    when: 'null value is saved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.store(null).subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then:
    testSubscriber1.assertNoValues()
    testSubscriber1.assertComplete()
    testSubscriber2.assertValue(null)
    testSubscriber2.assertComplete()
  }

  void "should retrieve all values"() {
    given:
    def hoard = Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    }

    hoard.createDepositor('test1', String).store('hello world')
    hoard.createDepositor('test2', Person).store(new Person(firstName: 'Jack', lastName: 'Sparrow'))
    hoard.createDepositor('test3', Types.newParameterizedType(List, Integer)).store([1, 2, 3])

    when:
    def allValues = hoard.retrieveAll()


    then:
    allValues['test1'] == 'hello world'
    allValues['test2'] == new Person(firstName: 'Jack', lastName: 'Sparrow')
    allValues['test3'] == [1, 2, 3]
  }
}
