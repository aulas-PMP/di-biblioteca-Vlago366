import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BibliotecaMultimedia extends Application {

    private MediaPlayer mediaPlayer;
    private Label tituloArchivo;
    private Slider barraProgreso, sliderVolumen;
    private MenuItem menuMinimizar, menuMaximizar;
    private Label tiempoReproduccion;
    private ImageView imageViewFondo;
    private MediaView mediaView;
    private ListView<String> listaArchivos;
    private Button btnAumentarVelocidad, btnReducirVelocidad, btnCerrarEditor;
    private VBox editorVideo, biblioteca;

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
        sliderVolumen = (Slider) root.lookup("#sliderVolumen");
        tiempoReproduccion = (Label) root.lookup("#tiempoReproduccion");
        listaArchivos = (ListView<String>) root.lookup("#listaArchivos");
        btnAumentarVelocidad = (Button) root.lookup("#btnAumentarVelocidad");
        btnReducirVelocidad = (Button) root.lookup("#btnReducirVelocidad");
        btnCerrarEditor = (Button) root.lookup("#btnCerrarEditor");
        imageViewFondo = (ImageView) root.lookup("#imageViewFondo");

        editorVideo = (VBox) root.lookup("#editor-video");
        biblioteca = (VBox) root.lookup("#biblioteca");

        // Ruta específica de la carpeta
        String rutaCarpeta = "Multimedia";
        establecerCarpetaPorDefecto(rutaCarpeta);

        // Configuración de botones de reproducción
        Button btnPlay = (Button) root.lookup("#Play");
        Button btnPause = (Button) root.lookup("#Pause");
        Button btnStop = (Button) root.lookup("#Stop");

        btnPlay.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });

        btnPause.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });

        btnStop.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
        });

        // Configuración de velocidad del video
        btnAumentarVelocidad.setOnAction(e -> cambiarVelocidad(0.5));
        btnReducirVelocidad.setOnAction(e -> cambiarVelocidad(-0.5));

        // Acción para ocultar/mostrar el editor de video
        btnCerrarEditor.setOnAction(e -> {
            if (editorVideo.isVisible()) {
                editorVideo.setVisible(false);
                btnCerrarEditor.setText("▶");
            } else {
                editorVideo.setVisible(true);
                btnCerrarEditor.setText("❌");
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

        // Configurar el control de volumen con el slider
        sliderVolumen.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        //menuMinimizar.setOnAction(e -> minimizarMaximizar(biblioteca));
        //menuMaximizar.setOnAction(e -> minimizarMaximizar(editorVideo));
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

    // Método para minimizar/maximizar los VBox de la biblioteca o el editor de video
    /**private void minimizarMaximizar(VBox vbox) {
        if (vbox.isVisible()) {
            vbox.setVisible(false);
        } else {
            vbox.setVisible(true);
        }
    }*/

    /**private void cargarArchivo(File archivo) {
    if (mediaPlayer != null) {
        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = null;
    }*/

    private void cargarArchivo(File archivo) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    
        try {
            String nombreArchivo = archivo.getName().toLowerCase();
            if (nombreArchivo.endsWith(".mp4") || nombreArchivo.endsWith(".mp3") || nombreArchivo.endsWith(".wav")) {
                // Si es un video o audio, cargarlo en MediaPlayer
                Media media = new Media(archivo.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                tituloArchivo.setText("Reproduciendo: " + archivo.getName());
    
                // Sincronizar volumen inicial con el slider
                mediaPlayer.setVolume(sliderVolumen.getValue());
    
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
            } else {
                // Si no es un video o audio, mostrar la imagen de fondo
                mediaView.setMediaPlayer(null);
                Image imagenFondo = new Image(new File("ambipom.png").toURI().toString());
                mediaView.setPreserveRatio(true);
                mediaView.setFitWidth(600);
                mediaView.setFitHeight(400);
                tituloArchivo.setText("Mostrando imagen: " + archivo.getName());
            }
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
