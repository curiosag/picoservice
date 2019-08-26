package miso.misc;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.logging.Logger;

public class TcpServer implements Runnable {

    private final Logger LOG = Logger.getLogger(TcpServer.class.getSimpleName());
    private final int port;

    private boolean running;

    public TcpServer(int port) {
        this.port = port;
    }

/*iCg*U#5L6F%XycLH0$wTQ8t5$A8FX22OHzBAr@Xe
miso.misc.Service FOO:fth
* FOO_SERVICE_HOST=<the host the miso.misc.Service is running on>
  FOO_SERVICE_PORT=<the port the miso.misc.Service is running on>
* */

    private void talk(String value){

    }

    public void eval(){
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");

            String name = "Mahesh";
            Integer result = null;

            try {
                nashorn.eval("print('" + name + "')");
                //nashorn.setBindings();
                result = (Integer) nashorn.eval("10 + 2");
            } catch(ScriptException e) {
                System.out.println("Error executing script: "+ e.getMessage());
            }
            System.out.println(result.toString());
        }


    public synchronized void start() {
        if (running) {
            throw new IllegalStateException("already running");
        }
        LOG.info("rudi on " + port);
        running = true;
        new Thread(this).start();
    }

    @Override
    public synchronized void run() {
        if (!running) {
            throw new IllegalStateException("won't run sans start.");
        }
        listen();
    }

    private void listen(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);
            while (running && !Thread.interrupted()) {
                try (Socket clientSocket = serverSocket.accept()) {
                    clientSocket.setSoTimeout(3000);
                    listen(clientSocket);
                } catch (SocketTimeoutException ex) {
                }
            }
        } catch (Exception e) {
            LOG.severe("died from " + e.getMessage());
        }
    }

    private void listen(Socket clientSocket) throws IOException {
        try (BufferedReader in = getReader(clientSocket)) {
                try {
                   readJson(in).ifPresent(j -> System.out.println(j)

                   );

                } catch (SocketTimeoutException ex) {
                }
            }

    }

    private Optional<String> readJson(BufferedReader in) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int countLeftBraces = 0;
        int chr = 0;

        while (true) {
            try {
                chr = in.read();
            } catch (IOException e) {
                return Optional.empty();
            }
            if (!skip(countLeftBraces, chr)) {
                countLeftBraces = countLeftBraces + getBraceDelta(chr);

                if (countLeftBraces < 0) {
                    LOG.warning("read invalid json: " + buffer.toString());
                    return Optional.empty();
                }

                buffer.append(chr);
                if (countLeftBraces == 0) {
                    return Optional.of(buffer.toString());
                }
            }
        }
    }

    private static final int LBR = 123;
    private static final int RBR = 125;

    private boolean skip(int countLeftBraces, int chr) {
        return countLeftBraces == 0 && chr != LBR;
    }

    private int getBraceDelta(int chr) {
        switch (chr) {
            case LBR:
                return 1;
            case RBR:
                return -1;
            default:
                return 0;
        }
    }


    private int decodeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return -1;
    }

    private PrintWriter getWriter(Socket clientSocket) throws IOException {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
    }

    private BufferedReader getReader(Socket clientSocket) throws IOException {
        return new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public synchronized void stop() {
        this.running = false;
    }


}
