package tech.codingless.biz.core.plugs.mybaties3;

import java.io.Serializable;
import java.util.function.Function;
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {

}
