package betting;

public class MeasuredMethod {

	String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public int getCalls() {
		return calls;
	}

	public void setCalls(int calls) {
		this.calls = calls;
	}

	long timeTaken;
	int calls;

	public MeasuredMethod(String name, long timeTaken, int calls) {
		this.name = name;
		this.timeTaken = timeTaken;
		this.calls = calls;
		
	}

}
