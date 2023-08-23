package newhorizon.expand.eventsys.custome;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ParserType{
	Class<?> value();
	Class<?>[] genericParams() default {};
}
