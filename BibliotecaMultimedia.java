import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BibliotecaMultimedia extends Application {

    private MediaPlayer mediaPlayer;
    private Label tituloArchivo;
    private Slider barraProgreso;
    private Label tiempoReproduccion;
    private MediaView mediaView;
    private ListView<String> listaArchivos;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Biblioteca Multimedia");

        // Cargar el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("pantalla.fxml"));

        // Obtener los controles del archivo FXML
        tituloArchivo = (Label) root.lookup("#tituloArchivo");
        mediaView = (MediaView) root.lookup("#mediaView");
        barraProgreso = (Slider) root.lookup("#barraProgreso");
        tiempoReproduccion = (Label) root.lookup("#tiempoReproduccion");
        listaArchivos = (ListView<String>) root.lookup("#listaArchivos");

        // Ruta específica de la carpeta
        String rutaCarpeta = "Multimedia"; // Cambia esta ruta a la carpeta que desees
        establecerCarpetaPorDefecto(rutaCarpeta);

        // Configuración de botones
        Button btnPlay = (Button) root.lookup("#Play");
        Button btnPause = (Button) root.lookup("#Pause");
        Button btnStop = (Button) root.lookup("#Stop");

        btnPlay.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        });

        btnPause.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        btnStop.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        // Acción al seleccionar un archivo de la biblioteca
        listaArchivos.setOnMouseClicked(e -> {
            String archivoSeleccionado = listaArchivos.getSelectionModel().getSelectedItem();
            if (archivoSeleccionado != null) {
                File archivo = new File(rutaCarpeta, archivoSeleccionado);
                cargarArchivo(archivo);
            }
        });

        // Crear la escena
        Scene scene = new Scene(root, 800, 600);

        // Agregar el CSS a la escena
        URL cssURL = getClass().getResource("@styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarArchivo(File archivo) {
        // Detener la reproducción actual si existe
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Configurar Media y MediaPlayer
        Media media = new Media(archivo.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        barraProgreso.setValue(0);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            barraProgreso.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
            tiempoReproduccion.setText(
                formatTime(newTime) + " / " + formatTime(mediaPlayer.getTotalDuration())
            );
        });

        barraProgreso.setOnMouseClicked(e -> {
            if (mediaPlayer != null) {
                double newTime = barraProgreso.getValue() * mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(newTime));
            }
        });

        // Mostrar título del archivo
        tituloArchivo.setText("Reproduciendo: " + archivo.getName());

        // Mostrar contenido multimedia en MediaView
        mediaView.setMediaPlayer(mediaPlayer);

        // Reproducir el video
        mediaPlayer.play();
    }

    private void establecerCarpetaPorDefecto(String rutaCarpeta) {
        File carpeta = new File(rutaCarpeta);

        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("La carpeta especificada no existe o no es válida.");
            return;
        }

        cargarArchivosDeBiblioteca(carpeta);
    }

    private void cargarArchivosDeBiblioteca(File carpeta) {
        // Limpiar la lista actual
        if(listaArchivos != null){
            listaArchivos.getItems().clear();
        }

        // Listar los archivos multimedia en la carpeta especificada
        File[] archivos = carpeta.listFiles((dir, name) ->
            name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav")
        );

        if (archivos != null) {
            for (File archivo : archivos) {
                if(listaArchivos != null){
                listaArchivos.getItems().add(archivo.getName());
                }
            }
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
