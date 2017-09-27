package domain;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Domain implements Delayed{
	String name;
	String prev;
	int id;

	public Domain(String name, String prev, int id) {
		super();
		this.name = name;
		this.prev = prev;
		this.id = id;
		time=now()+10000l;
	}

	public Domain() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrev() {
		return prev;
	}

	public void setPrev(String prev) {
		this.prev = prev;
	}

	@Override
	public String toString() {
		return "Domain [name=" + name + ", prev=" + prev + ", id=" + id + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name,prev,id);
	}

	@Override
	public boolean equals(Object obj) {
		if(null==obj||obj.getClass()!=getClass())
			return false;
		Domain temp=(Domain)obj;
		if(temp.getName()==getName()&&temp.getId()==getId()&&temp.getPrev()==getPrev())
			return true;
		return false;
	}

	@Override
	public int compareTo(Delayed o) {
		return o.equals(this) ?1:-1;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(time - now(), NANOSECONDS);
	}
	
	
	long time;
	final static long now() {
        return System.nanoTime();
    }
}
