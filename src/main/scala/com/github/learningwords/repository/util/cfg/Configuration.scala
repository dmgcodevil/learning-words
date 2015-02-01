package com.github.learningwords.repository.util.cfg

import com.github.learningwords.domain.{Profile, Word, Translation, Domain}

/**
 * @author dmgcodevil
 */
object Configuration {
  val entities = Set[Class[_ <: Domain]](
    classOf[Profile],
    classOf[Word],
    classOf[Translation]
  )

}
