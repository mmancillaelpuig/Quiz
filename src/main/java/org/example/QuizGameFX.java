package org.example;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class QuizGameFX extends Application {

    private Stage primaryStage;
    private Scene loginScene, gameScene;
    private String playerName;
    private int score = 0;
    private int questionIndex = 0;
    private List<String[]> preguntas = new ArrayList<>();

    private Label questionLabel;
    private TextField answerField;
    private Label feedbackLabel;
    private TableView<RankingEntry> rankingTable;
    private ObservableList<RankingEntry> rankingData = FXCollections.observableArrayList();
    private boolean isAnswerChecked = false;
    private Button nextButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            FirebaseManager.initFirebase();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Preguntas
        preguntas.addAll(Arrays.asList(
                new String[]{"¿Cuál es la capital de Francia?", "Paris"},
                new String[]{"¿Cuánto es 5 + 3?", "8"},
                new String[]{"¿De qué color es el cielo en un día despejado?", "Azul"},
                new String[]{"¿Quién escribió 'Don Quijote de la Mancha'?", "Miguel de Cervantes"},
                new String[]{"¿Cuál es el río más largo del mundo?", "Amazonas"},
                new String[]{"¿En qué año llegó el hombre a la Luna?", "1969"},
                new String[]{"¿Cuál es el elemento químico con símbolo 'Au'?", "Oro"},
                new String[]{"¿Qué planeta es conocido como el planeta rojo?", "Marte"},
                new String[]{"¿Cuántos huesos tiene el cuerpo humano adulto?", "206"},
                new String[]{"¿Quién pintó la Mona Lisa?", "Leonardo da Vinci"},
                new String[]{"¿Cuál es la capital de Australia?", "Canberra"},
                new String[]{"¿Qué significa 'www' en las páginas web?", "World Wide Web"},
                new String[]{"¿Cuál es el país más grande del mundo?", "Rusia"},
                new String[]{"¿En qué continente se encuentra Egipto?", "África"},
                new String[]{"¿Cuál es el océano más grande?", "Pacífico"},
                new String[]{"¿Qué artista pintó 'La noche estrellada'?", "Van Gogh"},
                new String[]{"¿Cuál es el hueso más largo del cuerpo humano?", "Fémur"},
                new String[]{"¿Qué planeta tiene anillos visibles?", "Saturno"},
                new String[]{"¿Cuál es la fórmula del agua?", "H2O"},
                new String[]{"¿En qué año comenzó la Segunda Guerra Mundial?", "1939"},
                new String[]{"¿Cuál es la capital de Canadá?", "Ottawa"},
                new String[]{"¿Qué país tiene forma de bota?", "Italia"},
                new String[]{"¿Cuál es el animal más grande del mundo?", "Ballena azul"},
                new String[]{"¿Qué vitamina se obtiene de la luz solar?", "Vitamina D"},
                new String[]{"¿Cuál es el país más poblado del mundo?", "China"},
                new String[]{"¿Qué planeta está más cerca del Sol?", "Mercurio"},
                new String[]{"¿Cuál es el instrumento musical nacional de Japón?", "Koto"},
                new String[]{"¿En qué deporte se usa un puck?", "Hockey"},
                new String[]{"¿Qué elemento tiene el símbolo químico 'Fe'?", "Hierro"},
                new String[]{"¿Cuántos lados tiene un heptágono?", "7"},
                new String[]{"¿Quién fue el primer hombre en el espacio?", "Yuri Gagarin"},
                new String[]{"¿Cuál es la capital de Brasil?", "Brasilia"},
                new String[]{"¿En qué año se hundió el Titanic?", "1912"},
                new String[]{"¿Qué órgano produce insulina?", "Páncreas"},
                new String[]{"¿Cuál es el país con más islas del mundo?", "Suecia"},
                new String[]{"¿Qué poeta escribió 'La Divina Comedia'?", "Dante Alighieri"},
                new String[]{"¿Cuál es el desierto más grande del mundo?", "Sahara"},
                new String[]{"¿Qué país inventó el sushi?", "Japón"},
                new String[]{"¿Cuántos planetas hay en nuestro sistema solar?", "8"},
                new String[]{"¿Qué artista es conocido como 'El Rey del Pop'?", "Michael Jackson"},
                new String[]{"¿Cuál es la montaña más alta del mundo?", "Everest"},
                new String[]{"¿Qué país tiene la bandera con una hoja de arce?", "Canadá"},
                new String[]{"¿En qué deporte se usa el término 'hole in one'?", "Golf"},
                new String[]{"¿Qué científico formuló la teoría de la relatividad?", "Einstein"},
                new String[]{"¿Cuál es el libro más vendido de la historia?", "Biblia"},
                new String[]{"¿Qué país tiene como capital Nairobi?", "Kenia"},
                new String[]{"¿Cuál es el metal líquido a temperatura ambiente?", "Mercurio"},
                new String[]{"¿Qué fruto seco produce el árbol del nogal?", "Nuez"},
                new String[]{"¿En qué país se encuentra la Torre Eiffel?", "Francia"},
                new String[]{"¿Qué órgano bombea sangre en el cuerpo?", "Corazón"}
        ));

        Collections.shuffle(preguntas);

        createLoginScene();
        createGameScene();

        primaryStage.setTitle("🎯 Juego de Trivial");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        FirebaseManager.listenRanking(ranking -> Platform.runLater(() -> rankingData.setAll(ranking)));
    }

    private void createLoginScene() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1e1e1e;");

        Label label = new Label("🎮 Tu nombre:");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        TextField nameField = new TextField();
        nameField.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px;");

        Button startButton = new Button("🚀 Empezar");
        startButton.setStyle(buttonStyle());

        startButton.setOnAction(e -> {
            playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Ingresa un nombre.").showAndWait();
            } else {
                FirebaseManager.checkOrCreateUser(playerName, () -> Platform.runLater(() -> {
                    primaryStage.setScene(gameScene);
                    nextQuestion();
                }));
            }
        });

        layout.getChildren().addAll(label, nameField, startButton);
        loginScene = new Scene(layout, 400, 200);
    }

    private void createGameScene() {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #1e1e1e;");

        // Tabla ranking
        rankingTable = new TableView<>(rankingData);
        rankingTable.setStyle("-fx-background-color: #2e2e2e; -fx-table-cell-border-color: transparent;");
        rankingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rankingTable.setPlaceholder(new Label("Sin jugadores aún"));

        TableColumn<RankingEntry, String> playerCol = new TableColumn<>("Jugador");
        playerCol.setCellValueFactory(new PropertyValueFactory<>("player"));
        playerCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<RankingEntry, Integer> scoreCol = new TableColumn<>("Puntuación");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setStyle("-fx-alignment: CENTER;");

        rankingTable.getColumns().addAll(playerCol, scoreCol);
        VBox rankingBox = new VBox(10);
        rankingBox.setPadding(new Insets(10));
        rankingBox.setStyle("-fx-background-color: #2e2e2e;");

        rankingTable.setMaxHeight(150); // ⬅️ Altura reducida

        rankingBox.getChildren().addAll(new Label("🏆 Ranking:"), rankingTable);
        layout.setTop(rankingBox);


        VBox questionBox = new VBox(15);
        questionBox.setPadding(new Insets(30));

        questionLabel = new Label();
        questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        answerField = new TextField();
        answerField.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 16px;");

        feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #bbb; -fx-font-size: 16px;");

        nextButton = new Button("Siguiente");
        nextButton.setStyle(buttonStyle());
        nextButton.setOnAction(e -> handleAnswer());

        questionBox.getChildren().addAll(questionLabel, answerField, nextButton, feedbackLabel);
        layout.setCenter(questionBox);

        gameScene = new Scene(layout, 700, 500);
    }

    private String buttonStyle() {
        return "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 16px; " +
                "-fx-background-radius: 8px; -fx-padding: 10px 20px; " +
                "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, black, 5, 0.2, 0, 2);";
    }

    private void nextQuestion() {
        if (questionIndex < preguntas.size()) {
            String[] currentQuestion = preguntas.get(questionIndex);
            questionLabel.setText(currentQuestion[0]);
            answerField.clear();
            feedbackLabel.setText("");
            nextButton.setText("Comprobar");
            isAnswerChecked = false;
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Juego terminado. Tu puntuación final es: " + score);
            alert.showAndWait();
            Platform.exit();
        }
    }

    private void handleAnswer() {
        if (!isAnswerChecked) {
            String[] currentQuestion = preguntas.get(questionIndex);
            String correctAnswer = currentQuestion[1];
            String userAnswer = answerField.getText().trim();

            if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                feedbackLabel.setText("✅ ¡Correcto!");
                score++;
            } else {
                feedbackLabel.setText("❌ Incorrecto. La respuesta correcta es: " + correctAnswer);
            }

            CountDownLatch latch = new CountDownLatch(1);
            FirebaseManager.updateScore(playerName, score, latch);
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            isAnswerChecked = true;
            nextButton.setText("Continuar");
        } else {
            questionIndex++;
            nextQuestion();
            isAnswerChecked = false;
            nextButton.setText("Siguiente");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
