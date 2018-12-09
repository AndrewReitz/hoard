package io.github.roguesdev.hoard.test.functional

import io.github.roguesdev.hoard.Hoard
import io.github.roguesdev.hoard.serialization.ObjectStreamSerializer
import io.github.roguesdev.hoard.test.model.Animal
import io.reactivex.subscribers.TestSubscriber
import org.amshove.kluent.shouldEqual
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HoardKotlinFunctionalSpec {

  @Rule
  @JvmField
  val dir = TemporaryFolder()

  @Test fun `should use reified createDepositor to create a depositor and save values`() {
    val hoard = Hoard(
      rootDirectory = dir.root,
      serializer = ObjectStreamSerializer()
    )

    val test = hoard.createDepositor<Animal>("test")
    val nullResult = test.retrieve()

    nullResult shouldEqual null

    test.store(value = Animal("Jack"))

    val animalJackResult = test.retrieve()

    animalJackResult shouldEqual Animal("Jack")
  }

  @Test fun `should use reified create to create a depositor and save values`() {
    val hoard = Hoard(
      rootDirectory = dir.root,
      serializer = ObjectStreamSerializer()
    )

    val test = hoard.createReactiveDepositor<Animal>("test")

    with(TestSubscriber<Animal>()) {
      test.retrieve().subscribe(this)
      assertResult()
    }

    test.store(value = Animal(name = "Jack")).subscribe(TestSubscriber())

    with(TestSubscriber<Animal>()) {
      test.retrieve().subscribe(this)
      assertResult(Animal(name = "Jack"))
    }
  }
}
