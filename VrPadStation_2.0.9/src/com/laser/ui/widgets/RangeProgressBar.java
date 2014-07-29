package com.laser.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RangeProgressBar extends View {

	private Paint outlinePaint;
	private Path outlinePath = new Path();
	private Paint rangePaint;
	private Path rangePath = new Path();
	
	private int height, width;	
	private float percent = 0.5f;
	private float fheight, fwidth;
	
	private float min = 0.5f;
	private int minVal;
	private float max = 0.5f;
	private int maxVal;

	private boolean reverse = false;
	private boolean showRange = false;	

	public RangeProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {		
		outlinePaint = new Paint();
		outlinePaint.setAntiAlias(false);
		outlinePaint.setStyle(Style.STROKE);
		outlinePaint.setStrokeWidth(5);

		rangePaint = new Paint(outlinePaint);
		rangePaint.setStyle(Style.FILL);		
	}

	public float getPercent() {
		return percent;
	}

	private void setPercent(float percent) {
		this.percent = percent;
		min = min > percent ? percent : min;
		max = max < percent ? percent : max;
		invalidate();
	}

	public void setBounds(int max, int min) {
		maxVal = max;
		minVal = min;
	}

	public void setProgress(int value) {
		this.setPercent((value - minVal) / ((float) (maxVal - minVal)));
	}

	public float getMin() {
		return min;
	}
	
	public int getMinValue() {
		return minVal + (int) (getMin() * ((float) (maxVal - minVal)));
	}

	public float getMax() {
		return max;
	}
	
	public int getMaxValue() {
		return minVal + (int) (getMax() * ((float) (maxVal - minVal)));
	}	

	public void showRange(boolean showRange) {
		this.showRange = showRange;
		if (showRange) {
			min = 0.5f;
			max = 0.5f;
		}
		invalidate();
	}
	
	public void reverseBar(boolean inv) {
		reverse = inv;
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w - 1;
		height = h - 1;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		fheight = height < width ? height : (height * (1 - percent));
		fwidth = height < width ? (width * (percent)) : width;

		rangePaint.setColor(Color.BLACK);
		
		outlinePath.reset();
		outlinePath.moveTo(0, 0);
		outlinePath.lineTo(0, height);
		outlinePath.lineTo(width, height);
		outlinePath.lineTo(width, 0);
		outlinePath.lineTo(0, 0);
		
		canvas.drawPath(outlinePath, rangePaint);

		rangePaint.setColor(Color.BLUE);
		rangePath.reset();

		if (reverse) {
			if (height > width) {
				rangePath.moveTo(0, 0);
				rangePath.lineTo(0, height - fheight);
				rangePath.lineTo(width, height - fheight);
				rangePath.lineTo(width, 0);
				rangePath.lineTo(0, 0);
			} else {
				rangePath.moveTo(0, 0);
				rangePath.lineTo(0, height);
				rangePath.lineTo(width - fwidth, height);
				rangePath.lineTo(width - fwidth, 0);
				rangePath.lineTo(0, 0);
			}
		} else {
			if (height > width) {
				rangePath.moveTo(0, fheight);
				rangePath.lineTo(0, height);
				rangePath.lineTo(fwidth, height);
				rangePath.lineTo(fwidth, fheight);
				rangePath.lineTo(0, fheight);
			} else {
				rangePath.moveTo(0, 0);
				rangePath.lineTo(0, height);
				rangePath.lineTo(fwidth, height);
				rangePath.lineTo(fwidth, 0);
				rangePath.lineTo(0, 0);
			}
		}
		canvas.drawPath(rangePath, rangePaint);

		outlinePaint.setColor(Color.RED);

		if (showRange) {
			float f;
			// int _t, _l, _w, _h;
			outlinePath.reset();

			if (reverse) {
				if (height > width) {
					//
					f = height * (min);
					outlinePath.reset();
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, outlinePaint);

					outlinePath.reset();
					f = height * (max);
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, outlinePaint);

				} else {

					f = width * max;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, outlinePaint);

					f = width * min;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, outlinePaint);
				}
			} else {
				if (height > width) {
					f = height * (1 - min);

					outlinePath.reset();
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, outlinePaint);

					outlinePath.reset();
					f = height * (1 - max);
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, outlinePaint);

				} else {

					f = width * min;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, outlinePaint);

					f = width * max;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, outlinePaint);
				}
			}
		}
	}
}
