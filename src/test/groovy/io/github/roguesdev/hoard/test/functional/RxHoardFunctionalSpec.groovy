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
    given: 'default build of hoard'
    def hoard = new RxHoard(Hoard.builder().with {
      rootDirectory(dir.root)
      build()
    })

    def testSubscriber1 = new TestSubscriber()
    def testSubscriber2 = new TestSubscriber()

    and: 'regular depositor created'
    def depositor = hoard.createDepositor('test', String)

    when: 'delete called and nothing has been saved'
    depositor.delete().subscribe(testSubscriber1)

    then: 'no exceptions should be thrown'
    testSubscriber1.assertNoValues()
    testSubscriber1.onCompleted()

    when: 'retrieve has been called and nothing saved'
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'the retrieved value should be null'
    testSubscriber2.assertValue(null)
    testSubscriber2.assertCompleted()

    when: 'value is saved then retrieved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.store('test').subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'the retrieved value should be the same as the saved value'
    testSubscriber1.assertNoValues()
    testSubscriber1.assertCompleted()
    testSubscriber2.assertValue('test')
    testSubscriber2.assertCompleted()

    when: 'delete is called and there was a value saved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.delete().subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then: 'retrieve value is null'
    testSubscriber1.assertNoValues()
    testSubscriber1.assertCompleted()
    testSubscriber2.assertValue(null)
    testSubscriber2.assertCompleted()

    when: 'null value is saved'
    testSubscriber1 = new TestSubscriber()
    testSubscriber2 = new TestSubscriber()
    depositor.store(null).subscribe(testSubscriber1)
    depositor.retrieve().subscribe(testSubscriber2)

    then:
    testSubscriber1.assertNoValues()
    testSubscriber1.assertCompleted()
    testSubscriber2.assertValue(null)
    testSubscriber2.assertCompleted()
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
