package arch.view;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import arch.model.Observable;
import arch.model.Point2d;
import arch.model.TimeProfile;
import arch.model.TimeValuePair;
import arch.model.TimelineParticipant;

import com.sun.opengl.util.j2d.TextRenderer;

public class Timeline extends Entity {

	static TextRenderer textRenderer, hoverLabel;

	public static void poke() {}
	static {
		EntityViewFactory.register(Timeline.class, arch.model.Timeline.class);
		
		textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 14), true, false);
		hoverLabel = new TextRenderer(new Font("SansSerif", Font.BOLD, 18), true, false);
	}
	
	static DateFormat dateFormat = new SimpleDateFormat();

	final arch.model.Timeline model;
	float leftMargin = 200f, rightMargin = 200f;
	long msSpan;
	float bodyWidth, pixelsPerMS, pixelsPerDay = 100f;
	int numRows;
	//Date viewMin, viewMax;

	boolean hovering = false;
	Point2d hoverPos = new Point2d();
	
	public Timeline(arch.model.Entity model) {
		super(model);
		this.model = (arch.model.Timeline) model;
		
		color = new Color(1f, 1f, 193/255f); //cream
		alpha = 1f;

		countRows();
		refresh();
	}
	
	//private Date oldMin;
	private void refresh() {
		// TODO adjust position based on delta to oldMin
		
		msSpan = model.max.getTime() - model.min.getTime();
		bodyWidth = (int)(msSpan/(float)(24*60*60*1000) * pixelsPerDay); // 20px/day
		if(bodyWidth < 1f) bodyWidth = 1f;
		pixelsPerMS = bodyWidth / (msSpan+1);
		
		size.x = leftMargin + bodyWidth + rightMargin;
		
		//oldMin = (Date) model.min.clone();
	}

	private void countRows() {
		int row = 0;
		for(TimelineParticipant p : model.getParticipants()) {
			++row;
			for(TimeProfile<?> tp : p.getSubmissions())
				++row;
		}
		numRows = row;
		size.y = numRows * 20f + 25f;
	}
	
	float timeToOffset(Date t) {
		return leftMargin + (t.getTime()-model.min.getTime())*pixelsPerMS;
	}
	
	Date offsetToTime(float x) {
		return new Date( model.min.getTime() + (long)((x-leftMargin)/bodyWidth * msSpan) );
	}
	
	public void draw(GLAutoDrawable glD) {
		super.draw(glD);
		
		GL gl = glD.getGL();
		
		gl.glPushMatrix();
			gl.glTranslatef(pos.x(), pos.y(), getDepth());
		
			// Draw min and max timestamps in margins
			textRenderer.begin3DRendering();
			textRenderer.setColor(0,0,0,1f);
			textRenderer.draw3D(dateFormat.format(model.min), 5f, 5f, 5f, 1f);
			textRenderer.draw3D(dateFormat.format(model.max), leftMargin+bodyWidth+5f, 5f, 5f, 1f);
			textRenderer.end3DRendering();

			// Draw day boundary lines
			if(pixelsPerDay >= 2f) {
				gl.glColor3f(.9f, .9f, .9f);
				gl.glBegin(GL.GL_LINES);
					for(float i = leftMargin+pixelsPerDay; i < size.x-rightMargin; i += pixelsPerDay) {
						gl.glVertex3f(i, 1f, 1f);
						gl.glVertex3f(i, size.y-1f, 1f);
					}
				gl.glEnd();
			}

			// Draw month boundary lines
			gl.glColor3f(.7f, .7f, .7f);
			gl.glBegin(GL.GL_LINES);
				for(float i = leftMargin+pixelsPerDay; i < size.x-rightMargin; i += pixelsPerDay*30) {
					gl.glVertex3f(i, 1f, 1f);
					gl.glVertex3f(i, size.y-1f, 1f);
				}
			gl.glEnd();

			// Draw margin lines
			gl.glColor3f(254/255f, 58/255f, 24/255f);
			gl.glBegin(GL.GL_LINES);
				gl.glVertex3f(leftMargin, 0f, 3f);
				gl.glVertex3f(leftMargin, size.y, 3f);
				gl.glVertex3f(size.x-rightMargin, 0f, 3f);
				gl.glVertex3f(size.x-rightMargin, size.y, 3f);
			gl.glEnd();

			// Draw row divisions
			//gl.glColor3f(0f, .3f, .8f);
			gl.glColor3f(.2f, .44f, .791f);
			gl.glBegin(GL.GL_LINES);
				for(int i = 1; i < numRows; ++i) {
					gl.glVertex3f(5f, i*20f, 2f);
					gl.glVertex3f(size.x-5f, i*20f, 2f);
				}
			gl.glEnd();

			// Draw TimeValuePair Markers
			float y = size.y-20, z = 5f;
			gl.glColor3f(1f, 0f, 0f);
			for(TimelineParticipant p : model.getParticipants()) {
				y -= 20;
				for(TimeProfile<?> tp : p.getSubmissions()) {
					for(TimeValuePair<?> tvp : tp.getTimeValuePairs()) {
						float tx = timeToOffset(tvp.timeStamp);
						switch(tvp.characterization) {
							case Atomic:
								gl.glBegin(GL.GL_LINES);
									gl.glVertex3f(tx, y+10, z);
									gl.glVertex3f(tx, y, z);
								gl.glEnd();
								break;
							case FromNowOn:
								gl.glBegin(GL.GL_LINE_STRIP);
									gl.glVertex3f(tx, y+10, z);
									gl.glVertex3f(tx, y, z);
									gl.glVertex3f(tx+10, y, z);
								gl.glEnd();
								break;
							case UpUntil:
								gl.glBegin(GL.GL_LINE_STRIP);
									gl.glVertex3f(tx, y+10, z);
									gl.glVertex3f(tx, y, z);
									gl.glVertex3f(tx-10, y, z);
								gl.glEnd();
								break;
							case PastAndFuture:
//								gl.glBegin(GL.GL_QUADS);
//									gl.glVertex3f(tx-1, y-1, z);
//									gl.glVertex3f(tx+1.5f, y-1, z);
//									gl.glVertex3f(tx+1.5f, y+1.5f, z);
//									gl.glVertex3f(tx-1, y+1.5f, z);
//								gl.glEnd();
								gl.glBegin(GL.GL_LINE_LOOP);
									gl.glVertex3f(tx-10f, y, z);
									gl.glVertex3f(tx+10f, y, z);
									gl.glVertex3f(tx, y+3f, z);
								gl.glEnd();
								break;
						}
					}
					y -= 20f;
				}
			}
			
			// Draw TimeValuePair Labels
			y = size.y-20f; z = 5f;
			textRenderer.begin3DRendering();
			textRenderer.setColor(0,0,0,1f);
			for(TimelineParticipant p : model.getParticipants()) {
				y -= 20;
				for(TimeProfile<?> tp : p.getSubmissions()) {
					for(TimeValuePair<?> tvp : tp.getTimeValuePairs()) {
						float tx = timeToOffset(tvp.timeStamp);
						textRenderer.draw3D(tvp.value.toString(), tx+3f, y+3f, z, 1f);
					}
					y -= 20f;
				}
			}
			textRenderer.end3DRendering();

			// Draw time-hover marker
			if(hovering) {
				z = 5f;
				if(hoverPos.x >= leftMargin && hoverPos.x <= size.x-rightMargin) {
					gl.glColor3f(0f, .6f, .3f);
					gl.glBegin(GL.GL_LINES);
						gl.glVertex3f(hoverPos.x, 0f         , z);
						gl.glVertex3f(hoverPos.x, getHeight()+25, z);
					gl.glEnd();
	
					Date h = offsetToTime(hoverPos.x);
					hoverLabel.begin3DRendering();
					hoverLabel.setColor(0f,.6f,.3f,1f);
					hoverLabel.draw3D(dateFormat.format(h), hoverPos.x+3, getHeight()+5, z, 1f);
					hoverLabel.end3DRendering();
				}
			}

			// Draw row labels
			float xs = 0f, x = xs;
			y = size.y-20f; z = 5f;
			textRenderer.begin3DRendering();
			textRenderer.setColor(0,0,0,1f);
			for(TimelineParticipant p : model.getParticipants()) {
				x = xs + 5;
				textRenderer.draw3D(p.entity.toString(), x, y, z, 1f);
				x += 20f; y -= 20;
				for(TimeProfile<?> tp : p.getSubmissions()) {
					textRenderer.draw3D(tp.getTimeProfileLabel(), x, y, z, 1f);
					y -= 20f;
				}
			}
			textRenderer.end3DRendering();

		gl.glPopMatrix();
	}

	public void hover(Point2d at) {
		// Local to Entity coordinate system (relative to pos)
		hovering = true;
		hoverPos.x = at.x;
		hoverPos.y = at.y;
	}
	
	@Override
	public void update(Observable o, String signal, Object obj) {
		super.update(o, signal, obj);

		if(o == model) {
			if(signal.equals(arch.model.Timeline.UpdateMessage.TimeSpan.name())) {
				refresh();
			}
			if(signal.equals(arch.model.Timeline.UpdateMessage.Participants.name())) {
				countRows();
			}
		}
	}

	public void zoomBy(float scale) {
		pixelsPerDay *= scale;
		if(pixelsPerDay < .1f) pixelsPerDay = .1f;

		float focus; // 'Zoom center'
		if(hovering)
			focus = Math.max(leftMargin, Math.min(hoverPos.x, size.x-rightMargin));
		else {
			float maxVisible = Stitch.currentStitchView.camera.pos.x + Stitch.currentStitchView.camera.halfViewWidth*2f;
			maxVisible = Math.min(maxVisible, pos.x()+size.x-rightMargin);
			float minVisible = Stitch.currentStitchView.camera.pos.x;
			minVisible = Math.max(minVisible, pos.x()+leftMargin);

			focus = (minVisible + maxVisible)/2 - pos.x();
		}

		float oldBodyWidth = bodyWidth;
		refresh();
		
		float proportion = (focus-leftMargin) / oldBodyWidth;
		float dx = (bodyWidth-oldBodyWidth) * proportion;
		pos.x(pos.x() - dx);
		pos.snap();
		hoverPos.x += dx;
	}

	public boolean isHovered() { return hovering; }
	public void exited() {
		hovering = false;
	}
	
}
