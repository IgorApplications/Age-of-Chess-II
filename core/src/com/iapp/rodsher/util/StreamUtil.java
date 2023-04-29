package com.iapp.rodsher.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The utility supports the conversion of an iterative approach to
 * a functional one by converting Iterable to Stream and vice versa.
 * @author Igor Ivanov
 * @version 1.0
 * */
public class StreamUtil {

    /** converts the interface Stream to an Iterable */
    public static <E> Stream<E> streamOf(Iterable<E> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /** converts the interface Iterable to a Stream */
    public static <E> Iterable<E> iterableOf(Stream<E> stream) {
        return stream::iterator;
    }
}
