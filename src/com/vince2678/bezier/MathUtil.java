package com.vince2678.bezier;

import java.math.BigInteger;
import java.util.HashMap;

public class MathUtil
{
	private static HashMap<Integer, BigInteger> factorials;
	private static HashMap<Pair<Integer, Integer>, Integer> coefficients;
	private static HashMap<Pair<Integer, Integer>, Integer> powers;

	static
	{
		factorials = new FixedSizeHashMap<>(200);
		coefficients = new FixedSizeHashMap<>(200);
		powers = new FixedSizeHashMap<>(200);
	}

	/**
	 * Raise a to the power b
	 * <p>
	 * The method will return 0 for negative exponents.
	 * Use {@link Math#pow} instead for negative exponents.
	 * @param a the base
	 * @param b the exponent, in interval [0, infinity)
	 * @return the result, a<sup>b</sup>
	 */
	public static int pow(int a, int b)
	{
		if (b < 0)
		{
			return 0;
		}
		else if (b == 0)
		{
			return 1;
		}
		else if (b == 1)
		{
			return a;
		}

		Pair<Integer, Integer> key = new Pair<>(a, b);
		if (powers.containsKey(key))
		{
			return powers.get(key);
		}

		int power = pow(a, b - 1) * a;
		powers.put(key, power);

		return power;
	}

	/**
	 * Get the factorial of n
	 * @param n integer in interval [0, infinity)
	 * @return the factorial, n!
	 */
	public static BigInteger factorial(int n)
	{
		if (n < 0)
		{
			return new BigInteger("0");
		}
		else if (n <= 1)
		{
			return new BigInteger("1");
		}
		else if (n == 2)
		{
			return new BigInteger("2");
		}
		else if (factorials.containsKey(n))
		{
			return factorials.get(n);
		}

		BigInteger fac = factorial(n - 1).multiply(new BigInteger(Integer.toString(n)));
		factorials.put(n, fac);

		return fac;
	}

	/**
	 * Get the binomial coefficient for indices n, k, n >= k.
	 * @param n integer in interval [0, infinity)
	 * @param k integer in interval [0, infinity)
	 * @return the coefficient.
	 */
	public static int nCk(int n, int k)
	{
		if (n == k || k == 0)
		{
			return 1;
		}

		Pair<Integer, Integer> key = new Pair<>(n, k);
		if (coefficients.containsKey(key))
		{
			return coefficients.get(key);
		}

		int coefficient = nCk(n - 1, k) + nCk(n - 1, k - 1);
		coefficients.put(key, coefficient);

		return coefficient;
	}

	/**
	 * Get the binomial coefficient for indices n, k, n >= k.
	 * @param n integer in interval [0, infinity)
	 * @param k integer in interval [0, infinity)
	 * @return the coefficient.
	 */
	public static int choose(int n, int k)
	{
		return nCk(n, k);
	}
}
