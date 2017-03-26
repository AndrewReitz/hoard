package hoard.test.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Person implements Serializable {
  String firstName
  String lastName
}
