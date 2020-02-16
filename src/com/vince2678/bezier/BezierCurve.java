package com.vince2678.bezier;

import java.awt.Point;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;

@Getter
public class BezierCurve
{
	public static final BigDecimal MAXIMUM_TIMESTEP = new BigDecimal("0.32");
	public static final int LENGTH_DIVISOR_INCREMENT = 50;
	public static final MathContext DECIMAL_CONTEXT = new MathContext(1, RoundingMode.HALF_EVEN);

	private List<Point> controlPoints;
	private Rectangle bounds;

	public BezierCurve(Point p1, Point p2, int controls)
	{
		this(p1, p2, getBoundingRectangle(p1, p2), controls);
	}

	private BezierCurve(Point p1, Point p2, Rectangle bounds, int controls)
	{
		this.bounds = bounds;

		controlPoints = new ArrayList<>();
		controlPoints.add(p1);

		/*
		*  default to straight line if there's
		*  no area to unique control points from.
		*/
		int area = bounds.width * bounds.height;
		if (area < controls + distance(p1, p2))
		{
			controlPoints.add(p2);
			return;
		}

		int i = 0;
		while (i < controls)
		{
			Point point = getControl(bounds);
			if (!(controlPoints.contains(point) || point.equals(p2)))
			{
				controlPoints.add(point);
				i++;
			}
		}

		controlPoints.add(p2);
	}

	/**
	 * Get the point on the curve at the time t.
	 * <p>
	 * <br />
	 *  The equation used to get the point is:
	 * <br />
	 *  B(t) = (1-t)<sup>2</sup> * P<sub>1</sub>
	 *  + 2t(1-t)P<sub>control</sub> + t<sup>2</sup> * P<sub>2</sub>
	 * @param t the time, in interval [0.0, 1.0]
	 * @return the point on the curve
	 */
	public Point solve(double t)
	{
		double x = 0;
		double y = 0;

		int len = controlPoints.size();
		int n = len - 1;
		for (int k = 0; k < len; k++)
		{
			Point p = controlPoints.get(k);

			int fac = MathUtil.choose(n, k);
			double tK = Math.pow(t, k);
			double tCNK = Math.pow((1.0D - t), n - k);

			double intermediate = fac * tK * tCNK;

			x += intermediate * p.getX();
			y += intermediate * p.getY();
		}

		return new Point((int) Math.ceil(x), (int) Math.ceil(y));
	}

	/**
	 * Get a time step based on the length of the line.
	 * <br />
	 * <p>
	 * The number of times the length can be divided by
	 * {@link #LENGTH_DIVISOR_INCREMENT} determines how small the
	 * time step will be.
	 * <br />
	 * The greater the length, the smaller the time step, and the
	 * finer the resulting curve.
	 *
	 * @param p1 an endpoint of the line
	 * @param p2 an endpoint of the line
	 * @return return a time step between 0 and {@link #MAXIMUM_TIMESTEP}
	 */
	private static BigDecimal getTimeStep(Point p1, Point p2)
	{
		/* scale increment to line length, down to a minimum of 0.01 */
		int length = (int) Math.ceil(distance(p1, p2));

		int result = Integer.MAX_VALUE;
		int divisor = 0;
		int count = 0;

		while (result > 0)
		{
			divisor += LENGTH_DIVISOR_INCREMENT;
			count += 1;
			result = length / divisor;
		}

		return MAXIMUM_TIMESTEP.divide(new BigDecimal(count), DECIMAL_CONTEXT);
	}

	/**
	 * Get a path covering the curve.
	 * <p>
	 * Use this instead of the iterator to get
	 * the points covering the curve.
	 *
	 * @return the path
	 */
	public List<Point> getPath()
	{
		int len = controlPoints.size();
		Point p1 = controlPoints.get(0).getLocation();
		Point p2 = controlPoints.get(len - 1).getLocation();
		Point prev = p1;

		BigDecimal increment = getTimeStep(p1, p2);

		List<Point> path = new ArrayList<>();
		path.add(prev);

		BigDecimal t = new BigDecimal(increment.toString());

		while(t.compareTo(BigDecimal.ONE) < 0)
		{
			Point p = solve(t.doubleValue());
			if (!p.equals(prev))
			{
				prev = p;
				path.add(p);
			}
			t = t.add(increment);
		}

		if (!prev.equals(p2))
		{
			path.add(p2.getLocation());
		}

		return path;
	}

	/**
	 * Return a number between lower (inclusive) and upper (exclusive)
	 * @param lower the lower bound
	 * @param upper the upper bound
	 * @return the next pseudorandom number
	 */
	public static int randomInt(int lower, int upper)
	{
		Random random = ThreadLocalRandom.current();
		int difference = upper - lower;

		return random.nextInt(difference) + lower;
	}

	/**
	 * Get a rectangle where the line with endpoints p1 and p2
	 * form the diagonal (if gradient is non-zero/defined).
	 * <p>
	 * If the gradient of the line is undefined, the line length
	 * becomes the height of the rectangle, and width is random
	 * and in interval [height/2, height)
	 * <p>
	 * If the gradient of the line is zero, the line length
	 * becomes the width of the rectangle, and height is random
	 * and in interval [width/2, width)
	 *
	 * @param p1 the first endpoint of the line
	 * @param p2 the second endpoint of the line
	 * @return a bounding rectangle containing all the points in
	 *  the line.
	 */
	public static Rectangle getBoundingRectangle(Point p1, Point p2)
	{
		int dX = p1.x - p2.x;
		int dY = p1.y - p2.y;

		int midX = Math.floorDiv(p1.x + p2.x, 2);
		int midY = Math.floorDiv(p1.y + p2.y, 2);

		Point mid = new Point(midX, midY);

		/* upper left wrt screen coordinate quadrant */
		Point upperLeft;

		int width;
		int height;

		if (dY == 0)
		{
			// gradient == 0
			width = Math.abs(dX);
			height = randomInt(width / 2, width);

			Point left = p2;
			if (p1.x < p2.x)
			{
				left = p1;
			}

			upperLeft = new Point(left.x, left.y - Math.floorDiv(height, 2));
		}
		else if (dX == 0)
		{
			// gradient undefined
			height = Math.abs(dY);
			width = randomInt(height / 2, height);

			Point top = p1;
			if (p1.y > p2.y)
			{
				top = p2;
			}

			upperLeft = new Point(top.x - Math.floorDiv(width, 2), top.y);
		}
		else
		{
			width = Math.abs(dX);
			height = Math.abs(dY);

			Point top = p1;
			if (p1.y > p2.y)
			{
				top = p2;
			}

			int m = Math.floorDiv(dY, dX);
			if (m < 0)
			{
				upperLeft = new Point(top.x - width, top.y);
			}
			else
			{
				upperLeft = top;
			}
		}

		return new Rectangle(upperLeft.x, upperLeft.y, width, height);
	}

	/**
	 * Get control point within bounds
	 * <p>
	 * The generated control point will always be within
	 * the bounds specified.
	 * <p>
	 * @param bounds the bounds the point should lie in
	 * @return a control point for the curve
	 */
	public static Point getControl(Rectangle bounds)
	{
		return PointUtil.getRandomBoundPoint(bounds);
	}

	public static double distance(Point p1, Point p2)
	{
		return p1.distance(p2);
	}
}
