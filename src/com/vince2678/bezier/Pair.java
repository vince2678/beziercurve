package com.vince2678.bezier;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Pair<T, V>
{
	public final T first;
	public final V second;
}
