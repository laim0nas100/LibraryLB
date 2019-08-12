package lt.lb.commons;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Supplier;
import lt.lb.commons.containers.caching.LazyValue;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class DefaultLogDecorators {

    public static Lambda.L5R<Log, String, String, Long, String, String> finalPrintDecorator() {
        return (Log log, String trace, String name, Long millis, String string) -> {
            String timeSt = log.timeStamp ? Log.getZonedDateTime(log.timeStringFormat, millis) : "";
            String threadSt = log.threadName ? "[" + name + "]" : "";
            if (!trace.isEmpty()) {
                int firstComma = trace.indexOf("(");
                int lastComma = trace.indexOf(")");
                if (firstComma > 0 && lastComma > firstComma && lastComma > 0) {
                    trace = "@" + trace.substring(firstComma + 1, lastComma) + ":";
                }
            }
            String str = log.surroundString ? "{" + string + "}" : string;
            return timeSt + threadSt + trace + str;
        };
    }

    public static Lambda.L1R<Object[], Supplier<String>> printLnDecorator() {
        return (Object[] objs) -> {
            return () -> {
                LineStringBuilder sb = new LineStringBuilder();
                if (objs.length == 1) {
                    sb.append(String.valueOf(objs[0]));
                } else if (objs.length > 1) {
                    for (Object s : objs) {
                        sb.appendLine(String.valueOf(s));
                    }
                    sb.removeFromEnd(sb.lineEnding.length());
                }
                return sb.toString();
            };
        };
    }

    public static Lambda.L1R<Object[], Supplier<String>> printDecorator() {
        return (Object[] objs) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                if (objs.length > 0) {
                    for (Object s : objs) {
                        string.append(", " + s);
                    }
                    string.delete(0, 2);
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L1R<Iterable, Supplier<String>> printLinesDecorator() {
        return (Iterable col) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                boolean empty = true;
                for (Object s : col) {
                    empty = false;
                    string.appendLine(s);
                }
                if (!empty) {
                    string.prependLine();
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L1R<Iterator, Supplier<String>> printIterDecorator() {
        return (Iterator col) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                ReadOnlyIterator iter = ReadOnlyIterator.of(col);
                if (iter.hasNext()) {
                    for (Object s : iter) {
                        string.appendLine(s);
                    }
                    string.prependLine();
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L1R<Throwable, Supplier<String>> stackTraceUnsafeSupplier() {
        LazyValue<Method> thMethod = new LazyValue<>(() -> {
            try {
                Method declaredMethod = Throwable.class.getDeclaredMethod("getStackTraceElement", Integer.TYPE);
                declaredMethod.setAccessible(true);
                return declaredMethod;
            } catch (Exception e) {
                throw NestedException.of(e);
            }
        });

        return (th) -> {
            return () -> {
                Value<String> trace = new Value<>();
                F.unsafeRun(() -> {
                    trace.set(StringOp.remove(thMethod.get().invoke(th, 3).toString(), ".java"));
                });
                return null;
            };
        };

    }

    public static Lambda.L1R<Throwable, Supplier<String>> stackTraceSupplier() {
        return (th) -> () -> th.getStackTrace()[3].toString().replace(".java", "");
    }
}
