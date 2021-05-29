package mx.edu.chmd1.networking;

public class APIUtils {
    public static final String BASE_URL = "https://www.chmd.edu.mx/WebAdminCirculares/ws/";

    public static ICircularesCHMD getCircularesService() {
        return RetrofitClient.getClient(BASE_URL).create(ICircularesCHMD.class);
    }
}
