package com.vince2678.bezier;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

public class PointUtil
{
	@RequiredArgsConstructor
	public enum Rotation
	{
		ANGLE_90(1, 0),
		ANGLE_180(0, -1),
		ANGLE_270(-1, 0),
		;
		final int sine;
		final int cosine;
	}

	/** Return a random point lying in a rectangular region
	 *
	 * @param bounds Rectangle defining coordinates and area of region
	 * @return Point contained in Rectangle
	 */
	public static Point getRandomBoundPoint(Rectangle bounds)
	{
		return getRandomBoundPoint(bounds, new Dimension(0 , 0));
	}

	/** Return a random point lying in a rectangular region
	 * <p>
	 *  If invalid values of widthPc and/or heightPc are passed (< 0.0 or >1.0),
	 *  no points will be excluded.
	 *
	 * @param bounds Rectangle defining coordinates and area of region
	 * @param widthPc Percent about either end of x-axis to exclude from consideration
	 * @param heightPc Percent about either end of y-axis to exclude from consideration
	 * @return Point contained in Rectangle
	 */
	public static Point getRandomBoundPoint(Rectangle bounds, double widthPc, double heightPc)
	{
		if (widthPc < 0F || widthPc > 1F)
		{
			widthPc = 0F;
		}

		if (heightPc < 0F || heightPc > 1F)
		{
			heightPc = 0F;
		}

		int borderWidth = (int) Math.floor(bounds.width * widthPc);
		int borderHeight = (int) Math.floor(bounds.height * heightPc);
		return getRandomBoundPoint(bounds, new Dimension(borderWidth, borderHeight));
	}

	/** Return a random point lying in a rectangular region
	 * <p>
	 *  If invalid values are set for width and height in the Dimension,
	 *  no points will be excluded.
	 *
	 * @param bounds Rectangle defining coordinates and area of region
	 * @param exclusionBorder Border about axes to exclude from consideration
	 * @return Point contained in Rectangle
	 */
	public static Point getRandomBoundPoint(Rectangle bounds, Dimension exclusionBorder)
	{
		int x = bounds.x;
		int y = bounds.y;

		int dX;
		int dY;

		int origin;
		int bound;

		origin = exclusionBorder.width;
		bound = bounds.width - exclusionBorder.width;
		if (origin >= bound)
		{
			dX = 0;
		}
		else
		{
			dX = ThreadLocalRandom.current().nextInt(origin, bound);
			if (dX < 0 || dX > bounds.width)
			{
				dX = 0;
			}
		}

		origin = exclusionBorder.height;
		bound = bounds.height - exclusionBorder.height;
		if (origin >= bound)
		{
			dY = 0;
		}
		else
		{
			dY = ThreadLocalRandom.current().nextInt(origin, bound);
			if (dY < 0 || dY > bounds.height)
			{
				dY = 0;
			}
		}

		return new Point(x + dX, y + dY);
	}

	/**
	 * Convert a path relative to reference to an absolute path.
	 * @param path the relative path
	 * @param reference the reference (origin) point
	 * @return an absolute path
	 */
	public static List<Point> relativeToAbsolute(List<Point> path, Point reference)
	{
		return path.stream()
			.map(p -> sum(p, reference))
			.collect(Collectors.toList());
	}

	/**
	 * Get the length of a path.
	 * @param path the path to check
	 * @return the length of path
	 */
	public static double pathLength(List<Point> path)
	{
		double length = 0D;
		int len = path.size();
		if (len < 2)
		{
			return length;
		}

		Point prev = path.get(0);
		Point curr = null;

		for (int i = 1; i < len; i++)
		{
			curr = path.get(i);
			length += prev.distance(curr);

			prev = curr;
		}

		return length;
	}

	/**
	 * Rotate the path by the given angle
	 * @param path the path to rotate
	 * @param angle the angle of rotation
	 * @return the rotated path
	 */
	public static List<Point> rotatePath(List<Point> path, Rotation angle)
	{
		List<Point> rotated = new ArrayList<>();

		for (Point p: path)
		{
			int x = (p.x * angle.cosine) - (p.y * angle.sine);
			int y = (p.x * angle.sine) + (p.y * angle.cosine);

			rotated.add(new Point(x, y));
		}

		return rotated;
	}

	/**
	 * Calculate the sum of two Points
	 * @param p1 the first point
	 * @param p2 the second point
	 * @return the sum of the two points
	 */
	public static Point sum(Point p1, Point p2)
	{
		return new Point(p1.x + p2.x, p1.y + p2.y);
	}

	/**
	 * Calculate the difference of two Points
	 * @param p1 the minuend point
	 * @param p2 the subtrahend point
	 * @return the difference of the two points
	 */
	public static Point difference(Point p1, Point p2)
	{
		return new Point(p1.x - p2.x, p1.y - p2.y);
	}

	/**
	 * Interpolate path using bresenham algorithm
	 * @param path path to interpolate
	 * @return the interpolated path
	 */
	public static List<Point> interpolatePath(List<Point> path)
	{
		int len = path.size();
		if (len < 2)
		{
			return path;
		}

		List<Point> extendedPath = new ArrayList<>();

		int i = 0;
		Point prev = path.get(i).getLocation();

		i += 1;
		while (i < len)
		{
			Point curr = path.get(i).getLocation();
			extendedPath.addAll(bresenhamPath(prev, curr));
			extendedPath.remove(extendedPath.size() - 1);

			prev = curr;
			i += 1;
		}
		extendedPath.add(prev);

		return extendedPath;
	}

	public static List<Point> bresenhamPath(Point p1, Point p2)
	{
		return bresenhamPath(p1.x, p1.y, p2.x, p2.y);
	}

	public static List<Point> bresenhamPath(int x1, int y1, int x2, int y2)
	{
		List<Point> path = new ArrayList<>();

		int w = x2 - x1;
		int h = y2 - y1;

		int dx1 = 0;
		int dy1 = 0;
		int dx2 = 0;
		int dy2 = 0;

		if (w < 0)
		{
			dx1 = -1;
		}
		else if (w > 0)
		{
			dx1 = 1;
		}

		if (h < 0)
		{
			dy1 = -1;
		}
		else if (h > 0)
		{
			dy1 = 1;
		}

		if (w < 0)
		{
			dx2 = -1 ;
		}
		else if (w > 0)
		{
			dx2 = 1 ;
		}

		int longest = Math.abs(w);
		int shortest = Math.abs(h);

		if (longest <= shortest)
		{
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h < 0)
			{
				dy2 = -1;
			}
			else if (h > 0)
			{
				dy2 = 1;
			}
			dx2 = 0;
		}

		int numerator = longest >> 1;
		for (int i = 0; i <= longest; i++)
		{
			path.add(new Point(x1, y1));
			numerator += shortest;
			if (numerator >= longest)
			{
				numerator -= longest;
				x1 += dx1;
				y1 += dy1;
			}
			else
			{
				x1 += dx2;
				y1 += dy2;
			}
		}
		return path;
	}
}
