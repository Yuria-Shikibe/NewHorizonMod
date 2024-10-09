package newhorizon.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All {@link ElementType#METHOD} & {@link ElementType#FIELD} & {@link ElementType#CONSTRUCTOR} annotated by this means cannot be
 * used in the headless server or should have no effects in headless server.
 * */

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface HeadlessDisabled{}
