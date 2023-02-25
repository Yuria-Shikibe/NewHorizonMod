package newhorizon.expand.eventsys.annotation;

import newhorizon.expand.eventsys.custom.NumberDisplay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberParam{
	float min() default 0;
	float max() default 10000;
	float stepSize() default 32;
	NumberDisplay display() default NumberDisplay.def;
}
