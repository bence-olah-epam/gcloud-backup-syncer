package com.olah.gcloud.backup.syncer;


import com.google.api.client.util.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 *
 *
 *
 * Based on LocalServerReceiver.
 * Accepts connections from everywhere and redirects to a specific hostname.
 *
 */
public final class VerificationCodeReceiver implements com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver {

    private static final String LOCALHOST = "localhost";

    private static final String CALLBACK_PATH = "/Callback";

    /** Server or {@code null} before {@link #getRedirectUri()}. */
    private Server server;

    /** Verification code or {@code null} for none. */
    String code;

    /** Error code or {@code null} for none. */
    String error;

    /** To block until receiving an authorization response or stop() is called. */
    final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

    /** Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}. */
    private int port;

    /** Host name to use. */
    private final String host;

    /** Callback path of redirect_uri */
    private final String callbackPath;

    /**
     * URL to an HTML page to be shown (via redirect) after successful login. If null, a canned
     * default landing page will be shown (via direct response).
     */
    private String successLandingPageUrl;

    /**
     * URL to an HTML page to be shown (via redirect) after failed login. If null, a canned
     * default landing page will be shown (via direct response).
     */
    private String failureLandingPageUrl;
    private String redirectHost;

    /**
     * Constructor that starts the server on {@link #LOCALHOST} and an unused port.
     *
     * <p>
     * Use {@link VerificationCodeReceiver.Builder} if you need to specify any of the optional parameters.
     * </p>
     */
    public VerificationCodeReceiver() {
        this(LOCALHOST, -1, CALLBACK_PATH, null, null);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    VerificationCodeReceiver(String host, int port,
                             String successLandingPageUrl, String failureLandingPageUrl) {
        this(host, port, CALLBACK_PATH, successLandingPageUrl, failureLandingPageUrl);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    VerificationCodeReceiver(String host, int port, String callbackPath,
                             String successLandingPageUrl, String failureLandingPageUrl) {
        this.host = host;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    public VerificationCodeReceiver(String serverHost, String redirectHost, int port, String callbackPath, String successLandingPageUrl, String failureLandingPageUrl) {
        this.host = serverHost;
        this.redirectHost = redirectHost;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    @Override
    public String getRedirectUri() throws IOException {
        server = new Server(port != -1 ? port : 0);
        Connector connector = server.getConnectors()[0];
        connector.setHost(host);
        server.addHandler(new VerificationCodeReceiver.CallbackHandler());
        try {
            server.start();
            port = connector.getLocalPort();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e);
            throw new IOException(e);
        }
        return "http://" + redirectHost + ":" + port + callbackPath;
    }

    /**
     * Blocks until the server receives a login result, or the server is stopped
     * by {@link #stop()}, to return an authorization code.
     *
     * @return authorization code if login succeeds; may return {@code null} if the server
     *    is stopped by {@link #stop()}
     * @throws IOException if the server receives an error code (through an HTTP request
     *    parameter {@code error})
     */
    @Override
    public String waitForCode() throws IOException {
        waitUnlessSignaled.acquireUninterruptibly();
        if (error != null) {
            throw new IOException("User authorization failed (" + error + ")");
        }
        return code;
    }

    @Override
    public void stop() throws IOException {
        waitUnlessSignaled.release();
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                throw new IOException(e);
            }
            server = null;
        }
    }

    /** Returns the serverHost name to use. */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns callback path used in redirect_uri.
     */
    public String getCallbackPath() {
        return callbackPath;
    }

    /**
     * Builder.
     *
     * <p>
     * Implementation is not thread-safe.
     * </p>
     */
    public static final class Builder {

        /** Host name to use. */
        private String serverHost = LOCALHOST;

        /** Port to use or {@code -1} to select an unused port. */
        private int port = -1;

        private String successLandingPageUrl;
        private String failureLandingPageUrl;

        private String callbackPath = CALLBACK_PATH;
        private String redirectHost;

        /** Builds the {@link VerificationCodeReceiver}. */
        public VerificationCodeReceiver build() {
            System.out.println("******build called");
            return new VerificationCodeReceiver(serverHost, redirectHost, port, callbackPath,
                    successLandingPageUrl, failureLandingPageUrl);
        }

        /** Returns the serverHost name to use. */
        public String getServerHost() {
            return serverHost;
        }

        /** Sets the serverHost name to use. */
        public VerificationCodeReceiver.Builder setServerHost(String host) {
            this.serverHost = host;
            return this;
        }

        /** Returns the port to use or {@code -1} to select an unused port. */
        public int getPort() {
            return port;
        }

        /** Sets the port to use or {@code -1} to select an unused port. */
        public VerificationCodeReceiver.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /** Returns the callback path of redirect_uri */
        public String getCallbackPath() {
            return callbackPath;
        }

        /** Set the callback path of redirect_uri */
        public VerificationCodeReceiver.Builder setRedirectHost(String redirectHost) {
            this.redirectHost = redirectHost;
            return this;
        }

        /** Set the callback path of redirect_uri */
        public VerificationCodeReceiver.Builder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public VerificationCodeReceiver.Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
            this.successLandingPageUrl = successLandingPageUrl;
            this.failureLandingPageUrl = failureLandingPageUrl;
            return this;
        }
    }

    /**
     * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it
     * where {@link #waitForCode} will find it.
     */
    class CallbackHandler extends AbstractHandler {

        @Override
        public void handle(
                String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException {
            System.out.println("******handle called");
            if (!callbackPath.equals(target)) {
                System.out.println("******call back is not equals to target");
                return;
            }

            try {
                ((Request) request).setHandled(true);
                error = request.getParameter("error");
                code = request.getParameter("code");

                if (error == null && successLandingPageUrl != null) {
                    System.out.println("******lofasz1");

                    response.sendRedirect(successLandingPageUrl);
                } else if (error != null && failureLandingPageUrl != null) {
                    System.out.println("******lofasz2");

                    response.sendRedirect(failureLandingPageUrl);
                } else {
                    System.out.println("******lofasz3");

                    writeLandingHtml(response);
                }
                response.flushBuffer();
            }
            finally {
                waitUnlessSignaled.release();
            }
        }

        private void writeLandingHtml(HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            PrintWriter doc = response.getWriter();
            doc.println("<html>");
            doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
            doc.println("<body>");
            doc.println("Received verification code. You may now close this window.");
            doc.println("</body>");
            doc.println("</html>");
            doc.flush();
        }
    }
}
