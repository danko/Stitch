package arch.model;

public class AnimatedValue {

	float value;
	private float goalValue;
	float rate, minRate = .05f, maxRate = .18f, metaRate = .005f;
	float threshold = .1f;
	boolean animating = false;
	
	public void init(float goal, float current) { setGoal(goal); snapTo(); setGoal(current); }
	
	public void live() {
		if(!animating) return;
		
		value += (goalValue-value)*rate;
		if(rate < maxRate) rate += metaRate;
		if(Math.abs(goalValue-value) < threshold) { value = goalValue; animating = false; }
	}
	
	public void setGoal(float g) { goalValue = g; rate = minRate; animating = true; }
	public float getGoal() { return goalValue; }
	public float get() { return value; }
	public void snapTo() { value = goalValue; animating = false; }

	public boolean isDone() { return !animating; }
	
}
