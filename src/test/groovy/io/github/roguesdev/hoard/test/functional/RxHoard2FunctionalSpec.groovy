package io.github.roguesdev.hoard.test.functional

import com.squareup.moshi.Types
import io.github.roguesdev.hoard.Hoard
import io.github.roguesdev.hoard.rxjava2.RxHoard2
import io.github.roguesdev.hoard.test.model.Person
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RxHoard2FunctionalSpec extends Specification {
  @Rule TemporaryFolder dir

  void "should work with reactive defaults"() {
    given: 'default build of hoard'
    def hoard = new RxHoard2(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testObservable1 = new TestObserver()
    def testObservable2 = new TestObserver()

    and: 'regular depositor created'
    def depositor = hoard.createDepositor('test', String)

    when: 'delete called and nothing has been saved'
    depositor.delete().subscribe(testObservable1)

    then: 'no exceptions should be thrown'
    testObservable1.assertNoValues()
    testObservable1.onComplete()

    when: 'retrieve has been called and nothing saved'
    depositor.retrieve().subscribe(testObservable2)

    then: 'the retrieved value should be null'
    testObservable2.assertValue(null)
    testObservable2.assertComplete()

    when: 'value is saved then retrieved'
    testObservable1 = new TestSubscriber()
    testObservable2 = new TestSubscriber()
    depositor.store('test').subscribe(testObservable1)
    depositor.retrieve().subscribe(testObservable2)

    then: 'the retrieved value should be the same as the saved value'
    testObservable1.assertNoValues()
    testObservable1.assertComplete()
    testObservable2.assertValue('test')
    testObservable2.assertComplete()

    when: 'delete is called and there was a value saved'
    testObservable1 = new TestSubscriber()
    testObservable2 = new TestSubscriber()
    depositor.delete().subscribe(testObservable1)
    depositor.retrieve().subscribe(testObservable2)

    then: 'retrieve value is null'
    testObservable1.assertNoValues()
    testObservable1.assertComplete()
    testObservable2.assertValue(null)
    testObservable2.assertComplete()

    when: 'null value is saved'
    testObservable1 = new TestSubscriber()
    testObservable2 = new TestSubscriber()
    depositor.store(null).subscribe(testObservable1)
    depositor.retrieve().subscribe(testObservable2)

    then:
    testObservable1.assertNoValues()
    testObservable1.assertComplete()
    testObservable2.assertValue(null)
    testObservable2.assertComplete()
  }

  void "should delete all with reactive"() {
    given:
    def hoard = new RxHoard2(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testObserver = new TestObserver()

    hoard.createDepositor('test1', String).store('Foo')
    hoard.createDepositor('test2', String).store('Bar')
    hoard.createDepositor('test3', String).store('Baz')

    when:
    hoard.deleteAllRx()
      .subscribe(testSubscriber)

    then:
    dir.root.listFiles().toList() == []
    testObserver.assertNoValues()
    testObserver.assertCompleted()
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
