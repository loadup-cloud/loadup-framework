package com.github.loadup.commons.error;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.result.ResultCode;
import com.github.loadup.commons.util.StringUtils;
import org.apache.commons.collections4.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Laysan
 * @since 1.0.0
 */

public class AssertUtil {
	private AssertUtil() {
	}

	public static void isTrue(boolean expValue, ResultCode resultCode, Object... objs) {
		if (!expValue) {
			String logString = getLogString(objs);
			String resultMsg = StringUtils.isBlank(logString) ? resultCode.getMessage() : logString;
			throw new CommonException(resultCode, resultMsg);
		}
	}

	public static void isTrue(boolean expValue, ResultCode resultCode, AssertionCallback callback) {
		if (!expValue) {
			callback.failAction();
			isTrue(false, resultCode);
		}
	}

	public static void isFalse(boolean expValue, ResultCode resultCode, Object... objs) {
		isTrue(!expValue, resultCode, objs);
	}

	public static void equals(Object obj1, Object obj2, ResultCode resultCode, Object... objs) {
		isTrue(Objects.equals(obj1, obj2), resultCode, objs);
	}

	public static void notEquals(Object obj1, Object obj2, ResultCode resultCode, Object... objs) {
		isTrue(!Objects.equals(obj1, obj2), resultCode, objs);
	}

	public static void is(Object base, Object target, ResultCode resultCode, Object... objs) {
		isTrue(base == target, resultCode, objs);
	}

	public static void isNot(Object base, Object target, ResultCode resultCode, Object... objs) {
		isTrue(base != target, resultCode, objs);
	}

	public static void contains(Object base, Collection<?> collection, ResultCode resultCode, Object... objs) {
		notEmpty(collection, resultCode, objs);
		isTrue(collection.contains(base), resultCode, objs);
	}

	public static void in(Object base, Object[] collection, ResultCode resultCode, Object... objs) {
		notNull(collection, resultCode, objs);
		boolean hasEqual = Arrays.stream(collection).anyMatch(obj2 -> base == obj2);

		isTrue(hasEqual, resultCode, objs);
	}

	public static void notIn(Object base, Object[] collection, ResultCode resultCode, Object... objs) {
		if (null != collection) {
			for (Object obj2 : collection) {
				isTrue(base != obj2, resultCode, objs);
			}
		}
	}

	public static void blank(String str, ResultCode resultCode, Object... objs) {
		isTrue(StringUtils.isBlank(str), resultCode, objs);
	}

	public static void notBlank(String str, ResultCode resultCode, Object... objs) {
		isTrue(StringUtils.isNotBlank(str), resultCode, objs);
	}

	public static void isNull(Object object, ResultCode resultCode, Object... objs) {
		isTrue(object == null, resultCode, objs);
	}

	public static void notNull(Object object, ResultCode resultCode, Object... objs) {
		isTrue(object != null, resultCode, objs);
	}

	public static void notEmpty(Collection<?> collection, ResultCode resultCode, Object... objs) {
		isTrue(!CollectionUtils.isEmpty(collection), resultCode, objs);
	}

	public static void empty(Collection<?> collection, ResultCode resultCode, Object... objs) {
		isTrue(CollectionUtils.isEmpty(collection), resultCode, objs);
	}

	public static void notEmpty(Map<?, ?> map, ResultCode resultCode, Object... objs) {
		isTrue(!MapUtils.isEmpty(map), resultCode, objs);
	}

	public static void empty(Map<?, ?> map, ResultCode resultCode, Object... objs) {
		isTrue(MapUtils.isEmpty(map), resultCode, objs);
	}

	public static void deductNotBlank(boolean condition, String str, ResultCode resultCode, Object... objs) {
		if (condition) {
			notBlank(str, resultCode, objs);
		}
	}

	public static void deductBlank(boolean condition, String str, ResultCode resultCode, Object... objs) {
		if (condition) {
			blank(str, resultCode, objs);
		}
	}

	public static void deductTrue(boolean condition, boolean expValue, ResultCode resultCode, Object... objs) {
		if (condition) {
			isTrue(expValue, resultCode, objs);
		}
	}

	private static String getLogString(Object... objs) {
		return Arrays.stream(objs).map(String::valueOf).collect(Collectors.joining());
	}

	private static final String TEMPLATE_VALUE_MUST_BE_BETWEEN_AND = "The value must be between {} and {}.";

	/**
	 * 断言是否为真，如果为 {@code false} 抛出给定的异常<br>
	 *
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0, IllegalArgumentException::new);
	 * </pre>
	 *
	 * @throws X if expression is {@code false}
	 */
	public static <X extends Throwable> void isTrue(final boolean expression, final Supplier<? extends X> supplier) throws X {
		if (!expression) {
			throw supplier.get();
		}
	}

	/**
	 * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常<br>
	 *
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if expression is {@code false}
	 */
	public static void isTrue(final boolean expression, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		isTrue(expression, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常<br>
	 *
	 * <pre class="code">
	 * Assert.isTrue(i &gt; 0);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if expression is {@code false}
	 */
	public static void isTrue(final boolean expression) throws IllegalArgumentException {
		isTrue(expression, "[Assertion failed] - this expression must be true");
	}

	/**
	 * 断言是否为假，如果为 {@code true} 抛出指定类型异常<br>
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 *  Assert.isFalse(i &gt; 0, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if expression is {@code false}
	 * @since 5.4.5
	 */
	public static <X extends Throwable> void isFalse(final boolean expression, final Supplier<X> errorSupplier) throws X {
		if (expression) {
			throw errorSupplier.get();
		}
	}

	/**
	 * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常<br>
	 *
	 * <pre class="code">
	 * Assert.isFalse(i &lt; 0, "The value must not be negative");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if expression is {@code false}
	 */
	public static void isFalse(final boolean expression, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		isFalse(expression, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常<br>
	 *
	 * <pre class="code">
	 * Assert.isFalse(i &lt; 0);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if expression is {@code false}
	 */
	public static void isFalse(final boolean expression) throws IllegalArgumentException {
		isFalse(expression, "[Assertion failed] - this expression must be false");
	}

	/**
	 * 断言对象是否为{@code null} ，如果不为{@code null} 抛出指定类型异常
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.isNull(value, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if the object is not {@code null}
	 * @since 5.4.5
	 */
	public static <X extends Throwable> void isNull(final Object object, final Supplier<X> errorSupplier) throws X {
		if (null != object) {
			throw errorSupplier.get();
		}
	}

	/**
	 * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
	 * <pre class="code">
	 * Assert.isNull(value, "The value must be null");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is not {@code null}
	 */
	public static void isNull(final Object object, final String errorMsgTemplate, final Object... params) throws IllegalArgumentException {
		isNull(object, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
	 * <pre class="code">
	 * Assert.isNull(value);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is not {@code null}
	 */
	public static void isNull(final Object object) throws IllegalArgumentException {
		isNull(object, "[Assertion failed] - the object argument must be null");
	}

	// ----------------------------------------------------------------------------------------------------------- Check not null

	/**
	 * 断言对象是否不为{@code null} ，如果为{@code null} 抛出指定类型异常
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.notNull(clazz, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if the object is {@code null}
	 * @since 5.4.5
	 */
	public static <T, X extends Throwable> T notNull(final T object, final Supplier<X> errorSupplier) throws X {
		if (null == object) {
			throw errorSupplier.get();
		}
		return object;
	}

	/**
	 * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常 Assert that an object is not {@code null} .
	 * <pre class="code">
	 * Assert.notNull(clazz, "The class must not be null");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is {@code null}
	 */
	public static <T> T notNull(final T object, final String errorMsgTemplate, final Object... params) throws IllegalArgumentException {
		return notNull(object, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常
	 * <pre class="code">
	 * Assert.notNull(clazz);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is {@code null}
	 */
	public static <T> T notNull(final T object) throws IllegalArgumentException {
		return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
	}

	// ----------------------------------------------------------------------------------------------------------- Check empty

	/**
	 * 检查给定字符串是否为空，为空抛出自定义异常，并使用指定的函数获取错误信息返回。
	 * <pre class="code">
	 * Assert.notEmpty(name, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X 被检查字符串为空抛出此异常
	 * @see StringUtils#isNotEmpty(CharSequence)
	 * @since 5.4.5
	 */
	public static <T extends CharSequence, X extends Throwable> T notEmpty(final T text, final Supplier<X> errorSupplier) throws X {
		if (StringUtils.isEmpty(text)) {
			throw errorSupplier.get();
		}
		return text;
	}

	/**
	 * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
	 *
	 * <pre class="code">
	 * Assert.notEmpty(name, "Name must not be empty");
	 * </pre>
	 *
	 * @throws IllegalArgumentException 被检查字符串为空
	 * @see StringUtils#isNotEmpty(CharSequence)
	 */
	public static <T extends CharSequence> T notEmpty(final T text, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		return notEmpty(text, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
	 *
	 * <pre class="code">
	 * Assert.notEmpty(name);
	 * </pre>
	 *
	 * @throws IllegalArgumentException 被检查字符串为空
	 * @see StringUtils#isNotEmpty(CharSequence)
	 */
	public static <T extends CharSequence> T notEmpty(final T text) throws IllegalArgumentException {
		return notEmpty(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
	}

	/**
	 * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出自定义异常。
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.notBlank(name, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X 被检查字符串为空白
	 * @see StringUtils#isNotBlank(CharSequence)
	 */
	public static <T extends CharSequence, X extends Throwable> T notBlank(final T text, final Supplier<X> errorMsgSupplier) throws X {
		if (StringUtils.isBlank(text)) {
			throw errorMsgSupplier.get();
		}
		return text;
	}

	/**
	 * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
	 *
	 * <pre class="code">
	 * Assert.notBlank(name, "Name must not be blank");
	 * </pre>
	 *
	 * @throws IllegalArgumentException 被检查字符串为空白
	 * @see StringUtils#isNotBlank(CharSequence)
	 */
	public static <T extends CharSequence> T notBlank(final T text, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		return notBlank(text, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
	 *
	 * <pre class="code">
	 * Assert.notBlank(name);
	 * </pre>
	 *
	 * @throws IllegalArgumentException 被检查字符串为空白
	 * @see StringUtils#isNotBlank(CharSequence)
	 */
	public static <T extends CharSequence> T notBlank(final T text) throws IllegalArgumentException {
		return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
	}

	/**
	 * 断言给定字符串是否不被另一个字符串包含（即是否为子串），并使用指定的函数获取错误信息返回<br>
	 * 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
	 * <pre class="code">
	 * Assert.notContain(name, "rod", ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return ");
	 *  });
	 * </pre>
	 *
	 * @throws X 非子串抛出异常
	 * @see StringUtils#contains(CharSequence, CharSequence)
	 * @since 5.4.5
	 */
	public static <T extends CharSequence, X extends Throwable> T notContain(final CharSequence textToSearch, final T substring,
																			final Supplier<X> errorSupplier) throws X {
		if (StringUtils.contains(textToSearch, substring)) {
			throw errorSupplier.get();
		}
		return substring;
	}

	/**
	 * 断言给定字符串是否不被另一个字符串包含（即是否为子串）<br>
	 * 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
	 * <pre class="code">
	 * Assert.notContain(name, "rod", "Name must not contain 'rod'");
	 * </pre>
	 *
	 * @throws IllegalArgumentException 非子串抛出异常
	 */
	public static String notContain(final String textToSearch, final String subString, final String errorMsgTemplate,
									final Object... params) throws IllegalArgumentException {
		return notContain(textToSearch, subString, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言给定字符串是否不被另一个字符串包含（即是否为子串），即subString是否不是textToSearch的子串。<br>
	 * 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
	 * <pre class="code">
	 * Assert.notContain(name, "rod");
	 * </pre>
	 *
	 * @throws IllegalArgumentException 非子串抛出异常
	 */
	public static String notContain(final String textToSearch, final String subString) throws IllegalArgumentException {
		return notContain(textToSearch, subString, "[Assertion failed] - this String argument must not contain the substring [{}]",
				subString);
	}

	/**
	 * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.notEmpty(array, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if the object array is {@code null} or has no elements
	 * @since 5.4.5
	 */
	public static <T, X extends Throwable> T[] notEmpty(final T[] array, final Supplier<X> errorSupplier) throws X {
		if (ArrayUtils.isEmpty(array)) {
			throw errorSupplier.get();
		}
		return array;
	}

	/**
	 * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
	 * <pre class="code">
	 * Assert.notEmpty(array, "The array must have elements");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object array is {@code null} or has no elements
	 */
	public static <T> T[] notEmpty(final T[] array, final String errorMsgTemplate, final Object... params) throws IllegalArgumentException {
		return notEmpty(array, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
	 * <pre class="code">
	 * Assert.notEmpty(array, "The array must have elements");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object array is {@code null} or has no elements
	 */
	public static <T> T[] notEmpty(final T[] array) throws IllegalArgumentException {
		return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
	}

	/**
	 * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.noNullElements(array, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return ");
	 *  });
	 * </pre>
	 *
	 * @throws X if the object array contains a {@code null} element
	 * @since 5.4.5
	 */
	public static <T, X extends Throwable> T[] noNullElements(final T[] array, final Supplier<X> errorSupplier) throws X {
		if (ArrayUtils.contains(array, null)) {
			throw errorSupplier.get();
		}
		return array;
	}

	/**
	 * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
	 * <pre class="code">
	 * Assert.noNullElements(array, "The array must not have null elements");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object array contains a {@code null} element
	 */
	public static <T> T[] noNullElements(final T[] array, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		return noNullElements(array, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
	 * <pre class="code">
	 * Assert.noNullElements(array);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object array contains a {@code null} element
	 */
	public static <T> T[] noNullElements(final T[] array) throws IllegalArgumentException {
		return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
	}

	/**
	 * 断言给定集合非空
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.notEmpty(collection, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if the collection is {@code null} or has no elements
	 * @since 5.4.5
	 */
	public static <E, T extends Iterable<E>, X extends Throwable> T notEmpty(final T collection, final Supplier<X> errorSupplier) throws X {
		if (Objects.isNull(collection) || IteratorUtils.isEmpty((Iterator<?>) collection)) {
			throw errorSupplier.get();
		}
		return collection;
	}

	/**
	 * 断言给定集合非空
	 * <pre class="code">
	 * Assert.notEmpty(collection, "Collection must have elements");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the collection is {@code null} or has no elements
	 */
	public static <E, T extends Iterable<E>> T notEmpty(final T collection, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		return notEmpty(collection, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言给定集合非空
	 * <pre class="code">
	 * Assert.notEmpty(collection);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the collection is {@code null} or has no elements
	 */
	public static <E, T extends Iterable<E>> T notEmpty(final T collection) throws IllegalArgumentException {
		return notEmpty(collection, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
	}

	/**
	 * 断言给定Map非空
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.notEmpty(map, ()-&gt;{
	 *      // to query relation message
	 *      return new IllegalArgumentException("relation message to return");
	 *  });
	 * </pre>
	 *
	 * @throws X if the map is {@code null} or has no entries
	 * @since 5.4.5
	 */
	public static <K, V, T extends Map<K, V>, X extends Throwable> T notEmpty(final T map, final Supplier<X> errorSupplier) throws X {
		if (MapUtils.isEmpty(map)) {
			throw errorSupplier.get();
		}
		return map;
	}

	/**
	 * 断言给定Map非空
	 * <pre class="code">
	 * Assert.notEmpty(map, "Map must have entries");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the map is {@code null} or has no entries
	 */
	public static <K, V, T extends Map<K, V>> T notEmpty(final T map, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		return notEmpty(map, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言给定Map非空
	 * <pre class="code">
	 * Assert.notEmpty(map, "Map must have entries");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the map is {@code null} or has no entries
	 */
	public static <K, V, T extends Map<K, V>> T notEmpty(final T map) throws IllegalArgumentException {
		return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
	}

	/**
	 * 断言给定对象是否是给定类的实例
	 * <pre class="code">
	 * Assert.instanceOf(Foo.class, foo);
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is not an instance of clazz
	 * @see Class#isInstance(Object)
	 */
	public static <T> T isInstanceOf(final Class<?> type, final T obj) {
		return isInstanceOf(type, obj, "Object [{}] is not instanceof [{}]", obj, type);
	}

	/**
	 * 断言给定对象是否是给定类的实例
	 * <pre class="code">
	 * Assert.instanceOf(Foo.class, foo, "foo must be an instance of class Foo");
	 * </pre>
	 *
	 * @throws IllegalArgumentException if the object is not an instance of clazz
	 * @see Class#isInstance(Object)
	 */
	public static <T> T isInstanceOf(final Class<?> type, final T obj, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
		}
		return obj;
	}

	/**
	 * 断言 {@code superType.isAssignableFrom(subType)} 是否为 {@code true}.
	 * <pre class="code">
	 * Assert.isAssignable(Number.class, myClass);
	 * </pre>
	 *
	 * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
	 */
	public static void isAssignable(final Class<?> superType, final Class<?> subType) throws IllegalArgumentException {
		isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
	}

	/**
	 * 断言 {@code superType.isAssignableFrom(subType)} 是否为 {@code true}.
	 * <pre class="code">
	 * Assert.isAssignable(Number.class, myClass, "myClass must can be assignable to class Number");
	 * </pre>
	 *
	 * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
	 */
	public static void isAssignable(final Class<?> superType, final Class<?> subType, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		notNull(superType, "Type to check against must not be null");
		if (subType == null || !superType.isAssignableFrom(subType)) {
			throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
		}
	}

	/**
	 * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。
	 * 并使用指定的函数获取错误信息返回
	 * <pre class="code">
	 * Assert.state(id == null, ()-&gt;{
	 *      // to query relation message
	 *      return "relation message to return ";
	 *  });
	 * </pre>
	 *
	 * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
	 */
	public static void state(final boolean expression, final Supplier<String> errorMsgSupplier) throws IllegalStateException {
		if (!expression) {
			throw new IllegalStateException(errorMsgSupplier.get());
		}
	}

	/**
	 * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。
	 * <pre class="code">
	 * Assert.state(id == null, "The id property must not already be initialized");
	 * </pre>
	 *
	 * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
	 */
	public static void state(final boolean expression, final String errorMsgTemplate, final Object... params) throws IllegalStateException {
		if (!expression) {
			throw new IllegalStateException(StringUtils.format(errorMsgTemplate, params));
		}
	}

	/**
	 * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。
	 * <pre class="code">
	 * Assert.state(id == null);
	 * </pre>
	 *
	 * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
	 */
	public static void state(final boolean expression) throws IllegalStateException {
		state(expression, "[Assertion failed] - this state invariant must be true");
	}

	/**
	 * 检查下标（数组、集合、字符串）是否符合要求，下标必须满足：
	 *
	 * <pre>
	 * 0 &le; index &lt; size
	 * </pre>
	 *
	 * @throws IllegalArgumentException 如果size &lt; 0 抛出此异常
	 * @throws IndexOutOfBoundsException 如果index &lt; 0或者 index &ge; size 抛出此异常
	 * @since 4.1.9
	 */
	public static int checkIndex(final int index, final int size) throws IllegalArgumentException, IndexOutOfBoundsException {
		return checkIndex(index, size, "[Assertion failed]");
	}

	/**
	 * 检查下标（数组、集合、字符串）是否符合要求，下标必须满足：
	 *
	 * <pre>
	 * 0 &le; index &lt; size
	 * </pre>
	 *
	 * @throws IllegalArgumentException 如果size &lt; 0 抛出此异常
	 * @throws IndexOutOfBoundsException 如果index &lt; 0或者 index &ge; size 抛出此异常
	 * @since 4.1.9
	 */
	public static int checkIndex(final int index, final int size, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException, IndexOutOfBoundsException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(badIndexMsg(index, size, errorMsgTemplate, params));
		}
		return index;
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @throws X if value is out of bound
	 * @since 5.7.15
	 */
	public static <X extends Throwable> int checkBetween(final int value, final int min, final int max,
														final Supplier<? extends X> errorSupplier) throws X {
		if (value < min || value > max) {
			throw errorSupplier.get();
		}

		return value;
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 5.7.15
	 */
	public static int checkBetween(final int value, final int min, final int max, final String errorMsgTemplate, final Object... params) {
		return checkBetween(value, min, max, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 4.1.10
	 */
	public static int checkBetween(final int value, final int min, final int max) {
		return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @throws X if value is out of bound
	 * @since 5.7.15
	 */
	public static <X extends Throwable> long checkBetween(final long value, final long min, final long max,
														final Supplier<? extends X> errorSupplier) throws X {
		if (value < min || value > max) {
			throw errorSupplier.get();
		}

		return value;
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 5.7.15
	 */
	public static long checkBetween(final long value, final long min, final long max, final String errorMsgTemplate,
									final Object... params) {
		return checkBetween(value, min, max, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 4.1.10
	 */
	public static long checkBetween(final long value, final long min, final long max) {
		return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @throws X if value is out of bound
	 * @since 5.7.15
	 */
	public static <X extends Throwable> double checkBetween(final double value, final double min, final double max,
															final Supplier<? extends X> errorSupplier) throws X {
		if (value < min || value > max) {
			throw errorSupplier.get();
		}

		return value;
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 5.7.15
	 */
	public static double checkBetween(final double value, final double min, final double max, final String errorMsgTemplate,
									final Object... params) {
		return checkBetween(value, min, max, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 4.1.10
	 */
	public static double checkBetween(final double value, final double min, final double max) {
		return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
	}

	/**
	 * 检查值是否在指定范围内
	 *
	 * @since 4.1.10
	 */
	public static Number checkBetween(final Number value, final Number min, final Number max) {
		notNull(value);
		notNull(min);
		notNull(max);
		final double valueDouble = value.doubleValue();
		final double minDouble = min.doubleValue();
		final double maxDouble = max.doubleValue();
		if (valueDouble < minDouble || valueDouble > maxDouble) {
			throw new IllegalArgumentException(StringUtils.format(TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max));
		}
		return value;
	}

	/**
	 * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
	 * <pre class="code">
	 *   Assert.notEquals(obj1,obj2);
	 * </pre>
	 *
	 * @throws IllegalArgumentException obj1 must be not equals obj2
	 */
	public static void notEquals(final Object obj1, final Object obj2) {
		notEquals(obj1, obj2, "({}) must be not equals ({})", obj1, obj2);
	}

	/**
	 * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
	 * <pre class="code">
	 *   Assert.notEquals(obj1,obj2,"obj1 must be not equals obj2");
	 * </pre>
	 *
	 * @throws IllegalArgumentException obj1 must be not equals obj2
	 */
	public static void notEquals(final Object obj1, final Object obj2, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		notEquals(obj1, obj2, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言两个对象是否不相等,如果两个对象相等,抛出指定类型异常,并使用指定的函数获取错误信息返回
	 *
	 * @throws X obj1 must be not equals obj2
	 */
	public static <X extends Throwable> void notEquals(final Object obj1, final Object obj2, final Supplier<X> errorSupplier) throws X {
		if (Objects.equals(obj1, obj2)) {
			throw errorSupplier.get();
		}
	}
	// ----------------------------------------------------------------------------------------------------------- Check not equals

	/**
	 * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
	 * <pre class="code">
	 *   Assert.isEquals(obj1,obj2);
	 * </pre>
	 *
	 * @throws IllegalArgumentException obj1 must be equals obj2
	 */
	public static void equals(final Object obj1, final Object obj2) {
		equals(obj1, obj2, "({}) must be equals ({})", obj1, obj2);
	}

	/**
	 * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
	 * <pre class="code">
	 *   Assert.isEquals(obj1,obj2,"obj1 must be equals obj2");
	 * </pre>
	 *
	 * @throws IllegalArgumentException obj1 must be equals obj2
	 */
	public static void equals(final Object obj1, final Object obj2, final String errorMsgTemplate, final Object... params)
			throws IllegalArgumentException {
		equals(obj1, obj2, () -> new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params)));
	}

	/**
	 * 断言两个对象是否相等,如果两个对象不相等,抛出指定类型异常,并使用指定的函数获取错误信息返回
	 *
	 * @throws X obj1 must be equals obj2
	 */
	public static <X extends Throwable> void equals(final Object obj1, final Object obj2, final Supplier<X> errorSupplier) throws X {
		if (!Objects.equals(obj1, obj2)) {
			throw errorSupplier.get();
		}
	}

	// ----------------------------------------------------------------------------------------------------------- Check is equals

	// -------------------------------------------------------------------------------------------------------------------------------------------- Private method start

	/**
	 * 错误的下标时显示的消息
	 */
	private static String badIndexMsg(final int index, final int size, final String desc, final Object... params) {
		if (index < 0) {
			return StringUtils.format("{} ({}) must not be negative", StringUtils.format(desc, params), index);
		} else if (size < 0) {
			throw new IllegalArgumentException("negative size: " + size);
		} else { // index >= size
			return StringUtils.format("{} ({}) must be less than size ({})", StringUtils.format(desc, params), index, size);
		}
	}

}
