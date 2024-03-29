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
    @Column(name="recordatorio")
    public int recordatorio;
    @Column(name="created_at")
    public String created_at;

    @Column(name="updated_at")
    public String updated_at;

    @Column(name="fecha_ics")
    public String fecha_ics;

    @Column(name="para")
    public String para;

    @Column(name="adm")
    public String adm;

    @Column(name="rts")
    public String rts;

    //nuevos
    @Column(name="estado")
    public String estado;
    @Column(name="temaIcs")
    public String temaIcs;
    @Column(name="adjunto")
    public String adjunto;
    @Column(name="nivel")
    public String nivel;

}
