package mx.edu.chmd1.networking;

import java.util.List;

import mx.edu.chmd1.modelos.Circular;
import mx.edu.chmd1.modelos.Circulares;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ICircularesCHMD {
    String VALIDAR_CUENTA="validarEmail.php";
    String METODO_REG="leerCircular.php";
    String METODO_DEL="eliminarCircular.php";
    String METODO_FAV="favCircular.php";
    String METODO_NOLEER="noleerCircular.php";
    String METODO_CIRC="getCirculares_iOS.php";
    @GET(VALIDAR_CUENTA)
    Call<String> validarCuenta(@Query("correo") String correo);
    @FormUrlEncoded
    @POST(METODO_REG)
    Call<String> leerCircular(@Field("circular_id") String circular_id,@Field("usuario_id") String usuario_id);
    @FormUrlEncoded
    @POST(METODO_NOLEER)
    Call<String> noLeerCircular(@Field("circular_id") String circular_id,@Field("usuario_id") String usuario_id);
    @FormUrlEncoded
    @POST(METODO_DEL)
    Call<String> eliminarCircular(@Field("circular_id") String circular_id,@Field("usuario_id") String usuario_id);
    @FormUrlEncoded
    @POST(METODO_FAV)
    Call<String> favCircular(@Field("circular_id") String circular_id,@Field("usuario_id") String usuario_id);

    @GET(METODO_CIRC)
    Call<List<Circulares>> getCirculares(@Query("usuario_id") String usuario_id);


}
