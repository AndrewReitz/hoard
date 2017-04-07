package io.github.roguesdev.hoard.test.functional

import com.squareup.moshi.Types
import io.github.roguesdev.hoard.Hoard
import io.github.roguesdev.hoard.rxjava.RxHoard
import io.github.roguesdev.hoard.test.model.Person
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import rx.observers.TestSubscriber
import spock.lang.Specification

class RxHoardFunctionalSpec extends Specification {

  @Rule TemporaryFolder dir

  void "should work with reactive defaults"() {
    given: 'default build of RxHoard'
    def hoard = new RxHoard(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    and: 'regular depositor created'
    def depositor = hoard.createDepositor('test', String)

    when: ''
    def exists = depositor.exists().test()

    then:
    exists.assertValue(false)
    exists.assertCompleted()

    when: 'delete called and nothing has been saved'
    def delete = depositor.delete().test()

    then: 'no values received and complete is called'
    delete.assertNoValues()
    delete.onCompleted()

    when: 'retrieve has been called and nothing saved'
    def retrieve = depositor.retrieve().test()

    then: 'there should be no value emitted'
    retrieve.assertNoValues()
    retrieve.assertCompleted()

    when: 'value is saved then retrieved'
    def storeTest = depositor.store('test').test()
    def testExists = depositor.exists().test()
    def retrieveTest = depositor.retrieve().test()

    then: 'the retrieved value should be the same as the saved value'
    storeTest.assertNoValues()
    storeTest.assertCompleted()
    testExists.assertValue(true)
    testExists.assertCompleted()
    retrieveTest.assertValue('test')
    retrieveTest.assertCompleted()

    when: 'delete is called and there was a value saved'
    delete = depositor.delete().test()
    retrieve = depositor.retrieve().test()

    then: 'retrieve only calls onComplete'
    delete.assertNoValues()
    delete.assertCompleted()
    retrieve.assertNoValues()
    retrieve.assertCompleted()

    when: 'null value is saved'
    def nullDeposit = depositor.store(null).test()
    def nullExists = depositor.exists().test()
    def nullRetrieve = depositor.retrieve().test()

    then:
    nullDeposit.assertNoValues()
    nullDeposit.assertCompleted()
    nullExists.assertValue(false)
    nullExists.assertCompleted()
    nullRetrieve.assertNoValues()
    nullRetrieve.assertCompleted()
  }

  void "should delete all with reactive"() {
    given:
    def hoard = new RxHoard(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testSubscriber = new TestSubscriber()

    hoard.createDepositor('test1', String).store('Foo')
    hoard.createDepositor('test2', String).store('Bar')
    hoard.createDepositor('test3', String).store('Baz')

    when:
    hoard.deleteAllRx()
      .subscribe(testSubscriber)

    then:
    dir.root.listFiles().toList() == []
    testSubscriber.assertNoValues()
    testSubscriber.assertCompleted()
  }

  void "should retrieve all with reactive"() {
    given:
    def hoard = new RxHoard(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testSubscriber = new TestSubscriber()

    hoard.createDepositor('test1', String).store('hello world').subscribe()
    hoard.createDepositor('test2', Person).store(new Person(firstName: 'Jack', lastName: 'Sparrow')).subscribe()
    hoard.createDepositor('test3', Types.newParameterizedType(List, Integer)).store([1, 2, 3]).subscribe()

    def expected = [new Hoard.Pair('test1', 'hello world'), new Hoard.Pair('test2',
      new Person(firstName: 'Jack', lastName: 'Sparrow')), new Hoard.Pair('test3', [1, 2, 3])]

    when:
    hoard.retrieveAllRx().subscribe(testSubscriber)


    then:
    testSubscriber.assertValues(*expected)
    testSubscriber.assertCompleted()
  }
}
