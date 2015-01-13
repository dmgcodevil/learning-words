package com.github.learningwords.util


import java.lang.reflect.ParameterizedType

import com.j256.ormlite.dao.BaseDaoImpl

/**
 * @author dmgcodevil
 */
object ReflectionUtils {


  def getEntityType(clazz: Class[_]): Class[_] = {
    var gtype = clazz.getGenericSuperclass

    while (!gtype.isInstanceOf[ParameterizedType] || gtype.asInstanceOf[ParameterizedType].getRawType != classOf[BaseDaoImpl[_,_]]) {
      gtype match {
        case parameterizedType: ParameterizedType =>
          gtype = parameterizedType.getRawType.asInstanceOf[Class[_]].getGenericSuperclass
        case _ =>
          gtype = gtype.asInstanceOf[Class[_]].getGenericSuperclass
      }
    }

     gtype.asInstanceOf[ParameterizedType].getActualTypeArguments()(0).asInstanceOf[Class[_]]
  }

}
