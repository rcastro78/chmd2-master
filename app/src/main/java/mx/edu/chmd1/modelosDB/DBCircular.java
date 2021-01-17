package mx.edu.chmd1.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

//String idCircular, String nombre, String textoCircular, String estado
@Table(name = "appCircular", id="id")
public class DBCircular extends Model {
    @Column(name="idCircular",unique = true)
    public String idCircular;
    @Column(name="nombre")
    public String nombre;
    @Column(name="textoCircular")
    public String textoCircular;
    @Column(name="no_leida")
    public int no_leida;
    @Column(name="leida")
    public int leida;
    @Column(name="favorita")
    public int favorita;
    @Column(name="contenido")
    public String contenido;
    @Column(name="eliminada")
    public int eliminada;
    @Column(name="idUsuario")
    public int idUsuario;

    @Column(name="created_at")
    public String created_at;

    @Column(name="updated_at")
    public String updated_at;

    @Column(name="recordatorio")
    public int recordatorio;
}
