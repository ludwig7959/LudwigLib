package xyz.ludwicz.library.command;

@FunctionalInterface
public interface Processor<T, R> {
    R process(T var1);
}

