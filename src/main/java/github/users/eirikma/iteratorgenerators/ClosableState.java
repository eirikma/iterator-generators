package github.users.eirikma.iteratorgenerators;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to share state between iterators & yields that
 * can conceptually be 'closed', just like input/outputStreams.
 */
class ClosableState implements Closeable {

    private volatile boolean closed;

    private List<PropertyChangeListener> listeners = new ArrayList<>(1);

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() throws IOException {
        boolean wasClosed = this.closed;
        this.closed = true;
        if(!wasClosed) {
            for (PropertyChangeListener propertyChangeListener : listeners) {
                propertyChangeListener.propertyChange(new PropertyChangeEvent(this, "closed", wasClosed, true));
            }
        }
    }

    public synchronized void addPListener(PropertyChangeListener l) {
        listeners.add(l);
    }


}
