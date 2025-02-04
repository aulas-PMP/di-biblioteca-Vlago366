import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BibliotecaMultimedia extends Application {

    private MediaPlayer mediaPlayer;
    private Label tituloArchivo;
    private Slider barraProgreso, sliderVolumen;
    private Label tiempoReproduccion;
    private MediaView mediaView;
    private ListView<String> listaArchivos;
    private Button btnAumentarVelocidad, btnReducirVelocidad, btnAmpliarVideo, btnReducirVideo;
    private VBox editorVideo, biblioteca;
    private StackPane stackPane;

    @FXML private MenuItem menuAbrir, menuBiblioteca, menuEditorVideo, menuAcercaDe;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Biblioteca Multimedia");

        // Cargar el archivo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("pantalla.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        // Obtener los controles del archivo FXML
        tituloArchivo = (Label) root.lookup("#tituloArchivo");
        mediaView = (MediaView) root.lookup("#mediaView");
        barraProgreso = (Slider) root.lookup("#barraProgreso");
        sliderVolumen = (Slider) root.lookup("#sliderVolumen");
        tiempoReproduccion = (Label) root.lookup("#tiempoReproduccion");
        listaArchivos = (ListView<String>) root.lookup("#listaArchivos");
        btnAumentarVelocidad = (Button) root.lookup("#btnAumentarVelocidad");
        btnReducirVelocidad = (Button) root.lookup("#btnReducirVelocidad");
        stackPane = (StackPane) root.lookup("#stackPane");
        btnAmpliarVideo = (Button) root.lookup("#btnAmpliarVideo");
        btnReducirVideo = (Button) root.lookup("#btnReducirVideo");
        mediaView.setPreserveRatio(false);

        editorVideo = (VBox) root.lookup("#editor-video");
        biblioteca = (VBox) root.lookup("#biblioteca");

        btnAmpliarVideo.setOnAction(e -> ajustarTamañoVideo(1.2));
        btnReducirVideo.setOnAction(e -> ajustarTamañoVideo(0.8));

        // Configuración del menú
        menuAbrir.setOnAction(e -> abrirArchivo());
        menuBiblioteca.setOnAction(e -> togglePanel(biblioteca));
        menuEditorVideo.setOnAction(e -> togglePanel(editorVideo));
        menuAcercaDe.setOnAction(e -> mostrarAcercaDe());

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

        btnAumentarVelocidad.setOnAction(e -> cambiarVelocidad(0.5));
        btnReducirVelocidad.setOnAction(e -> cambiarVelocidad(-0.5));

        // Cargar la lista de archivos de la carpeta Multimedia por defecto
        cargarArchivosDeBiblioteca();

        // Acción al seleccionar un archivo de la biblioteca
        listaArchivos.setOnMouseClicked(e -> {
            String archivoSeleccionado = listaArchivos.getSelectionModel().getSelectedItem();
            if (archivoSeleccionado != null) {
                File archivo = new File("Multimedia", archivoSeleccionado);
                cargarArchivo(archivo);
            }
        });

        // Configurar el control de volumen con el slider
        sliderVolumen.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        Scene scene = new Scene(root, 1200, 600);

        URL cssURL = getClass().getResource("styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void togglePanel(VBox panel) {
        if (panel.isVisible()) {
            panel.setVisible(false);
            panel.setManaged(false);
        } else {
            panel.setVisible(true);
            panel.setManaged(true);
        }
    }

    private void mostrarAcercaDe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText(null);
        alert.setContentText("Autor: Víctor Lago González\n2ºDAM\nTítulo: Biblioteca escolar" );
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void abrirArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos Multimedia", "*.mp4", "*.mp3", "*.wav", "*.jpg", "*.png"));
        File archivo = fileChooser.showOpenDialog(null);
        if (archivo != null) {
            cargarArchivo(archivo);
        }
    }

    private void cargarArchivosDeBiblioteca() {
        File carpetaMultimedia = new File("Multimedia");
        if (carpetaMultimedia.exists() && carpetaMultimedia.isDirectory()) {
            File[] archivos = carpetaMultimedia.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".jpg") || name.endsWith(".png"));
            if (archivos != null) {
                for (File archivo : archivos) {
                    listaArchivos.getItems().add(archivo.getName());
                }
            }
        } else {
            mostrarError("La carpeta 'Multimedia' no existe o no es accesible.");
        }
    }

    private void ajustarTamañoVideo(double factor) {
        if (mediaView != null) {
            double nuevaAnchura = mediaView.getFitWidth() * factor;
            double nuevaAltura = mediaView.getFitHeight() * factor;
            double maxWidth = stackPane.getWidth();
            double maxHeight = stackPane.getHeight();
            nuevaAnchura = Math.min(nuevaAnchura, maxWidth);
            nuevaAltura = Math.min(nuevaAltura, maxHeight);
            if (mediaView.fitWidthProperty().isBound()) {
                mediaView.fitWidthProperty().unbind();
            }
            if (mediaView.fitHeightProperty().isBound()) {
                mediaView.fitHeightProperty().unbind();
            }
            mediaView.setFitWidth(nuevaAnchura);
            mediaView.setFitHeight(nuevaAltura);
        }
    }

    private void cargarArchivo(File archivo) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        try {
            String nombreArchivo = archivo.getName().toLowerCase();
            if (nombreArchivo.endsWith(".mp4") || nombreArchivo.endsWith(".mp3") || nombreArchivo.endsWith(".wav")) {
                Media media = new Media(archivo.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                if (nombreArchivo.endsWith(".mp4")) {
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaView.setVisible(true);
                    stackPane.getChildren().setAll(mediaView);

                    mediaPlayer.setOnReady(() -> {
                        mediaView.fitWidthProperty().bind(stackPane.widthProperty());
                        mediaView.fitHeightProperty().bind(stackPane.heightProperty());
                    });
                    mediaPlayer.play();
                } else {
                    Image imagenFondo = new Image(new File("src/ambipom.png").toURI().toString());
                    ImageView imageView = new ImageView(imagenFondo);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(600);
                    imageView.setFitHeight(400);
                    stackPane.getChildren().setAll(imageView);
                    mediaView.setVisible(false);
                }
                tituloArchivo.setText("Reproduciendo: " + archivo.getName());
                mediaPlayer.setVolume(sliderVolumen.getValue());
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (mediaPlayer.getTotalDuration() != null) {
                        barraProgreso.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                        tiempoReproduccion.setText(formatTime(newTime) + " / " + formatTime(mediaPlayer.getTotalDuration()));
                    }
                });
                barraProgreso.setOnMouseClicked(e -> {
                    if (mediaPlayer != null) {
                        double posicion = barraProgreso.getValue() * mediaPlayer.getTotalDuration().toSeconds();
                        mediaPlayer.seek(Duration.seconds(posicion));
                    }
                });
            } else {
                mostrarError("El archivo no es compatible.");
            }
        } catch (Exception e) {
            mostrarError("Error al cargar el archivo: " + e.getMessage());
        }
    }

    private String formatTime(Duration duration) {
        int totalSeconds = (int) duration.toSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void cambiarVelocidad(double incremento) {
        if (mediaPlayer != null) {
            double velocidadActual = mediaPlayer.getRate();
            mediaPlayer.setRate(velocidadActual + incremento);
        }
    }
}
