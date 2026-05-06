import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class SupabaseService {

    private final String baseUrl;
    private final String apiKey;
    private final String jwt;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public SupabaseService(String baseUrl, String apiKey, String jwt) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.jwt = jwt;
    }

    private Request.Builder baseRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + jwt)
                .addHeader("Content-Type", "application/json");
    }

    // -------------------------
    // CREATE CLASS
    // -------------------------
    public Classroom createClass(Classroom classroom) throws IOException {
        String json = mapper.writeValueAsString(classroom);

        Request request = baseRequest(baseUrl + "/rest/v1/classes")
                .addHeader("Prefer", "return=representation")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();

        String body = response.body().string();

        List<Classroom> result = mapper.readValue(
                body,
                mapper.getTypeFactory().constructCollectionType(List.class, Classroom.class)
        );

        return result.get(0);
    }

    // -------------------------
    // JOIN CLASS BY CODE
    // -------------------------
    public void joinClass(String code) throws IOException {
        String json = "{ \"code\": \"" + code + "\" }";

        Request request = baseRequest(baseUrl + "/rest/v1/rpc/join_class_by_code")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        client.newCall(request).execute();
    }

    // -------------------------
    // GET MY CLASSES
    // -------------------------
    public List<Classroom> getMyClasses() throws IOException {
        Request request = baseRequest(baseUrl + "/rest/v1/classes")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();

        return mapper.readValue(
                body,
                mapper.getTypeFactory().constructCollectionType(List.class, Classroom.class)
        );
    }
}