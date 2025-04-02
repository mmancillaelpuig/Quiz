package org.example;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class QuizGame {

    private static final String[][] PREGUNTAS = {
            {"¿Cuál es la capital de Francia?", "Paris"},
            {"¿Cuánto es 5 + 3?", "8"},
            {"¿De qué color es el cielo en un día despejado?", "Azul"},
            {"¿Quién escribió 'Don Quijote de la Mancha'?", "Miguel de Cervantes"},
            {"¿Cuál es el río más largo del mundo?", "Amazonas"},
            {"¿En qué año llegó el hombre a la Luna?", "1969"},
            {"¿Cuál es el elemento químico con símbolo 'Au'?", "Oro"},
            {"¿Qué planeta es conocido como el planeta rojo?", "Marte"},
            {"¿Cuántos huesos tiene el cuerpo humano adulto?", "206"},
            {"¿Quién pintó la Mona Lisa?", "Leonardo da Vinci"},
            {"¿Cuál es la capital de Australia?", "Canberra"},
            {"¿Qué significa 'www' en las páginas web?", "World Wide Web"},
            {"¿Cuál es el país más grande del mundo?", "Rusia"},
            {"¿En qué continente se encuentra Egipto?", "África"},
            {"¿Cuál es el océano más grande?", "Pacífico"},
            {"¿Qué artista pintó 'La noche estrellada'?", "Van Gogh"},
            {"¿Cuál es el hueso más largo del cuerpo humano?", "Fémur"},
            {"¿Qué planeta tiene anillos visibles?", "Saturno"},
            {"¿Cuál es la fórmula del agua?", "H2O"},
            {"¿En qué año comenzó la Segunda Guerra Mundial?", "1939"},
            {"¿Cuál es la capital de Canadá?", "Ottawa"},
            {"¿Qué país tiene forma de bota?", "Italia"},
            {"¿Cuál es el animal más grande del mundo?", "Ballena azul"},
            {"¿Qué vitamina se obtiene de la luz solar?", "Vitamina D"},
            {"¿Cuál es el país más poblado del mundo?", "China"},
            {"¿Qué planeta está más cerca del Sol?", "Mercurio"},
            {"¿Cuál es el instrumento musical nacional de Japón?", "Koto"},
            {"¿En qué deporte se usa un puck?", "Hockey"},
            {"¿Qué elemento tiene el símbolo químico 'Fe'?", "Hierro"},
            {"¿Cuántos lados tiene un heptágono?", "7"},
            {"¿Quién fue el primer hombre en el espacio?", "Yuri Gagarin"},
            {"¿Cuál es la capital de Brasil?", "Brasilia"},
            {"¿En qué año se hundió el Titanic?", "1912"},
            {"¿Qué órgano produce insulina?", "Páncreas"},
            {"¿Cuál es el país con más islas del mundo?", "Suecia"},
            {"¿Qué poeta escribió 'La Divina Comedia'?", "Dante Alighieri"},
            {"¿Cuál es el desierto más grande del mundo?", "Sahara"},
            {"¿Qué país inventó el sushi?", "Japón"},
            {"¿Cuántos planetas hay en nuestro sistema solar?", "8"},
            {"¿Qué artista es conocido como 'El Rey del Pop'?", "Michael Jackson"},
            {"¿Cuál es la montaña más alta del mundo?", "Everest"},
            {"¿Qué país tiene la bandera con una hoja de arce?", "Canadá"},
            {"¿En qué deporte se usa el término 'hole in one'?", "Golf"},
            {"¿Qué científico formuló la teoría de la relatividad?", "Einstein"},
            {"¿Cuál es el libro más vendido de la historia?", "Biblia"},
            {"¿Qué país tiene como capital Nairobi?", "Kenia"},
            {"¿Cuál es el metal líquido a temperatura ambiente?", "Mercurio"},
            {"¿Qué fruto seco produce el árbol del nogal?", "Nuez"},
            {"¿En qué país se encuentra la Torre Eiffel?", "Francia"},
            {"¿Qué órgano bombea sangre en el cuerpo?", "Corazón"}
    };


    public static void main(String[] args) {
        try {
            FirebaseManager.initFirebase();
            FirebaseManager.listenRanking();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa tu nombre: ");
        String playerId = scanner.nextLine();

        final boolean[] usuarioListo = {false};
        FirebaseManager.checkOrCreateUser(playerId, () -> {
            usuarioListo[0] = true;
            System.out.println("¡Bienvenido " + playerId + "!");
        });

        while(!usuarioListo[0]) {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        List<String[]> preguntasMezcladas = new ArrayList<>(Arrays.asList(PREGUNTAS));
        Collections.shuffle(preguntasMezcladas);

        int score = 0;

        for (String[] pregunta : preguntasMezcladas) {
            System.out.println(pregunta[0]);
            String respuesta = scanner.nextLine();

            if (respuesta.equalsIgnoreCase(pregunta[1])) {
                score++;
                System.out.println("¡Correcto! Puntuación actual: " + score);
            } else {
                System.out.println("Incorrecto. La respuesta correcta es: " + pregunta[1]);
            }

            final CountDownLatch latch = new CountDownLatch(1);
            FirebaseManager.updateScore(playerId, score, latch);
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Juego terminado. Tu puntuación final es: " + score);
        scanner.close();

        FirebaseManager.listenRanking();
    }
}
