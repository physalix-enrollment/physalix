/*
 * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
 *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
 *               Alexander Rampp, Kai Weidner
 *
 * This file is part of the Physalix Enrollment System
 *
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package hsa.awp.admingui;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This class is for a temporary runtime instance of the jetty webserver.
 *
 * @author basti
 */
public final class Start {
  /**
   * Private constructor.
   */
  private Start() {

  }

  /**
   * The main method to run the server.
   *
   * @param args console args
   * @throws Exception something happend
   */
  public static void main(String[] args) throws Exception {

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);

    // Set some timeout options to make debugging easier.
    connector.setIdleTimeout(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(8081);
    server.setConnectors(new Connector[]{connector});

    WebAppContext bb = new WebAppContext();
    bb.setServer(server);
    bb.setContextPath("/");
    bb.setWar("src/main/webapp");

    server.setHandler(bb);

    try {
      server.start();
      System.in.read();
      // while (System.in.available() == 0) {
      // Thread.sleep(5000);
      // }
      server.stop();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }
}
