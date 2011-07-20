package temp;

public class DefaultMap implements TempMap {
	public String tempMap(AtomicTemp t) {
	   return t.toString();
	}

	public DefaultMap() {}
}
