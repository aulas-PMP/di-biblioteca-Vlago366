import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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
    private Button btnAumentarVelocidad, btnReducirVelocidad;

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
        btnAumentarVelocidad = (Button) root.lookup("#btnAumentarVelocidad");
        btnReducirVelocidad = (Button) root.lookup("#btnReducirVelocidad");

        // Ruta específica de la carpeta
        String rutaCarpeta = "Multimedia";
        establecerCarpetaPorDefecto(rutaCarpeta);

        // Configuración de botones de reproducción
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

        // Configuración de velocidad del video
        btnAumentarVelocidad.setOnAction(e -> cambiarVelocidad(0.5));
        btnReducirVelocidad.setOnAction(e -> cambiarVelocidad(-0.5));

        // Acción al seleccionar un archivo de la biblioteca
        listaArchivos.setOnMouseClicked(e -> {
            String archivoSeleccionado = listaArchivos.getSelectionModel().getSelectedItem();
            if (archivoSeleccionado != null) {
                File archivo = new File(rutaCarpeta, archivoSeleccionado);
                cargarArchivo(archivo);
            }
        });

        // Ajustar el MediaView al espacio disponible
        mediaView.fitWidthProperty().bind(((StackPane) mediaView.getParent()).widthProperty());
        mediaView.fitHeightProperty().bind(((StackPane) mediaView.getParent()).heightProperty());
        mediaView.setPreserveRatio(true);

        // Crear la escena
        Scene scene = new Scene(root, 1200, 600);

        // Agregar el CSS a la escena
        URL cssURL = getClass().getResource("styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarArchivo(File archivo) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(archivo.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            tituloArchivo.setText("Reproduciendo: " + archivo.getName());

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (mediaPlayer.getTotalDuration() != null) {
                    barraProgreso.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                    tiempoReproduccion.setText(formatTime(newTime) + " / " + formatTime(mediaPlayer.getTotalDuration()));
                }
            });

            barraProgreso.setOnMouseClicked(e -> {
                if (mediaPlayer != null) {
                    double newTime = barraProgreso.getValue() * mediaPlayer.getTotalDuration().toSeconds();
                    mediaPlayer.seek(Duration.seconds(newTime));
                }
            });

            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Error al cargar el archivo multimedia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cambiarVelocidad(double cambio) {
        if (mediaPlayer != null) {
            double nuevaVelocidad = mediaPlayer.getRate() + cambio;
            nuevaVelocidad = Math.max(0.5, Math.min(nuevaVelocidad, 3.0)); // Limitar entre 0.5x y 3.0x
            mediaPlayer.setRate(nuevaVelocidad);
        }
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
        if (listaArchivos != null) {
            listaArchivos.getItems().clear();
        }

        File[] archivos = carpeta.listFiles((dir, name) ->
            name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav")
        );

        if (archivos != null) {
            for (File archivo : archivos) {
                if (listaArchivos != null) {
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
