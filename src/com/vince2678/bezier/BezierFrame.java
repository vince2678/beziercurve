package com.vince2678.bezier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

public class BezierFrame extends JFrame
{
	BezierCurve curve;
	MouseAdapter mouseAdapter;

	Point p1 = null;
	Point p2 = null;
	Point control = null;
	int colorIndex = 0;

	int controls = 1;

	private final Color[] colors = {
		Color.BLACK,
		//Color.CYAN,
		//Color.DARK_GRAY,
		Color.BLUE,
		//Color.ORANGE,
		Color.RED
	};

	public BezierFrame()
	{
		super();
		curve = null;

		mouseAdapter = new MouseAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				setTitle("Moved to (" + e.getX() + ", " + e.getY() + ")");
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
			{
				controls += -1 * mouseWheelEvent.getWheelRotation();
				if (controls < 0)
				{
					controls = 0;
				}
				System.out.println(String.format("Control points: %d", controls));
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1) //left
				{
					if (p1 == null)
					{
						p1 = e.getPoint();
					}
					else if (p2 == null)
					{
						p2 = e.getPoint();
					}
					else if (control == null)
					{
						curve = new BezierCurve(p1, p2, controls);
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3) //right
				{
					if (p1 != null && p2 != null)
					{
						curve = new BezierCurve(p1, p2, controls);
					}
					else
					{
						p1 = null;
						p2 = null;
						control = null;
						curve = null;
					}
				}
				else
				{
					p1 = null;
					p2 = null;
					control = null;
					curve = null;
				}
				repaint();
			}

		};

		this.addMouseMotionListener(mouseAdapter);
		this.addMouseListener(mouseAdapter);
		this.addMouseWheelListener(mouseAdapter);
	}

	@Override
	public void paint(Graphics graphics)
	{
		super.paint(graphics);
		drawCurve(graphics);
	}

	private void drawCurve(Graphics graphics)
	{
		Dimension size = new Dimension(4, 4);

		if (p1 != null)
		{
			graphics.setColor(colors[colorIndex]);
			graphics.fillOval(p1.x, p1.y, size.width, size.height);
			colorIndex = (colorIndex + 1) % colors.length;
		}
		if (p2 != null)
		{
			graphics.setColor(colors[colorIndex]);
			graphics.fillOval(p2.x, p2.y, size.width, size.height);
			colorIndex = (colorIndex + 1) % colors.length;
		}
		if (control != null)
		{
			graphics.setColor(colors[colorIndex]);
			graphics.fillOval(control.x, control.y, size.width, size.height);
			colorIndex = (colorIndex + 1) % colors.length;
		}

		if (curve != null)
		{
			long time = System.nanoTime();
			List<Point> curvePath = curve.getPath();
			time = System.nanoTime() - time;
			System.out.println(String.format("Generated curve in: %dus, %dms", time/1000, time/1000000));

			if (controls > 0)
			{
				graphics.setColor(new Color(145, 85, 156));
				for (int i = 1; i < controls + 1; i++)
				{
					Point control = curve.getControlPoints().get(i);
					graphics.fillOval(control.x, control.y, size.width, size.height);
				}
			}

			graphics.setColor(Color.BLACK);
			((Graphics2D) graphics).draw(curve.getBounds());


			graphics.setColor(colors[colorIndex]);
			for (Point point: curvePath)
			{
				graphics.fillOval(point.x, point.y, size.width, size.height);
			}
			colorIndex = (colorIndex + 1) % colors.length;
		}
	}
}
