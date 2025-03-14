package hbv.web;

import jakarta.servlet.http.*;
import jakarta.websocket.*;
import jakarta.websocket.server.*;
import java.io.*;
import java.util.logging.*;

public class ServletAwareConfigurator extends ServerEndpointConfig.Configurator {
  @Override
  public void modifyHandshake(
      ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
    Object httpSessionObject = request.getHttpSession();
    if (httpSessionObject instanceof HttpSession httpSession) {
      Logger.getLogger("default").info("session:" + httpSession);
      Logger.getLogger("default").info("user:" + httpSession.getAttribute("user"));
    }
  }

  @Override
  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    T endpoint = super.getEndpointInstance(endpointClass);
    return endpoint;
  }
}
