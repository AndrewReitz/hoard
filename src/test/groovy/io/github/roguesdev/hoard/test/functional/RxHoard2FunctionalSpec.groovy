package io.github.roguesdev.hoard.test.functional

import com.squareup.moshi.Types
import io.github.roguesdev.hoard.Hoard
import io.github.roguesdev.hoard.rxjava2.RxDepositor
import io.github.roguesdev.hoard.rxjava2.RxHoard2
import io.github.roguesdev.hoard.test.model.Person
import io.reactivex.observers.TestObserver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RxHoard2FunctionalSpec extends Specification {
  @Rule TemporaryFolder dir

  void "should work with reactive defaults"() {
    given: 'RxHoard2 created from builder'
    def hoard = new RxHoard2(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    and: 'depositor created'
    def depositor = hoard.createDepositor('test', String)

    when: 'exists called'
    def exists = depositor.exists().test()

    then: 'returns false'
    exists.assertValue(false)
    exists.assertComplete()

    when: 'delete called and nothing has been saved'
    def delete = depositor.delete().test()

    then: 'no exceptions should be thrown'
    delete.assertNoValues()
    delete.onComplete()

    when: 'retrieve has been called and nothing saved'
    def retrieve = depositor.retrieve().test()

    then: 'no such element exception' // single can not return null / skip elements
    retrieve.assertError(NoSuchElementException)

    when: 'value is saved then retrieved'
    def testStore = depositor.store('test').test()
    def testRetrieve = depositor.retrieve().test()

    then: 'the retrieved value should be the same as the saved value'
    testStore.assertNoValues()
    testStore.assertComplete()
    testRetrieve.assertValue('test')
    testRetrieve.assertComplete()

    when: 'delete is called and there was a value saved'
    def testDelete = depositor.delete().test()
    testRetrieve = depositor.retrieve().test()

    then: 'retrieve value is null'
    testDelete.assertNoValues()
    testDelete.assertComplete()
    testRetrieve.assertError(NoSuchElementException)

    when: 'null value resets the existence'
    def storeNull = depositor.store(null).test()
    testRetrieve = depositor.retrieve().test()

    then:
    storeNull.assertNoValues()
    storeNull.assertComplete()
    testRetrieve.assertError(NoSuchElementException)
  }

  void "should delete all with reactive"() {
    given:
    def hoard = new RxHoard2(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    hoard.createDepositor('test1', String).store('Foo')
    hoard.createDepositor('test2', String).store('Bar')
    hoard.createDepositor('test3', String).store('Baz')

    when:
    def testObserver = hoard.deleteAllRx().test()

    then:
    dir.root.listFiles().toList() == []
    testObserver.assertNoValues()
    testObserver.assertComplete()
  }

  void "should retrieve all with reactive"() {
    given:
    def hoard = new RxHoard2(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testObserver = new TestObserver()

    hoard.createDepositor('test1', String).store('hello world').subscribe()
    hoard.createDepositor('test2', Person).store(new Person(firstName: 'Jack', lastName: 'Sparrow')).subscribe()
    hoard.createDepositor('test3', Types.newParameterizedType(List, Integer)).store([1, 2, 3]).subscribe()

    def expected = [new Hoard.Pair('test1', 'hello world'), new Hoard.Pair('test2',
      new Person(firstName: 'Jack', lastName: 'Sparrow')), new Hoard.Pair('test3', [1, 2, 3])]

    when:
    hoard.retrieveAllRx().subscribe(testObserver)


    then:
    testObserver.assertValues(*expected)
    testObserver.assertComplete()
  }
}
