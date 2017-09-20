package dmv;

import java.util.ArrayList;

public class Tuple {

	public Tuple() {
		// TODO Auto-generated constructor stub
		bp = new ArrayList<Tuple>();
	}

	public Tuple(int v, int h, int s, int e){
		this.pos = v;
		this.head = h;
		this.start =s;
		this.end = e;
		bp = new ArrayList<Tuple>();
	}
	
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int pos;
	public int head;
	public int start;
	public int end;
	public ArrayList<Tuple> bp;
	public int headindex;
}
