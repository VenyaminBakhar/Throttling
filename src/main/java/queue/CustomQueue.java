package queue;

public interface CustomQueue<T> {
    void add(T o);
    T poll();
}
