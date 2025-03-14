package hbv.web;

import jakarta.websocket.*;
import jakarta.websocket.server.*;
import java.io.*;
import java.util.logging.*;

@ServerEndpoint(value = "/websocketinsession", configurator = ServletAwareConfigurator.class)
public class HelloSessionEndpoint {

  @OnOpen
  public void onOpen(Session session, EndpointConfig config) {
    WebSocketContainer wsc = session.getContainer();
    Logger.getLogger("default").info(wsc.toString());
  }

  @OnClose
  public void onClose(Session session) {
    try {
      session.close();
    } catch (Exception e) {
    }
  }

  @OnError
  public void onError(Session session, Throwable thr) {}

  @OnMessage
  public void onMessage(String msg, Session session) throws IOException {
    if (session.isOpen()) {
      session.getBasicRemote().sendText("got:" + msg);
    }
  }
}
