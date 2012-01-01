package com.sybrix.easygsp.util

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 12/4/11
 * Time: 11:20 AM
 */
class Mixins {
        static def doMixins(){
                BigDecimal.mixin RoundingMixin
        }
}
