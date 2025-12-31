package com.github.loadup.modules.upms.infrastructure.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Operation Log Annotation Mark methods that need operation logging
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

  /** Operation type: CREATE/UPDATE/DELETE/QUERY/LOGIN/LOGOUT etc. */
  String type();

  /** Operation module */
  String module();

  /** Operation description */
  String description() default "";

  /** Whether to record response */
  boolean recordResponse() default false;
}
