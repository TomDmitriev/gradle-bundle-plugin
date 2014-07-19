package org.dm.gradle.plugins.bundle

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a testing method as a one addressed to a particular github issue.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Documented
@interface Issue {
    /**
     * The number of the github issue the annotated method is addressed to.
     */
    int value()
}