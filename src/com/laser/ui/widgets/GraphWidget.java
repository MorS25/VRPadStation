package com.laser.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GraphWidget extends SurfaceView implements SurfaceHolder.Callback{
	
	private CustomSurfaceGraphThread thread;
	private int w = 1;
	private int h = 1;
	private int widthCenter = 1;
	private int cursorPos = -1;
	private float[] defVertices = {  -1.0f, -1.0f,
									  -0.9f, -0.9f,
									  -0.8f, -0.8f,
									  -0.7f, -0.7f,
									  -0.6f, -0.6f,
									  -0.5f, -0.5f,
									  -0.4f, -0.4f,
									  -0.3f, -0.3f,
									  -0.2f, -0.2f,
									  -0.1f, -0.1f,
									  0.0f, 0.0f,
									  0.1f, 0.1f,
									  0.2f, 0.2f,
									  0.3f, 0.3f,
									  0.4f, 0.4f,
									  0.5f, 0.5f,
									  0.6f, 0.6f,
									  0.7f, 0.7f,
									  0.8f, 0.8f,
									  0.9f, 0.9f,
									  1.0f, 1.0f ,
									  -0.95f, -0.95f,
									  -0.85f, -0.85f,
									  -0.75f, -0.75f,
									  -0.65f, -0.65f,
									  -0.55f, -0.55f,
									  -0.45f, -0.45f,
									  -0.35f, -0.35f,
									  -0.25f, -0.25f,
									  -0.15f, -0.15f,
									  -0.05f, -0.05f,
									  0.05f, 0.05f,
									  0.15f, 0.15f,
									  0.25f, 0.25f,
									  0.35f, 0.35f,
									  0.45f, 0.45f,
									  0.55f, 0.55f,
									  0.65f, 0.65f,
									  0.75f, 0.75f,
									  0.85f, 0.85f,
									  0.95f, 0.95f  };
	private float[] vertices = defVertices.clone();
	
	private Paint graph = new Paint();
	private Paint axis = new Paint();
    /*private float normalize(float coord, int max)
	    {
	    return (float) (((coord - 0) / (max - 0) - 0.5 ) * 2);
    }*/
    private float denormalize(float coordNorm, int max)
    {
    	return (float) ((coordNorm/2+0.5) * (max-0) + 0);
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		//	super.onDraw(canvas);
		
		if (canvas != null)
		{	
			canvas.save();
			//canvas.drawRGB(0, 0, 0);	
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			
		    h = getHeight();
		    w = getWidth();
		    widthCenter = w / 2;
		    w = h; //devo avere un quadrato
			vertices = defVertices.clone();
			calculateVerticesToPixels();

			axis.setColor(Color.GRAY);

			if (cursorPos < 0)
				canvas.drawLine(widthCenter, 0, widthCenter, h, axis);
			else
				canvas.drawLine(cursorPos + (widthCenter - w/2), 0, cursorPos + (widthCenter - w/2), h, axis);
						
			axis.setColor(Color.WHITE);
//			canvas.drawLine(0, h/2, w, h/2, axis);
//			canvas.drawLine(w/2, 0, w/2, h, axis);
			canvas.drawLine(widthCenter-w/2, h/2, widthCenter+w/2, h/2, axis);
			canvas.drawLine(widthCenter, 0, widthCenter, h, axis);

			graph.setStrokeWidth(2);
			graph.setColor(Color.GREEN);
			canvas.drawPoints(vertices, graph);
					
        	canvas.restore();
		}
	}
	

	private static float MAXEXP = 4.0f;
	private float negDR = 1;
	private float posDR = 1;
	private float negEXP = 0;
	private float posEXP = 0;	
	private float fnegEXP = 0;
	private float fposEXP = 0;
	
	private void setRates(float negDR, float posDR, float negEXP, float posEXP)
	{
		this.negDR = negDR / 100.0f;
		this.posDR = posDR / 100.0f;
		this.negEXP = negEXP;
		fnegEXP = this.negEXP / 100.0f;
		this.posEXP = posEXP;
		fposEXP = this.posEXP / 100.0f;
	}
	
	private void calcExp()
	{		
		if (posEXP <= 0)
		{
			fposEXP = (-posEXP)/100 * (MAXEXP-1);
		}
		else
		{
			fposEXP = (1/MAXEXP - 1) * (posEXP / 100);
		}
		if (negEXP <= 0)
		{
			fnegEXP = (-negEXP)/100 * (MAXEXP-1);
		}
		else
		{
			fnegEXP = (1/MAXEXP - 1) * (negEXP / 100);
		}		
	}
	
	private void calculateVerticesToPixels()
	{
		for (int i = 0; i < vertices.length; i++)
		{
			float fval = 0;
			if (vertices[i] == 0)
			{
				fval = 0;
			}
			else if (vertices[i] > 0)
			{
				fval = vertices[i] * (this.posDR * (float)(Math.pow(Math.abs(vertices[i]),fposEXP )));
			}
			else if (vertices[i] < 0)
			{
				fval = vertices[i] * (this.negDR * (float)(Math.pow(Math.abs(vertices[i]),fnegEXP )));
			}
			
			if (i%2 == 0) 	// x
			{
				vertices[i] = denormalize(vertices[i], w) + (widthCenter - w/2);
			}
			else			// y
			{
				vertices[i] = denormalize(-fval, h);
			}			
		}
	}
	
	public GraphWidget(Context context) {
		super(context);
		init(context, negDR, posDR, negEXP, posEXP);
	}
	
	public GraphWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, negDR, posDR, negEXP, posEXP);
	}
	
	public GraphWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, negDR, posDR, negEXP, posEXP);
	}
	
	public GraphWidget(Context context, float negDR, float posDR, float negEXP, float posEXP) {
		super(context);
		init(context, negDR, posDR, negEXP, posEXP);
	}

	
	private void init(Context context, float negDR, float posDR, float negEXP, float posEXP){
		
		setRates(negDR, posDR, negEXP, posEXP);
		calcExp();
				
		getHolder().addCallback(this);
		
		thread = new CustomSurfaceGraphThread(getHolder(), this);
		
		setFocusable(true); // make sure we get key events
	
		setZOrderOnTop(true);    // necessario per la trasparenza
		getHolder().setFormat(PixelFormat.TRANSPARENT);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
			int arg3) {
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (thread.getState() == Thread.State.TERMINATED) {
            thread = new CustomSurfaceGraphThread(getHolder(), this);
            thread.setRunning(true);
            thread.start();
        }
        else {
            thread.setRunning(true);
            thread.start();
        }
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {}
    	}
	}
	
	public void update(float iNegDR, float iPosDR, float iNegEXP, float iPosEXP)
	{
		setRates(iNegDR, iPosDR, iNegEXP, iPosEXP);
		calcExp();
	}
	
	public void updateCursor(float xNorm)
	{
		cursorPos = (int) denormalize(xNorm, w);
	}
	
	
	public class CustomSurfaceGraphThread extends Thread {		
		 
		private SurfaceHolder myThreadSurfaceHolder;
		private GraphWidget myThreadSurfaceView;
		private boolean myThreadRun = false;
	 
		public CustomSurfaceGraphThread(SurfaceHolder surfaceHolder, GraphWidget surfaceView) {
			myThreadSurfaceHolder = surfaceHolder;
			myThreadSurfaceView = surfaceView;
		}
	 
		public void setRunning(boolean b) {
			myThreadRun = b;
		}

		
		@Override
		public void run() {
			while (myThreadRun) {
				Canvas c = null;
				try {
					c = myThreadSurfaceHolder.lockCanvas(null);
					synchronized (myThreadSurfaceHolder) {
						myThreadSurfaceView.onDraw(c);
					}
					sleep(2);             
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					//  do this in a finally so that if an exception is thrown
					// 	during the above, we don't leave the Surface in an
					// 	inconsistent state
					if (c != null) {
						myThreadSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}	
	}
	
	
}