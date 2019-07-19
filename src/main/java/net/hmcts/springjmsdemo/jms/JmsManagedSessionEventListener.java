package net.hmcts.springjmsdemo.jms;

import javax.jms.Session;

public interface JmsManagedSessionEventListener {

    /**
     * Called immediately after a session is opened (includes being reopened!)
     *
     * @param session
     */
    void onSessionOpened(Session session);

    /**
     * Called immediately before a session is closed
     * @param session
     */
    void onSessionClosed(Session session);

}
