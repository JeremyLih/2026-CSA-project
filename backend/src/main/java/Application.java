import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) throws IOException {
        int port = Config.port();
        Gemini gemini = new Gemini();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        SessionStore sessionStore = new SessionStore();
        var chatContext = server.createContext("/api/chat", new ChatHandler(gemini));
        chatContext.getFilters().add(new CorsFilter());        
        var sessionsContext = server.createContext("/api/sessions/start", new StartSessionHandler(sessionStore));
        sessionsContext.getFilters().add(new CorsFilter());

        var questionsContext = server.createContext("/api/questions/next", new NextQuestionHandler(sessionStore, gemini));
        questionsContext.getFilters().add(new CorsFilter());
        server.createContext("/health", exchange -> HttpResponses.sendJson(exchange, 200, "{\"status\":\"ok\"}"));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Backend listening on http://localhost:" + port);
    }
}
